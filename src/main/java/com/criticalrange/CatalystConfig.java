package com.criticalrange;

/**
 * Central configuration for Catalyst optimizations.
 *
 * <p>Allows runtime toggling of all optimization features. All fields are volatile
 * to ensure thread-safe reads without explicit synchronization.</p>
 *
 * <h2>Available Optimizations:</h2>
 * <ul>
 *   <li><b>Entity Distance</b> - Configurable entity view distance multiplier</li>
 *   <li><b>Chunk Rate</b> - Configurable chunks per tick</li>
 *   <li><b>Pathfinding</b> - Configurable NPC pathfinding limits</li>
 *   <li><b>Chunk Generation</b> - Pool size and cache size configuration</li>
 *   <li><b>Lighting</b> - Early exit thresholds for light propagation</li>
 *   <li><b>Visual Effects</b> - Toggle particles and animations</li>
 * </ul>
 *
 * <p><b>For immediate effect:</b> A server restart is recommended after changing values.</p>
 */
public class CatalystConfig {

    private CatalystConfig() {
    }

    // ========== Hytale Default Constants ==========
    // These are the vanilla Hytale values that Catalyst can override

    /** Default blocks per chunk used in entity view distance calculation */
    public static final int DEFAULT_BLOCKS_PER_CHUNK = 32;

    /** Default chunks sent to player per tick */
    public static final int DEFAULT_CHUNKS_PER_TICK = 4;

    /** Default maximum path length for A* pathfinding */
    public static final int DEFAULT_MAX_PATH_LENGTH = 200;

    /** Default maximum nodes in A* open set */
    public static final int DEFAULT_OPEN_NODES_LIMIT = 80;

    /** Default maximum total visited nodes in A* */
    public static final int DEFAULT_TOTAL_NODES_LIMIT = 400;

    /** Default chunk generator cache size */
    public static final int DEFAULT_GENERATOR_CACHE_SIZE = 50000;

    /** Default cave generator cache size */
    public static final int DEFAULT_CAVE_CACHE_SIZE = 5000;

    /** Default unique prefab cache size */
    public static final int DEFAULT_PREFAB_CACHE_SIZE = 50;

    /** Number of Y levels per chunk section */
    public static final int BLOCKS_PER_SECTION = 32;

    /** Maximum world height in blocks */
    public static final int MAX_WORLD_HEIGHT = 320;

    /** Number of chunk sections per chunk column */
    public static final int SECTIONS_PER_CHUNK = MAX_WORLD_HEIGHT / BLOCKS_PER_SECTION;

    // ========== Entity Distance Configuration ==========

    /**
     * Enables entity tracker distance optimization.
     * When enabled, uses {@link #ENTITY_VIEW_MULTIPLIER} instead of default 32.
     */
    public static volatile boolean ENTITY_DISTANCE_ENABLED = false;

    /**
     * Multiplier for entity view distance calculation.
     * Original game uses 32 (blocks per chunk).
     * Lower values = less network traffic, shorter entity sync distance.
     */
    public static volatile int ENTITY_VIEW_MULTIPLIER = DEFAULT_BLOCKS_PER_CHUNK;

    // ========== Chunk Loading Configuration ==========

    /**
     * Enables configurable chunk loading rate.
     * When enabled, uses {@link #CHUNKS_PER_TICK} instead of default 4.
     */
    public static volatile boolean CHUNK_RATE_ENABLED = false;

    /**
     * Maximum chunks sent to a player per tick.
     * Hytale default is 4. Range: 1-16 recommended.
     */
    public static volatile int CHUNKS_PER_TICK = DEFAULT_CHUNKS_PER_TICK;

    // ========== NPC Pathfinding Configuration ==========

    /**
     * Enables configurable pathfinding limits.
     * When enabled, uses the configured limits instead of Hytale defaults.
     */
    public static volatile boolean PATHFINDING_ENABLED = false;

    /** Maximum path length in nodes. Hytale default: 200. */
    public static volatile int MAX_PATH_LENGTH = DEFAULT_MAX_PATH_LENGTH;

