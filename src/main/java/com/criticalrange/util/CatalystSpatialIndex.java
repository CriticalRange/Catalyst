package com.criticalrange.util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Spatial index and entity tracking optimization helper.
 * 
 * NOTE: Hytale already implements spatial partitioning via SpatialStructure.
 * This class now focuses on:
 * 1. Tracking metrics for entity tracking operations
 * 2. Rate limiting/batching under heavy load
 * 3. Providing statistics for performance monitoring
 */
public class CatalystSpatialIndex {
    
    // ========== Configuration ==========
    
    /** Maximum packet sends per tick before batching kicks in */
    private static final int MAX_PACKET_SENDS_PER_TICK = 500;
    
    /** TPS threshold below which we enable aggressive batching */
    private static final double LOW_TPS_THRESHOLD = 18.0;
    
    // ========== State ==========
    
    /** Current tick counter */
    private static final AtomicLong tickCounter = new AtomicLong(0);
    
    /** Packet sends this tick */
    private static final AtomicInteger packetSendsThisTick = new AtomicInteger(0);
    
    // ========== Metrics ==========
    
    private static final AtomicLong searchCount = new AtomicLong(0);
    private static final AtomicLong searchesOptimized = new AtomicLong(0);
    private static final AtomicLong updatesSkipped = new AtomicLong(0);
    private static final AtomicLong packetsSent = new AtomicLong(0);
    private static final AtomicLong packetsSkipped = new AtomicLong(0);
    private static final AtomicLong viewerClears = new AtomicLong(0);
    private static final AtomicLong visibilityAdds = new AtomicLong(0);
    
    // ========== Entity Tracker Optimization ==========
    
    /**
     * Called when an entity search/visibility collection is performed.
     * Used for metrics tracking.
     */
    public static void onEntitySearch() {
        searchCount.incrementAndGet();
    }
    
    /**
     * Called when a viewer clear operation is performed.
     */
    public static void onViewerClear() {
        viewerClears.incrementAndGet();
    }
    
    /**
     * Called when an entity is added to visibility list.
     */
    public static void onVisibilityAdd() {
        visibilityAdds.incrementAndGet();
    }
    
    /**
     * Determine if packet send operations should proceed for this entity.
     * Under heavy load, batches packet sends by skipping some.
     * 
     * @param index The entity index
     * @return true to send packets, false to skip/defer
     */
    public static boolean shouldSendPackets(int index) {
        if (!com.criticalrange.CatalystConfig.ENTITY_TRACKING_ENABLED) {
            packetsSent.incrementAndGet();
            return true;
        }
        
        int sendCount = packetSendsThisTick.incrementAndGet();
        double tps = CatalystMetrics.getCurrentTPS();
        
        // Always allow up to the limit
        if (sendCount <= MAX_PACKET_SENDS_PER_TICK && tps > LOW_TPS_THRESHOLD) {
            packetsSent.incrementAndGet();
            return true;
        }
        
        // Under load: use distributed scheduling
        long tick = tickCounter.get();
        
        // If over limit or low TPS, only allow 1/2 of sends
        if (tps < LOW_TPS_THRESHOLD) {
            if ((index + tick) % 2 != 0) {
                packetsSkipped.incrementAndGet();
                return false;
            }
        } else if (sendCount > MAX_PACKET_SENDS_PER_TICK) {
            // Over limit but TPS OK - skip every other
            if ((index + tick) % 2 != 0) {
                packetsSkipped.incrementAndGet();
                return false;
            }
        }
        
        packetsSent.incrementAndGet();
        return true;
    }
    
    /**
     * Legacy method - determines if entity tracker should update this tick.
     * Kept for backwards compatibility with existing injections.
     */
    public static boolean shouldUpdateTracker() {
        if (!com.criticalrange.CatalystConfig.ENTITY_TRACKING_ENABLED) {
            return true;
        }
        
        long tick = tickCounter.get();
        double tps = CatalystMetrics.getCurrentTPS();
        
        // Under normal load, always update
        if (tps > LOW_TPS_THRESHOLD) {
            return true;
        }
        
        // Under heavy load, skip every other tick
        if (tick % 2 == 0) {
            return true;
        }
        
        updatesSkipped.incrementAndGet();
        return false;
    }
    
    /**
     * Called once per server tick to reset per-tick counters.
     */
    public static void onServerTick() {
        tickCounter.incrementAndGet();
        packetSendsThisTick.set(0);
    }
    
    // ========== Statistics ==========
    
    public static long getSearchCount() {
        return searchCount.get();
    }
    
    public static long getSearchesOptimized() {
        return searchesOptimized.get();
    }
    
    public static long getUpdatesSkipped() {
        return updatesSkipped.get();
    }
    
    public static long getPacketsSent() {
        return packetsSent.get();
    }
    
    public static long getPacketsSkipped() {
        return packetsSkipped.get();
    }
    
    public static long getViewerClears() {
        return viewerClears.get();
    }
    
    public static long getVisibilityAdds() {
        return visibilityAdds.get();
    }
    
    public static double getPacketSkipRatio() {
        long sent = packetsSent.get();
        long skipped = packetsSkipped.get();
        long total = sent + skipped;
        if (total == 0) return 0;
        return (skipped * 100.0) / total;
    }
    
    /**
     * Reset metrics.
     */
    public static void resetMetrics() {
        searchCount.set(0);
        searchesOptimized.set(0);
        updatesSkipped.set(0);
        packetsSent.set(0);
        packetsSkipped.set(0);
        viewerClears.set(0);
        visibilityAdds.set(0);
    }
    
    /**
     * Clear all data (for shutdown).
     */
    public static void clear() {
        resetMetrics();
    }
}
