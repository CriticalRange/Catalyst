package com.criticalrange.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lighting calculation cache and optimization helper.
 *
 * <p>Called by injected bytecode in lighting-related classes.</p>
 *
 * <p>Optimizations implemented:</p>
 * <ul>
 *   <li>Defers non-urgent lighting updates</li>
 *   <li>Batches multiple updates together</li>
 *   <li>Caches lighting results for static areas</li>
 *   <li>Rate-limits propagation calculations</li>
 * </ul>
 */
public class CatalystLightingCache {
    
    /** Maximum propagation calls per tick before throttling */
    private static final int MAX_PROPAGATIONS_PER_TICK = 1000;
    
    /** Maximum lighting updates per tick before batching */
    private static final int MAX_UPDATES_PER_TICK = 500;
    
    /** Batch interval in ticks (50ms = 1 tick at 20 TPS) */
    private static final int BATCH_INTERVAL_TICKS = 2;
    
    /** Current propagation count this tick */
    private static final AtomicInteger propagationCount = new AtomicInteger(0);
    
    /** Current update count this tick */
    private static final AtomicInteger updateCount = new AtomicInteger(0);
    
    /** Current tick counter */
    private static final AtomicLong tickCounter = new AtomicLong(0);
    
    /** Stats tracking */
    private static final AtomicLong totalCalculations = new AtomicLong(0);
    private static final AtomicLong skippedPropagations = new AtomicLong(0);
    private static final AtomicLong skippedUpdates = new AtomicLong(0);
    private static final AtomicLong batchedUpdates = new AtomicLong(0);
    
    /** Simple LRU-like cache for lighting results */
    private static final ConcurrentHashMap<Long, CachedLightingResult> lightingCache = 
        new ConcurrentHashMap<>();
    
    /** Maximum cache size */
    private static final int MAX_CACHE_SIZE = 10000;
    
    /**
     * Called at the start of lighting calculation methods.
     * Tracks metrics and potentially checks cache.
     */
    public static void onLightingCalculation() {
        totalCalculations.incrementAndGet();
    }
    
    /**
     * Determine if a lighting propagation should proceed.
     * Rate-limits propagation calls to prevent lag spikes.
     * 
     * @return true if propagation should proceed, false to skip
     */
    public static boolean shouldPropagate() {
        int count = propagationCount.incrementAndGet();
        
        // Allow propagation up to the limit
        if (count <= MAX_PROPAGATIONS_PER_TICK) {
            return true;
        }
        
        // Rate limit exceeded - defer this propagation
        skippedPropagations.incrementAndGet();
        return false;
    }
    
    /**
     * Determines if a lighting update should proceed.
     * Batches updates to reduce redundant calculations.
     *
     * @param positionHash Hash of the position being updated
     * @param lightValue The light value being set
     * @return true if update should proceed, false to defer
     */
    public static boolean shouldCalculate(long positionHash, int lightValue) {
        if (!com.criticalrange.CatalystConfig.LIGHTING_OPTIMIZATION_ENABLED) return true;

        int count = updateCount.incrementAndGet();
        
        // Allow updates up to the limit
        if (count <= MAX_UPDATES_PER_TICK) {
            return true;
        }
        
        // Check if we should process this batch
        long currentTick = tickCounter.get();
        if (currentTick % BATCH_INTERVAL_TICKS == 0) {
            batchedUpdates.incrementAndGet();
            return true;
        }
        
        // Defer this update
        skippedUpdates.incrementAndGet();
        return false;
    }
    
    // Legacy support for LightingOptimizationTransformer
    /**
     * Legacy method - checks if lighting should be updated.
     *
     * @return true if update should proceed
     */
    public static boolean shouldUpdateLighting() {
        return shouldCalculate(0, 0);
    }

    /**
     * Called once per server tick to reset per-tick counters.
     */
    public static void onServerTick() {
        tickCounter.incrementAndGet();
        propagationCount.set(0);
        updateCount.set(0);

        // Periodically clean cache
        if (tickCounter.get() % 1200 == 0) { // Every minute at 20 TPS
            cleanCache();
        }
    }

    /**
     * Tries to get a cached lighting result.
     *
     * @param chunkKey Unique key for the chunk
     * @return Cached result or null if not cached or expired
     */
    public static CachedLightingResult getCached(long chunkKey) {
        CachedLightingResult result = lightingCache.get(chunkKey);
        if (result != null && !result.isExpired()) {
            result.markAccessed();
            return result;
        }
        return null;
    }

    /**
     * Caches a lighting result.
     *
     * @param chunkKey Unique key for the chunk
     * @param result The lighting result to cache
     */
    public static void cache(long chunkKey, CachedLightingResult result) {
        // Evict if cache is too large
        if (lightingCache.size() >= MAX_CACHE_SIZE) {
            evictOldest();
        }
        lightingCache.put(chunkKey, result);
    }

