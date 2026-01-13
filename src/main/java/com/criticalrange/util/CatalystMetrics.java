package com.criticalrange.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Performance metrics collection for Catalyst optimizations.
 * Tracks timing, counts, and provides reporting.
 */
public class CatalystMetrics {
    
    /** Method timing data (exponential moving average) */
    private static final Map<String, Long> timings = new ConcurrentHashMap<>();
    
    /** Call counts per method */
    private static final Map<String, AtomicLong> callCounts = new ConcurrentHashMap<>();
    
    /** Server tick tracking */
    private static volatile long lastTickTime = System.nanoTime();
    private static volatile double currentTPS = 20.0;
    private static final AtomicLong tickCount = new AtomicLong(0);
    
    /** Memory tracking */
    private static volatile long lastMemoryCheck = System.currentTimeMillis();
    private static volatile long lastUsedMemory = 0;
    
    /**
     * Start timing a method.
     * @param name Method identifier
     * @return Start time in nanoseconds
     */
    public static long start(String name) {
        return System.nanoTime();
    }
    
    /**
     * End timing a method and record the duration.
     * @param startTime The start time from start()
     * @param name Method identifier
     */
    public static void end(long startTime, String name) {
        long duration = System.nanoTime() - startTime;
        
        // Update timing with exponential moving average
        timings.compute(name, (k, v) -> v == null ? duration : (v * 9 + duration) / 10);
        
        // Increment call count
        callCounts.computeIfAbsent(name, k -> new AtomicLong()).incrementAndGet();
    }
    
    /**
     * Record a server tick for TPS calculation.
     */
    public static void onServerTick() {
        long now = System.nanoTime();
        long elapsed = now - lastTickTime;
        lastTickTime = now;
        
        // Calculate TPS (smoothed)
        double instantTPS = 1_000_000_000.0 / elapsed;
        currentTPS = (currentTPS * 0.95) + (instantTPS * 0.05);
        
        tickCount.incrementAndGet();
        
        // Update tick helper
        CatalystTickHelper.onServerTick();
        CatalystTickHelper.updateTPS(currentTPS);
    }
    
    /**
     * Get current TPS.
     */
    public static double getCurrentTPS() {
        return currentTPS;
    }
    
    /**
     * Get total tick count.
     */
    public static long getTickCount() {
        return tickCount.get();
    }
    
    /**
     * Get timing for a method in microseconds.
     */
    public static double getTimingMicros(String name) {
        Long timing = timings.get(name);
        return timing == null ? 0 : timing / 1000.0;
    }
    
    /**
     * Get call count for a method.
     */
    public static long getCallCount(String name) {
        AtomicLong count = callCounts.get(name);
        return count == null ? 0 : count.get();
    }
    
    /**
     * Get memory usage stats.
     */
    public static MemoryStats getMemoryStats() {
        Runtime runtime = Runtime.getRuntime();
        long total = runtime.totalMemory();
        long free = runtime.freeMemory();
        long used = total - free;
        long max = runtime.maxMemory();
        
        return new MemoryStats(used, free, total, max);
    }
    
    /**
     * Generate a performance report.
     */
    public static String generateReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n═══════════════════════════════════════════════\n");
        sb.append("         Catalyst Performance Report            \n");
        sb.append("═══════════════════════════════════════════════\n");
        
        // TPS
        sb.append(String.format("TPS: %.2f (ticks: %d)\n", currentTPS, tickCount.get()));
        
        // Memory
        MemoryStats mem = getMemoryStats();
        sb.append(String.format("Memory: %dMB / %dMB (%.1f%%)\n",
            mem.usedMB(), mem.maxMB(), mem.usagePercent()));
        
        // Tick optimization stats
        sb.append("\n── Tick Optimization ──\n");
        sb.append(String.format("  Ticks skipped: %d\n", CatalystTickHelper.getTicksSkipped()));
        sb.append(String.format("  Ticks processed: %d\n", CatalystTickHelper.getTicksProcessed()));
        sb.append(String.format("  Skip ratio: %.1f%%\n", CatalystTickHelper.getSkipRatio()));
        
        // Entity tracking stats
        sb.append("\n── Entity Tracking ──\n");
        sb.append(String.format("  Searches: %d\n", CatalystSpatialIndex.getSearchCount()));
        sb.append(String.format("  Optimized: %d\n", CatalystSpatialIndex.getSearchesOptimized()));
        sb.append(String.format("  Updates skipped: %d\n", CatalystSpatialIndex.getUpdatesSkipped()));
        
        // Chunk cache stats
        sb.append("\n── Chunk Cache ──\n");
        sb.append(String.format("  Cache hits: %d\n", CatalystChunkCache.getCacheHits()));
        sb.append(String.format("  Cache misses: %d\n", CatalystChunkCache.getCacheMisses()));
        sb.append(String.format("  Hit ratio: %.1f%%\n", CatalystChunkCache.getHitRatio()));
        sb.append(String.format("  Active chunks: %d\n", CatalystChunkCache.getActiveChunkCount()));
        sb.append(String.format("  Chunks loaded: %d\n", CatalystChunkCache.getChunksLoaded()));
        sb.append(String.format("  Chunks unloaded: %d\n", CatalystChunkCache.getChunksUnloaded()));
        
        // Top methods by time
        sb.append("\n── Hot Methods ──\n");
        timings.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .forEach(e -> sb.append(String.format("  %s: %.2fμs\n",
                e.getKey(), e.getValue() / 1000.0)));
        
        sb.append("═══════════════════════════════════════════════\n");
        
        return sb.toString();
    }
    
    /**
     * Reset all metrics.
     */
    public static void reset() {
        timings.clear();
        callCounts.clear();
        tickCount.set(0);
        CatalystTickHelper.resetStats();
        CatalystSpatialIndex.resetMetrics();
    }
    
    /**
     * Memory statistics record.
     */
    public record MemoryStats(long used, long free, long total, long max) {
        public long usedMB() {
            return used / (1024 * 1024);
        }
        
        public long freeMB() {
            return free / (1024 * 1024);
        }
        
        public long totalMB() {
            return total / (1024 * 1024);
        }
        
        public long maxMB() {
            return max / (1024 * 1024);
        }
        
        public double usagePercent() {
            return (used * 100.0) / max;
        }
    }
}
