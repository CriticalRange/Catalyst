package com.criticalrange.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Network optimization helper.
 * Controls packet flush batching under heavy load.
 */
public class CatalystNetworkHelper {
    
    // ========== Configuration ==========
    
    /** TPS threshold for heavy load */
    private static final double HEAVY_LOAD_TPS = 18.0;
    
    /** TPS threshold for critical load */
    private static final double CRITICAL_LOAD_TPS = 15.0;
    
    // ========== State ==========
    
    private static final AtomicLong tickCounter = new AtomicLong(0);
    
    // ========== Statistics ==========
    
    private static final AtomicLong flushesSkipped = new AtomicLong(0);
    private static final AtomicLong flushesProcessed = new AtomicLong(0);
    
    // ========== Optimization Logic ==========
    
    /**
     * Determines if the network connection should be flushed this tick.
     * Under heavy load, batches flushes to reduce CPU context switches.
     * 
     * @return true to flush, false to batch/skip
     */
    public static boolean shouldFlush() {
        if (!com.criticalrange.CatalystConfig.NETWORK_OPTIMIZATION_ENABLED) {
            flushesProcessed.incrementAndGet();
            return true;
        }
        
        double tps = CatalystMetrics.getCurrentTPS();
        long tick = tickCounter.get();
        
        // If TPS is good, flush every tick for best latency
        if (tps > HEAVY_LOAD_TPS) {
            flushesProcessed.incrementAndGet();
            return true;
        }
        
        // Under heavy load, flush every other tick
        if (tps > CRITICAL_LOAD_TPS) {
            if (tick % 2 == 0) {
                flushesProcessed.incrementAndGet();
                return true;
            }
            flushesSkipped.incrementAndGet();
            return false;
        }
        
        // Under critical load, flush every 4 ticks
        if (tick % 4 == 0) {
            flushesProcessed.incrementAndGet();
            return true;
        }
        
        flushesSkipped.incrementAndGet();
        return false;
    }
    
    /**
     * Called once per server tick.
     */
    public static void onServerTick() {
        tickCounter.incrementAndGet();
    }
    
    // ========== Statistics ==========
    
    public static long getSkipped() {
        return flushesSkipped.get();
    }
    
    public static long getProcessed() {
        return flushesProcessed.get();
    }
    
    public static double getSkipRatio() {
        long skipped = flushesSkipped.get();
        long processed = flushesProcessed.get();
        long total = skipped + processed;
        if (total == 0) return 0;
        return (skipped * 100.0) / total;
    }
    
    public static void resetStats() {
        flushesSkipped.set(0);
        flushesProcessed.set(0);
    }
}