    /**
     * Invalidates cached lighting for a chunk.
     *
     * @param chunkKey Unique key for the chunk
     */
    public static void invalidate(long chunkKey) {
        lightingCache.remove(chunkKey);
    }

    /**
     * Invalidates all cached lighting in a region.
     *
     * @param centerX Center X coordinate
     * @param centerZ Center Z coordinate
     * @param radius Radius in chunks
     */
    public static void invalidateRegion(int centerX, int centerZ, int radius) {
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                long key = chunkKey(x, z);
                lightingCache.remove(key);
            }
        }
    }

    /**
     * Creates a chunk key from coordinates.
     *
     * @param x Chunk X coordinate
     * @param z Chunk Z coordinate
     * @return Combined chunk key
     */
    public static long chunkKey(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }
    
    /**
     * Evict the oldest cache entries.
     */
    private static void evictOldest() {
        // Simple eviction: remove entries accessed longest ago
        long now = System.currentTimeMillis();
        lightingCache.entrySet().removeIf(entry -> 
            (now - entry.getValue().lastAccessed) > 60000 // 1 minute
        );
        
        // If still too large, remove 25%
        if (lightingCache.size() >= MAX_CACHE_SIZE) {
            int toRemove = MAX_CACHE_SIZE / 4;
            var iterator = lightingCache.entrySet().iterator();
            while (iterator.hasNext() && toRemove > 0) {
                iterator.next();
                iterator.remove();
                toRemove--;
            }
        }
    }
    
    /**
     * Clean expired cache entries.
     */
    private static void cleanCache() {
        long now = System.currentTimeMillis();
        lightingCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    // ===== Statistics =====

    /**
     * Gets total lighting calculations count.
     *
     * @return Total calculations
     */
    public static long getTotalCalculations() {
        return totalCalculations.get();
    }

    /**
     * Gets skipped propagations count.
     *
     * @return Total skipped propagations
     */
    public static long getSkippedPropagations() {
        return skippedPropagations.get();
    }

    /**
     * Gets skipped updates count.
     *
     * @return Total skipped updates
     */
    public static long getSkippedUpdates() {
        return skippedUpdates.get();
    }

    /**
     * Gets batched updates count.
     *
     * @return Total batched updates
     */
    public static long getBatchedUpdates() {
        return batchedUpdates.get();
    }

    /**
     * Gets current cache size.
     *
     * @return Number of cached entries
     */
    public static int getCacheSize() {
        return lightingCache.size();
    }

    /**
     * Gets skip ratio as a percentage.
     *
     * @return Skip ratio (0-100)
     */
    public static double getSkipRatio() {
        long total = getTotalCalculations();
        long skipped = getSkippedPropagations() + getSkippedUpdates();
        if (total == 0) return 0;
        return (skipped * 100.0) / total;
    }

    /**
     * Resets all statistics to zero.
     */
    public static void resetStats() {
        totalCalculations.set(0);
        skippedPropagations.set(0);
        skippedUpdates.set(0);
        batchedUpdates.set(0);
    }

    /**
     * Cached lighting result holder.
     */
    public static class CachedLightingResult {

        /** The lighting data bytes */
        public final byte[] lightData;

        /** Creation timestamp in milliseconds */
        public final long createdAt;

        /** Last access timestamp in milliseconds */
        public long lastAccessed;

        /** Expiration timestamp in milliseconds */
        public final long expiresAt;
        
        /** Default cache duration: 30 seconds */
        private static final long DEFAULT_TTL_MS = 30000;

        /**
         * Creates a cached lighting result with default TTL.
         *
         * @param lightData The lighting data bytes to cache
         */
        public CachedLightingResult(byte[] lightData) {
            this.lightData = lightData;
            this.createdAt = System.currentTimeMillis();
            this.lastAccessed = this.createdAt;
            this.expiresAt = this.createdAt + DEFAULT_TTL_MS;
        }

        /**
         * Creates a cached lighting result with custom TTL.
         *
         * @param lightData The lighting data bytes to cache
         * @param ttlMs Time-to-live in milliseconds
         */
        public CachedLightingResult(byte[] lightData, long ttlMs) {
            this.lightData = lightData;
            this.createdAt = System.currentTimeMillis();
            this.lastAccessed = this.createdAt;
            this.expiresAt = this.createdAt + ttlMs;
        }

        /**
         * Checks if this cached result has expired.
         *
         * @return true if expired, false otherwise
         */
        public boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }

        /**
         * Marks this result as accessed, updating the last access time.
         */
        public void markAccessed() {
            this.lastAccessed = System.currentTimeMillis();
        }
    }
}
