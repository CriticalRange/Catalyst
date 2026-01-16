package com.criticalrange.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * AI optimization helper.
 *
 * <p>Called by injected bytecode in AI-related systems.</p>
 *
 * <p>Optimizations implemented:</p>
 * <ul>
 *   <li>Distributes AI updates across multiple ticks</li>
 *   <li>Skips path smoothing under heavy load</li>
 *   <li>Tracks AI performance metrics</li>
 * </ul>
 */
public class CatalystAiHelper {

    /**
     * Private constructor to prevent instantiation.
     */
    private CatalystAiHelper() {
    }

    // ========== Configuration ==========
    
    /** Number of tick groups to distribute AI updates */
    private static final int AI_TICK_GROUPS = 5;
    
    /** TPS threshold for heavy load */
    private static final double HEAVY_LOAD_TPS = 18.0;
    
    /** TPS threshold for critical load */
    private static final double CRITICAL_LOAD_TPS = 15.0;
    
    // ========== State ==========
    
    private static final AtomicLong tickCounter = new AtomicLong(0);
    
    // ========== Statistics ==========
    
    private static final AtomicLong aiSkipped = new AtomicLong(0);
    private static final AtomicLong aiProcessed = new AtomicLong(0);
    private static final AtomicLong pathSmoothSkipped = new AtomicLong(0);
    private static final AtomicLong pathSmoothProcessed = new AtomicLong(0);
    
    // ========== Optimization Logic ==========
    
    /**
     * Determines if AI should be updated for this entity.
     * Uses index-based distribution to spread AI load across ticks.
     *
     * @param index Entity index within the ArchetypeChunk
     * @return true to update AI, false to skip
     */
    public static boolean shouldTickAi(int index) {
        if (!com.criticalrange.CatalystConfig.AI_OPTIMIZATION_ENABLED) {
            aiProcessed.incrementAndGet();
            return true;
        }
        
        long tick = tickCounter.get();
        double tps = CatalystMetrics.getCurrentTPS();
        
        // Determine how many tick groups are active based on load
        int activeGroups;
        if (tps < CRITICAL_LOAD_TPS) {
            // Critical load: only 1/10 of NPCs update per tick
            activeGroups = 1;
        } else if (tps < HEAVY_LOAD_TPS) {
            // Heavy load: only 2/5 of NPCs update per tick
            activeGroups = 2;
        } else {
            // Normal: all NPCs can update, but still use distribution
            activeGroups = AI_TICK_GROUPS;
        }
        
        // Check if this entity's group is active this tick
        int entityGroup = Math.abs(index) % AI_TICK_GROUPS;
        int currentActiveGroup = (int)(tick % AI_TICK_GROUPS);
        
        // An entity is active if its group is within the active range
        boolean shouldTick = (entityGroup < activeGroups) || 
                            (Math.abs(entityGroup - currentActiveGroup) < activeGroups);
        
        if (shouldTick) {
            aiProcessed.incrementAndGet();
        } else {
            aiSkipped.incrementAndGet();
        }
        
        return shouldTick;
    }
    
    /**
     * Determines if path smoothing should proceed.
     * Path smoothing is purely aesthetic and can be skipped under load.
     *
     * @return true to smooth path, false to skip
     */
    public static boolean shouldSmoothPath() {
        if (!com.criticalrange.CatalystConfig.AI_OPTIMIZATION_ENABLED) {
            pathSmoothProcessed.incrementAndGet();
            return true;
        }
        
        double tps = CatalystMetrics.getCurrentTPS();
        long tick = tickCounter.get();
        
        // Under critical load, skip all path smoothing
        if (tps < CRITICAL_LOAD_TPS) {
            pathSmoothSkipped.incrementAndGet();
            return false;
        }
        
        // Under heavy load, only smooth every 4th tick
        if (tps < HEAVY_LOAD_TPS) {
            if (tick % 4 != 0) {
                pathSmoothSkipped.incrementAndGet();
                return false;
            }
        }
        
        // Normal load: smooth every other tick (still provides savings)
        if (tick % 2 != 0) {
            pathSmoothSkipped.incrementAndGet();
            return false;
        }
        
        pathSmoothProcessed.incrementAndGet();
        return true;
    }
    
    /**
     * Called once per server tick.
     */
    public static void onServerTick() {
        tickCounter.incrementAndGet();
    }
    
    // ========== Statistics ==========

    /**
     * Gets the number of AI updates that were skipped.
     *
     * @return Total skipped AI updates
     */
    public static long getSkipped() {
        return aiSkipped.get();
    }

    /**
     * Gets the number of AI updates that were processed.
     *
     * @return Total processed AI updates
     */
    public static long getProcessed() {
        return aiProcessed.get();
    }

    /**
     * Gets the number of path smoothing operations that were skipped.
     *
     * @return Total skipped path smooths
     */
    public static long getPathSmoothSkipped() {
        return pathSmoothSkipped.get();
    }

    /**
     * Gets the number of path smoothing operations that were processed.
     *
     * @return Total processed path smooths
     */
    public static long getPathSmoothProcessed() {
        return pathSmoothProcessed.get();
    }

    /**
     * Gets the skip ratio as a percentage.
     *
     * @return Skip ratio (0-100)
     */
    public static double getSkipRatio() {
        long skipped = aiSkipped.get();
        long processed = aiProcessed.get();
        long total = skipped + processed;
        if (total == 0) return 0;
        return (skipped * 100.0) / total;
    }

    /**
     * Resets all statistics to zero.
     */
    public static void resetStats() {
        aiSkipped.set(0);
        aiProcessed.set(0);
        pathSmoothSkipped.set(0);
        pathSmoothProcessed.set(0);
    }
}
