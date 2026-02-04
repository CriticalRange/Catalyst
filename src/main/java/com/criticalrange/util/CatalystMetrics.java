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

        // Runtime optimizations status
        sb.append("\n-- Runtime Optimizations --\n");
        sb.append(String.format("  Entity Distance: %s (multiplier: %d)\n", 
            status(com.criticalrange.CatalystConfig.ENTITY_DISTANCE_ENABLED),
            com.criticalrange.CatalystConfig.ENTITY_VIEW_MULTIPLIER));
        sb.append(String.format("  Chunk Rate: %s (chunks/tick: %d)\n", 
            status(com.criticalrange.CatalystConfig.CHUNK_RATE_ENABLED),
            com.criticalrange.CatalystConfig.CHUNKS_PER_TICK));

        // Pathfinding
        sb.append("\n-- Pathfinding --\n");
        sb.append(String.format("  Custom Limits: %s\n", 
            status(com.criticalrange.CatalystConfig.PATHFINDING_ENABLED)));
        sb.append(String.format("  Max Path Length: %d (default 200)\n", 
            com.criticalrange.CatalystConfig.MAX_PATH_LENGTH));
        sb.append(String.format("  Open Nodes Limit: %d (default 80)\n", 
            com.criticalrange.CatalystConfig.OPEN_NODES_LIMIT));
        sb.append(String.format("  Total Nodes Limit: %d (default 400)\n", 
            com.criticalrange.CatalystConfig.TOTAL_NODES_LIMIT));

        // Chunk Generation
        sb.append("\n-- Chunk Generation --\n");
        sb.append(String.format("  Pool Size Override: %s (size: %d)\n", 
            status(com.criticalrange.CatalystConfig.CHUNK_POOL_SIZE_ENABLED),
            com.criticalrange.CatalystConfig.CHUNK_POOL_SIZE));
        sb.append(String.format("  Cache Size Override: %s\n", 
            status(com.criticalrange.CatalystConfig.CHUNK_CACHE_SIZE_ENABLED)));
        if (com.criticalrange.CatalystConfig.CHUNK_CACHE_SIZE_ENABLED) {
            sb.append(String.format("    Generator Cache: %d\n", 
                com.criticalrange.CatalystConfig.GENERATOR_CACHE_SIZE));
            sb.append(String.format("    Cave Cache: %d\n", 
                com.criticalrange.CatalystConfig.CAVE_CACHE_SIZE));
            sb.append(String.format("    Prefab Cache: %d\n", 
                com.criticalrange.CatalystConfig.PREFAB_CACHE_SIZE));
        }
        sb.append(String.format("  Thread Priority Override: %s (priority: %d)\n", 
            status(com.criticalrange.CatalystConfig.CHUNK_THREAD_PRIORITY_ENABLED),
            com.criticalrange.CatalystConfig.CHUNK_THREAD_PRIORITY));

        // Lighting
        sb.append("\n-- Lighting --\n");
        sb.append(String.format("  Skip Empty Sections: %s\n", 
            status(com.criticalrange.CatalystConfig.LIGHT_SKIP_EMPTY_ENABLED)));

        // Visual Effects
        sb.append("\n-- Visual Effects --\n");
        sb.append(String.format("  Particles: %s\n", 
            status(com.criticalrange.CatalystConfig.PARTICLES_ENABLED)));
        sb.append(String.format("  Animations: %s\n", 
            status(com.criticalrange.CatalystConfig.ANIMATIONS_ENABLED)));

        // Caching Optimizations
        sb.append("\n-- Caching Optimizations --\n");
        sb.append(String.format("  Block Section Cache: %s\n", 
            status(com.criticalrange.CatalystConfig.BLOCK_SECTION_CACHE_ENABLED)));
        sb.append(String.format("  Block Type Cache: %s (size: %d)\n", 
            status(com.criticalrange.CatalystConfig.BLOCK_TYPE_CACHE_ENABLED),
            com.criticalrange.CatalystConfig.BLOCK_TYPE_CACHE_SIZE));
        sb.append(String.format("  Local Chunk Cache: %s\n", 
            status(com.criticalrange.CatalystConfig.LOCAL_CHUNK_CACHE_ENABLED)));

        sb.append("==============================================\n");

        return sb.toString();
    }

    /**
     * Returns a formatted status string for boolean config values.
     */
    private static String status(boolean enabled) {
        return enabled ? "ON" : "OFF";
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
