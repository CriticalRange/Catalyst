package com.criticalrange.util;

/**
 * Performance metrics for Catalyst.
 *
 * <p>Provides memory statistics and lazy loading status reporting.</p>
 */
public class CatalystMetrics {

    /**
     * Private constructor to prevent instantiation.
     */
    private CatalystMetrics() {
    }

    /**
     * Generates a performance report.
     *
     * @return Formatted report string
     */
    public static String generateReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n==============================================\n");
        sb.append("         Catalyst Performance Report            \n");
        sb.append("==============================================\n");

        // Memory
        MemoryStats mem = getMemoryStats();
        sb.append(String.format("Memory: %dMB / %dMB (%.1f%%)\n",
            mem.usedMB(), mem.maxMB(), mem.usagePercent()));

        // Lazy loading status
        sb.append("\n-- Lazy Loading (Chunk Gen) --\n");
        sb.append(String.format("  Block Entities: %s\n", 
            com.criticalrange.CatalystConfig.LAZY_BLOCK_ENTITIES_ENABLED ? "ENABLED" : "DISABLED"));
        sb.append(String.format("  Block Tick: %s\n", 
            com.criticalrange.CatalystConfig.LAZY_BLOCK_TICK_ENABLED ? "ENABLED" : "DISABLED"));
        sb.append(String.format("  Fluid: %s\n", 
            com.criticalrange.CatalystConfig.LAZY_FLUID_ENABLED ? "ENABLED" : "DISABLED"));

        // Runtime optimizations status
        sb.append("\n-- Runtime Optimizations --\n");
        sb.append(String.format("  Entity Distance: %s\n", 
            com.criticalrange.CatalystConfig.ENTITY_DISTANCE_ENABLED ? "ENABLED" : "DISABLED"));
        sb.append(String.format("  Chunk Rate: %s\n", 
            com.criticalrange.CatalystConfig.CHUNK_RATE_ENABLED ? "ENABLED" : "DISABLED"));

        // Configuration values
        sb.append("\n-- Configuration --\n");
        sb.append(String.format("  Entity View Multiplier: %d (default 32)\n", 
            com.criticalrange.CatalystConfig.ENTITY_VIEW_MULTIPLIER));
        sb.append(String.format("  Chunks Per Tick: %d (default 4)\n", 
            com.criticalrange.CatalystConfig.CHUNKS_PER_TICK));

        sb.append("==============================================\n");

        return sb.toString();
    }

    /**
     * Gets memory usage statistics.
     *
     * @return MemoryStats containing current memory usage
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
     * Memory statistics record.
     *
     * @param used Used memory in bytes
     * @param free Free memory in bytes
     * @param total Total memory in bytes
     * @param max Maximum memory in bytes
     */
    public record MemoryStats(long used, long free, long total, long max) {

        /** Gets used memory in megabytes */
        public long usedMB() {
            return used / (1024 * 1024);
        }

        /** Gets free memory in megabytes */
        public long freeMB() {
            return free / (1024 * 1024);
        }

        /** Gets total memory in megabytes */
        public long totalMB() {
            return total / (1024 * 1024);
        }

        /** Gets maximum memory in megabytes */
        public long maxMB() {
            return max / (1024 * 1024);
        }

        /** Gets memory usage as a percentage */
        public double usagePercent() {
            return (used * 100.0) / max;
        }
    }
}