    /** Maximum nodes in the A* open set. Hytale default: 80. */
    public static volatile int OPEN_NODES_LIMIT = DEFAULT_OPEN_NODES_LIMIT;

    /** Maximum total visited nodes per pathfinding operation. Hytale default: 400. */
    public static volatile int TOTAL_NODES_LIMIT = DEFAULT_TOTAL_NODES_LIMIT;

    // ========== Chunk Generation Configuration ==========

    /**
     * Enables configurable chunk generation thread pool size.
     * When enabled, uses {@link #CHUNK_POOL_SIZE} instead of default calculation.
     */
    public static volatile boolean CHUNK_POOL_SIZE_ENABLED = false;
    
    /**
     * Chunk generation thread pool size.
     * Default: availableProcessors(). Higher = faster generation, more CPU usage.
     */
    public static volatile int CHUNK_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    /**
     * Enables configurable chunk cache sizes.
     * When enabled, uses configured cache sizes instead of Hytale defaults.
     */
    public static volatile boolean CHUNK_CACHE_SIZE_ENABLED = false;
    
    /** Generator cache size. Hytale default: 50000. */
    public static volatile int GENERATOR_CACHE_SIZE = DEFAULT_GENERATOR_CACHE_SIZE;
    
    /** Cave generator cache size. Hytale default: 5000. */
    public static volatile int CAVE_CACHE_SIZE = DEFAULT_CAVE_CACHE_SIZE;
    
    /** Unique prefab cache size. Hytale default: 50. */
    public static volatile int PREFAB_CACHE_SIZE = DEFAULT_PREFAB_CACHE_SIZE;

    /**
     * Enables chunk generation thread priority configuration.
     * When enabled, uses {@link #CHUNK_THREAD_PRIORITY} for worker threads.
     */
    public static volatile boolean CHUNK_THREAD_PRIORITY_ENABLED = false;
    
    /** Chunk generation thread priority (1-10). Default: 5 (NORM_PRIORITY). */
    public static volatile int CHUNK_THREAD_PRIORITY = Thread.NORM_PRIORITY;

    // ========== Lighting Configuration ==========

    /**
     * Enables skipping empty sections during lighting calculations.
     * 
     * <p>When enabled, sections that are solid air (all blocks are air) AND have no
     * calculated light data will be skipped during light propagation. This reduces
     * CPU usage in worlds with many empty air sections (e.g., above ground level).</p>
     * 
     * <p>This is safe because:</p>
     * <ul>
     *   <li>Air blocks don't emit light</li>
     *   <li>If no light data exists, there's nothing to propagate</li>
     * </ul>
     */
    public static volatile boolean LIGHT_SKIP_EMPTY_ENABLED = false;

    // ========== Visual Effects Configuration ==========

    /**
     * Enables server-side particle effects.
     * When disabled, particle spawn calls return early (no packets sent).
     */
    public static volatile boolean PARTICLES_ENABLED = true;
    
    /**
     * Enables server-side NPC animations.
     * When disabled, animation calls return early (no packets sent).
     */
    public static volatile boolean ANIMATIONS_ENABLED = true;

    // ========== Caching Optimizations ==========

    /**
     * Enables BlockChunk section reference caching.
     * 
     * <p>Caches the last accessed BlockSection in each BlockChunk. This optimizes
     * Y-iteration patterns common in lighting and collision detection where
     * consecutive block accesses often hit the same section (32 Y levels per section).</p>
     * 
     * <p>Expected impact: 5-15% faster block access in Y-iteration patterns.</p>
     */
    public static volatile boolean BLOCK_SECTION_CACHE_ENABLED = false;

    /**
     * Enables BlockType lookup result caching.
     * 
     * <p>Caches BlockType lookups by block ID. Since BlockType objects are immutable
     * once loaded, this avoids repeated array bounds checks and lookups for
     * frequently accessed block types during collision and lighting.</p>
     * 
     * <p>Expected impact: 10-20% faster BlockType lookups in hot paths.</p>
     */
    public static volatile boolean BLOCK_TYPE_CACHE_ENABLED = false;

