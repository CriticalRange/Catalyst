package com.criticalrange;

/**
 * Central configuration for Catalyst optimizations.
 *
 * <p>Allows runtime toggling of all optimization features. All fields are volatile
 * to ensure thread-safe reads without explicit synchronization.</p>
 *
 * <h2>Runtime Configuration:</h2>
 * <ul>
 *   <li>{@link #ENTITY_DISTANCE_ENABLED} - Configurable entity view distance multiplier</li>
 *   <li>{@link #CHUNK_RATE_ENABLED} - Configurable chunks per tick</li>
 *   <li>{@link #PATHFINDING_ENABLED} - Configurable NPC pathfinding limits</li>
 * </ul>
 *
 * <h2>Configuration Behavior:</h2>
 * <p>All configuration values are read from this class at class load time of the target
 * Hytale classes. The injected fields in target classes are initialized from these values
 * in their static initializers ({@code <clinit>}).</p>
 *
 * <p><b>Runtime Changes:</b></p>
 * <ul>
 *   <li><b>ChunkRate:</b> Changes affect new player connections. Existing players keep their
 *       current value unless manually updated via {@code ChunkTracker.setMaxChunksPerTick()}.</li>
 *   <li><b>EntityDistance:</b> Changes affect new EntityViewer calculations. Existing players
 *       may need to rejoin or have their view radius recalculated.</li>
 * </ul>
 *
 * <p><b>For immediate effect on all players:</b> A server restart is recommended after
 * changing configuration values.</p>
 */
public class CatalystConfig {

    /**
     * Private constructor to prevent instantiation.
     */
    private CatalystConfig() {
    }

    // ========== Runtime Optimizations ==========

    /**
     * Enables entity tracker distance optimization.
     *
     * <p>When enabled, the entity view distance multiplier can be configured.
     * The game calculates entity view distance as: viewRadius * multiplier (default 32).</p>
     *
     * <p>Lower multiplier = shorter entity view distance = less network traffic.</p>
     * <p>Higher multiplier = longer entity view distance = more entities visible.</p>
     *
     * <p><b>Note:</b> This affects how far away entities are synced to players.
     * Set lower for survival servers, higher for PvP servers.</p>
     */
    public static volatile boolean ENTITY_DISTANCE_ENABLED = false;  // Disabled by default

    /**
     * Multiplier for entity view distance calculation.
     * Original game uses 32 (blocks per chunk).
     *
     * <p>Lower values reduce entity sync distance:</p>
     * <ul>
     *   <li>32 = default (full chunk distance)</li>
     *   <li>16 = half distance (50% reduction in synced entities)</li>
     *   <li>24 = 75% of default</li>
     * </ul>
     *
     * <p>Default: 32 (same as vanilla)</p>
     */
    public static volatile int ENTITY_VIEW_MULTIPLIER = 32;

    /**
     * Enables configurable chunk loading rate.
     *
     * <p>Controls how many chunks are sent to players per tick.
     * Higher = faster chunk loading, potentially lower TPS.
     * Lower = slower chunk loading, smoother TPS.</p>
     */
    public static volatile boolean CHUNK_RATE_ENABLED = false;  // Disabled by default

    /**
     * Maximum chunks sent to a player per tick.
     * Hytale default is 4.
     *
     * <p>Recommended values:</p>
     * <ul>
     *   <li>2-3 = Lower-end servers, many players</li>
     *   <li>4 = Default, balanced</li>
     *   <li>6-8 = High-end servers, fast chunk loading</li>
     * </ul>
     *
     * <p>Default: 4 (same as vanilla)</p>
     */
    public static volatile int CHUNKS_PER_TICK = 4;

    // ========== NPC Pathfinding Configuration ==========

    /**
     * Enables configurable pathfinding limits.
     *
     * <p>When enabled, NPC pathfinding will use the configured limits instead of defaults.
     * This allows tuning pathfinding performance for your server's needs.</p>
     */
    public static volatile boolean PATHFINDING_ENABLED = false;  // Disabled by default

    /**
     * Maximum path length in nodes.
     * Hytale default is 200.
     *
     * <p>Controls how long of a path NPCs can calculate:</p>
     * <ul>
     *   <li>100 = Shorter paths, faster calculation, NPCs may get stuck more</li>
     *   <li>200 = Default, balanced</li>
     *   <li>300+ = Longer paths, more CPU per NPC</li>
     * </ul>
     *
     * <p>Default: 200 (same as vanilla)</p>
     */
    public static volatile int MAX_PATH_LENGTH = 200;

