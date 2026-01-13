package com.criticalrange.util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Spatial hash index for O(log n) entity lookups.
 * Replaces O(n) linear searches in entity tracking.
 */
public class CatalystSpatialIndex {
    
    /** Chunk size for spatial partitioning (in blocks) */
    private static final int CHUNK_SIZE = 16;
    
    /** Entity storage by chunk coordinate */
    private static final Map<Long, Set<Object>> chunkEntities = new ConcurrentHashMap<>();
    
    /** Entity to chunk mapping for fast updates */
    private static final Map<Object, Long> entityChunks = new ConcurrentHashMap<>();
    
    /** Update frequency control */
    private static final AtomicLong updateCounter = new AtomicLong(0);
    private static final int UPDATE_BATCH_SIZE = 4; // Update every N ticks
    
    /** Metrics */
    private static final AtomicLong searchCount = new AtomicLong(0);
    private static final AtomicLong searchesOptimized = new AtomicLong(0);
    private static final AtomicLong updatesSkipped = new AtomicLong(0);
    
    /**
     * Add an entity to the spatial index.
     */
    public static void addEntity(Object entity, double x, double y, double z) {
        long chunkKey = getChunkKey(x, z);
        
        // Remove from old chunk if entity moved
        Long oldChunk = entityChunks.get(entity);
        if (oldChunk != null && oldChunk != chunkKey) {
            Set<Object> oldChunkSet = chunkEntities.get(oldChunk);
            if (oldChunkSet != null) {
                oldChunkSet.remove(entity);
            }
        }
        
        // Add to new chunk
        chunkEntities.computeIfAbsent(chunkKey, k -> ConcurrentHashMap.newKeySet()).add(entity);
        entityChunks.put(entity, chunkKey);
    }
    
    /**
     * Remove an entity from the spatial index.
     */
    public static void removeEntity(Object entity) {
        Long chunkKey = entityChunks.remove(entity);
        if (chunkKey != null) {
            Set<Object> chunkSet = chunkEntities.get(chunkKey);
            if (chunkSet != null) {
                chunkSet.remove(entity);
            }
        }
    }
    
    /**
     * Get entities near a position.
     * O(1) for small radius, O(k) where k = number of nearby chunks for larger radius.
     */
    public static Set<Object> getNearbyEntities(double x, double y, double z, double radius) {
        searchesOptimized.incrementAndGet();
        
        Set<Object> result = new HashSet<>();
        
        int chunkRadius = (int) Math.ceil(radius / CHUNK_SIZE);
        int centerChunkX = (int) Math.floor(x / CHUNK_SIZE);
        int centerChunkZ = (int) Math.floor(z / CHUNK_SIZE);
        
        for (int cx = -chunkRadius; cx <= chunkRadius; cx++) {
            for (int cz = -chunkRadius; cz <= chunkRadius; cz++) {
                long chunkKey = chunkKey(centerChunkX + cx, centerChunkZ + cz);
                Set<Object> chunkSet = chunkEntities.get(chunkKey);
                if (chunkSet != null) {
                    result.addAll(chunkSet);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Check if entity tracker should update this tick.
     * Uses batching to reduce update frequency.
     */
    public static boolean shouldUpdateTracker() {
        long current = updateCounter.incrementAndGet();
        
        if (current % UPDATE_BATCH_SIZE == 0) {
            return true;
        } else {
            updatesSkipped.incrementAndGet();
            return false;
        }
    }
    
    /**
     * Called when an entity search is performed.
     * Used for metrics tracking.
     */
    public static void onEntitySearch() {
        searchCount.incrementAndGet();
    }
    
    /**
     * Get chunk key from world coordinates.
     */
    private static long getChunkKey(double x, double z) {
        int chunkX = (int) Math.floor(x / CHUNK_SIZE);
        int chunkZ = (int) Math.floor(z / CHUNK_SIZE);
        return chunkKey(chunkX, chunkZ);
    }
    
    /**
     * Create a unique key from chunk coordinates.
     */
    private static long chunkKey(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }
    
    /**
     * Get total search count.
     */
    public static long getSearchCount() {
        return searchCount.get();
    }
    
    /**
     * Get count of searches that used spatial optimization.
     */
    public static long getSearchesOptimized() {
        return searchesOptimized.get();
    }
    
    /**
     * Get count of tracker updates that were skipped.
     */
    public static long getUpdatesSkipped() {
        return updatesSkipped.get();
    }
    
    /**
     * Clear the spatial index.
     */
    public static void clear() {
        chunkEntities.clear();
        entityChunks.clear();
    }
    
    /**
     * Reset metrics.
     */
    public static void resetMetrics() {
        searchCount.set(0);
        searchesOptimized.set(0);
        updatesSkipped.set(0);
    }
}
