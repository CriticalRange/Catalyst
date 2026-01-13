package com.criticalrange.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Chunk caching system with async loading support.
 * Provides LRU caching and compression for inactive chunks.
 */
public class CatalystChunkCache {
    
    /** Maximum chunks to keep in active cache */
    private static final int MAX_ACTIVE_CHUNKS = 512;
    
    /** Maximum chunks to keep in compressed cache */
    private static final int MAX_COMPRESSED_CHUNKS = 2048;
    
    /** Thread pool for async chunk loading */
    private static final ExecutorService chunkLoaderPool = Executors.newFixedThreadPool(
        Math.max(2, Runtime.getRuntime().availableProcessors() / 2),
        r -> {
            Thread t = new Thread(r, "Catalyst-ChunkLoader");
            t.setDaemon(true);
            t.setPriority(Thread.NORM_PRIORITY - 1);
            return t;
        }
    );
    
    /** LRU cache for active chunks */
    private static final Map<Long, Object> activeChunks = new LinkedHashMap<Long, Object>(
        MAX_ACTIVE_CHUNKS, 0.75f, true
    ) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, Object> eldest) {
            if (size() > MAX_ACTIVE_CHUNKS) {
                // Move to compressed cache
                compressAndCache(eldest.getKey(), eldest.getValue());
                return true;
            }
            return false;
        }
    };
    
    /** Cache for compressed chunks */
    private static final Map<Long, byte[]> compressedChunks = new LinkedHashMap<Long, byte[]>(
        MAX_COMPRESSED_CHUNKS, 0.75f, true
    ) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, byte[]> eldest) {
            return size() > MAX_COMPRESSED_CHUNKS;
        }
    };
    
    /** Pending async loads */
    private static final Map<Long, CompletableFuture<Object>> pendingLoads = new ConcurrentHashMap<>();
    
    /** Metrics */
    private static final AtomicLong cacheHits = new AtomicLong(0);
    private static final AtomicLong cacheMisses = new AtomicLong(0);
    private static final AtomicLong asyncLoads = new AtomicLong(0);
    private static final AtomicLong compressionSaved = new AtomicLong(0);
    private static final AtomicLong chunksLoaded = new AtomicLong(0);
    private static final AtomicLong chunksUnloaded = new AtomicLong(0);
    
    /**
     * Called when a chunk load operation starts.
     */
    public static void onChunkLoad() {
        chunksLoaded.incrementAndGet();
    }
    
    /**
     * Called when a chunk unload operation starts.
     */
    public static void onChunkUnload() {
        chunksUnloaded.incrementAndGet();
    }
    
    /**
     * Try to get a chunk from cache.
     * @param chunkKey Chunk coordinate key
     * @return Cached chunk or null if not cached
     */
    public static Object getCached(long chunkKey) {
        // Check active cache
        synchronized (activeChunks) {
            Object chunk = activeChunks.get(chunkKey);
            if (chunk != null) {
                cacheHits.incrementAndGet();
                return chunk;
            }
        }
        
        // Check compressed cache
        synchronized (compressedChunks) {
            byte[] compressed = compressedChunks.get(chunkKey);
            if (compressed != null) {
                Object chunk = decompress(compressed);
                if (chunk != null) {
                    cacheHits.incrementAndGet();
                    // Move back to active cache
                    synchronized (activeChunks) {
                        activeChunks.put(chunkKey, chunk);
                    }
                    return chunk;
                }
            }
        }
        
        cacheMisses.incrementAndGet();
        return null;
    }
    
    /**
     * Cache a chunk.
     */
    public static void cache(long chunkKey, Object chunk) {
        synchronized (activeChunks) {
            activeChunks.put(chunkKey, chunk);
        }
    }
    
    /**
     * Load a chunk asynchronously.
     */
    public static CompletableFuture<Object> loadAsync(long chunkKey, Callable<Object> loader) {
        // Check if already loading
        CompletableFuture<Object> pending = pendingLoads.get(chunkKey);
        if (pending != null) {
            return pending;
        }
        
        asyncLoads.incrementAndGet();
        
        CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> {
            try {
                Object chunk = loader.call();
                cache(chunkKey, chunk);
                return chunk;
            } catch (Exception e) {
                throw new CompletionException(e);
            } finally {
                pendingLoads.remove(chunkKey);
            }
        }, chunkLoaderPool);
        
        pendingLoads.put(chunkKey, future);
        return future;
    }
    
    /**
     * Compress and cache a chunk.
     */
    private static void compressAndCache(long chunkKey, Object chunk) {
        try {
            byte[] compressed = compress(chunk);
            if (compressed != null) {
                synchronized (compressedChunks) {
                    compressedChunks.put(chunkKey, compressed);
                }
                // Rough estimate of memory saved
                compressionSaved.addAndGet(estimateSize(chunk) - compressed.length);
            }
        } catch (Exception e) {
            // Compression failed, chunk will just be evicted
        }
    }
    
    /**
     * Compress a chunk (placeholder - implement actual compression).
     */
    private static byte[] compress(Object chunk) {
        // TODO: Implement actual chunk compression
        // For now, return null to skip compression
        return null;
    }
    
    /**
     * Decompress a chunk (placeholder - implement actual decompression).
     */
    private static Object decompress(byte[] compressed) {
        // TODO: Implement actual chunk decompression
        return null;
    }
    
    /**
     * Estimate memory size of an object.
     */
    private static long estimateSize(Object obj) {
        // Rough estimate: 64KB per chunk
        return 65536;
    }
    
    /**
     * Create chunk key from coordinates.
     */
    public static long chunkKey(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }
    
    // Metrics getters
    
    public static long getCacheHits() {
        return cacheHits.get();
    }
    
    public static long getCacheMisses() {
        return cacheMisses.get();
    }
    
    public static double getHitRatio() {
        long hits = cacheHits.get();
        long total = hits + cacheMisses.get();
        return total == 0 ? 0 : (hits * 100.0) / total;
    }
    
    public static long getAsyncLoads() {
        return asyncLoads.get();
    }
    
    public static long getCompressionSaved() {
        return compressionSaved.get();
    }
    
    public static long getChunksLoaded() {
        return chunksLoaded.get();
    }
    
    public static long getChunksUnloaded() {
        return chunksUnloaded.get();
    }
    
    public static int getActiveChunkCount() {
        synchronized (activeChunks) {
            return activeChunks.size();
        }
    }
    
    public static int getCompressedChunkCount() {
        synchronized (compressedChunks) {
            return compressedChunks.size();
        }
    }
    
    /**
     * Clear all caches.
     */
    public static void clear() {
        synchronized (activeChunks) {
            activeChunks.clear();
        }
        synchronized (compressedChunks) {
            compressedChunks.clear();
        }
        pendingLoads.clear();
    }
    
    /**
     * Shutdown the chunk loader pool.
     */
    public static void shutdown() {
        chunkLoaderPool.shutdown();
        try {
            if (!chunkLoaderPool.awaitTermination(5, TimeUnit.SECONDS)) {
                chunkLoaderPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            chunkLoaderPool.shutdownNow();
        }
    }
}