    /**
     * Maximum nodes in the A* open set.
     * Hytale default is 80.
     *
     * <p>Controls how many candidate nodes are considered at once:</p>
     * <ul>
     *   <li>40 = Faster but may miss optimal paths</li>
     *   <li>80 = Default, balanced</li>
     *   <li>120+ = Better paths, more memory and CPU</li>
     * </ul>
     *
     * <p>Default: 80 (same as vanilla)</p>
     */
    public static volatile int OPEN_NODES_LIMIT = 80;

    /**
     * Maximum total visited nodes per pathfinding operation.
     * Hytale default is 400.
     *
     * <p>Controls the hard limit on pathfinding work:</p>
     * <ul>
     *   <li>200 = Strict limit, quick failures for complex paths</li>
     *   <li>400 = Default, balanced</li>
     *   <li>600+ = More thorough search, higher CPU cost</li>
     * </ul>
     *
     * <p>Default: 400 (same as vanilla)</p>
     */
    public static volatile int TOTAL_NODES_LIMIT = 400;

    // ========== Chunk Generation Configuration ==========

    /** Enables configurable chunk generation thread pool size. */
    public static volatile boolean CHUNK_POOL_SIZE_ENABLED = false;
    
    /** Auto-detect optimal pool size based on CPU cores. */
    public static volatile boolean CHUNK_POOL_SIZE_AUTO = true;
    
    /** Manual chunk generation thread pool size. */
    public static volatile int CHUNK_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    /** Enables configurable chunk cache sizes. */
    public static volatile boolean CHUNK_CACHE_SIZE_ENABLED = false;
    
    /** Auto-detect optimal cache sizes. */
    public static volatile boolean CHUNK_CACHE_SIZE_AUTO = true;
    
    /** Generator cache size. */
    public static volatile int GENERATOR_CACHE_SIZE = 1024;
    
    /** Cave cache size. */
    public static volatile int CAVE_CACHE_SIZE = 512;
    
    /** Prefab cache size. */
    public static volatile int PREFAB_CACHE_SIZE = 256;

    /** Enables configurable biome interpolation radius. */
    public static volatile boolean BIOME_INTERPOLATION_ENABLED = false;
    
    /** Biome interpolation radius (default 4). */
    public static volatile int BIOME_INTERPOLATION_RADIUS = 4;

    /** Enables configurable tint interpolation radius. */
    public static volatile boolean TINT_INTERPOLATION_ENABLED = false;
    
    /** Tint interpolation radius (default 4). */
    public static volatile int TINT_INTERPOLATION_RADIUS = 4;

    /** Enables height search optimization. */
    public static volatile boolean HEIGHT_SEARCH_ENABLED = false;

    // ========== Lighting Configuration ==========

    /** Enables lighting batch processing. */
    public static volatile boolean LIGHTING_BATCH_ENABLED = false;
    
    /** Number of lighting sections to process per batch. */
    public static volatile int LIGHTING_BATCH_SIZE = 8;

    /** Enables lighting distance limit. */
    public static volatile boolean LIGHTING_DISTANCE_ENABLED = false;
    
    /** Maximum chunk distance for lighting calculations. */
    public static volatile int LIGHTING_MAX_DISTANCE = 8;

    /** Enables chunk thread priority configuration. */
    public static volatile boolean CHUNK_THREAD_PRIORITY_ENABLED = false;
    
    /** Chunk generation thread priority (1-10, default 5). */
    public static volatile int CHUNK_THREAD_PRIORITY = 5;

    // ========== Visual Effects Configuration ==========

    /** Enables particle effects (server-side). */
    public static volatile boolean PARTICLES_ENABLED = true;
    
    /** Enables NPC animations (server-side). */
    public static volatile boolean ANIMATIONS_ENABLED = true;

    // ========== Advanced Lighting Optimizations ==========

    /** Enables light propagation optimization (packed operations). */
    public static volatile boolean LIGHT_PROP_OPT_ENABLED = true;

    /** Enables opacity lookup cache for faster block type checks. */
    public static volatile boolean OPACITY_CACHE_ENABLED = true;

    /** Enables flat cache for light data (trades memory for speed). */
    public static volatile boolean LIGHT_FLAT_CACHE_ENABLED = true;

    /** Enables light queue processing optimization. */
    public static volatile boolean LIGHT_QUEUE_OPT_ENABLED = true;

    /** Enables packed light operations (SIMD-style). */
    public static volatile boolean PACKED_LIGHT_OPS_ENABLED = true;

    /** Enables skipping empty sections during lighting. */
    public static volatile boolean SKIP_EMPTY_SECTIONS = true;
}