    /**
     * Size of the BlockType lookup cache (power of 2 recommended).
     * Larger = fewer collisions but more memory.
     */
    public static volatile int BLOCK_TYPE_CACHE_SIZE = 256;

    /**
     * Enables LocalCachedChunkAccessor optimization.
     * 
     * <p>Caches the last 2 accessed chunks in LocalCachedChunkAccessor which is
     * used extensively during lighting calculations. Avoids repeated index
     * calculations for chunks accessed multiple times during propagation.</p>
     * 
     * <p>Expected impact: 5-10% faster chunk access during lighting calculations.</p>
     */
    public static volatile boolean LOCAL_CHUNK_CACHE_ENABLED = false;

    // ========== Advanced Optimizations ==========

    /**
     * Enables skipping idle block entities during ticking.
     * 
     * <p>Block entities that haven't changed state (hoppers with no items to move,
     * furnaces with no fuel, etc.) are marked as "sleeping" and skipped during
     * tick processing. They wake up when their state changes.</p>
     * 
     * <p>Expected impact: 20-40% reduction in block entity tick time for idle worlds.</p>
     */
    public static volatile boolean BLOCK_ENTITY_SLEEP_ENABLED = false;

    /**
     * Minimum ticks between block entity wake checks.
     * Lower = more responsive but more CPU. Higher = less CPU but slower reaction.
     */
    public static volatile int BLOCK_ENTITY_SLEEP_INTERVAL = 20;

    /**
     * Enables throttling of entity stat modifier recalculations.
     * 
     * <p>StatModifiersManager normally recalculates every tick when flagged.
     * This optimization adds a minimum interval between recalculations,
     * reducing CPU usage for entities with frequently changing stats.</p>
     * 
     * <p>Expected impact: 10-25% reduction in stat recalculation overhead.</p>
     */
    public static volatile boolean STAT_RECALC_THROTTLE_ENABLED = false;

    /**
     * Minimum ticks between stat modifier recalculations.
     * Default: 5 ticks (250ms at 20 TPS).
     */
    public static volatile int STAT_RECALC_INTERVAL = 5;

    /**
     * Enables batching of block update notifications.
     * 
     * <p>Instead of sending individual packets for each block change,
     * batches updates per chunk and sends them together at end of tick.
     * Reduces packet overhead for bulk block operations.</p>
     * 
     * <p>Expected impact: 15-30% reduction in network packets for bulk operations.</p>
     */
    public static volatile boolean BLOCK_UPDATE_BATCHING_ENABLED = false;

    /**
     * Maximum updates to batch before forcing a flush.
     * Prevents memory buildup during massive operations.
     */
    public static volatile int BLOCK_UPDATE_BATCH_SIZE = 64;

    /**
     * Enables flood fill depth limiting for spawn position selection.
     * 
     * <p>Limits how deep the flood fill algorithm searches when finding
     * spawn positions. Prevents lag spikes in complex terrain.</p>
     * 
     * <p>Expected impact: Prevents spawn-related lag spikes in complex worlds.</p>
     */
    public static volatile boolean FLOOD_FILL_LIMIT_ENABLED = false;

    /**
     * Maximum flood fill iterations before stopping.
     * Default: 5000. Lower = faster but may miss valid positions.
     */
    public static volatile int FLOOD_FILL_MAX_ITERATIONS = 5000;

    /**
     * Enables pathfinding memory pool optimization.
     * 
     * <p>Reuses AStarNode objects instead of allocating new ones for each
     * pathfinding operation. Reduces GC pressure during heavy NPC activity.</p>
     * 
     * <p>Expected impact: 15-25% reduction in pathfinding allocations.</p>
     */
    public static volatile boolean PATHFINDING_POOL_ENABLED = false;

    /**
     * Size of the pathfinding node pool per thread.
     * Larger = fewer allocations but more memory.
     */
    public static volatile int PATHFINDING_POOL_SIZE = 512;
}
