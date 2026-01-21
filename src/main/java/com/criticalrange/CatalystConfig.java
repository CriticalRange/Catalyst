package com.criticalrange;

/**
 * Central configuration for Catalyst optimizations.
 *
 * <p>Allows runtime toggling of all optimization features. All fields are volatile
 * to ensure thread-safe reads without explicit synchronization.</p>
 *
 * <h2>Chunk Loading Optimizations (Lazy Loading):</h2>
 * <ul>
 *   <li>{@link #LAZY_BLOCK_ENTITIES_ENABLED} - Defers block entity creation until accessed</li>
 *   <li>{@link #LAZY_BLOCK_TICK_ENABLED} - Defers ticking block discovery</li>
 *   <li>{@link #LAZY_FLUID_ENABLED} - Defers fluid pre-processing simulation</li>
 * </ul>
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
 *   <li><b>Lazy optimizations:</b> Changes take effect immediately for newly loaded chunks.
 *       Already-loaded chunks are not affected.</li>
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

    // ========== Chunk Loading Optimizations (Lazy Loading) ==========

    /**
     * Enables lazy block entity initialization.
     *
     * <p>When enabled, block entities (chests, furnaces, signs, etc.) are NOT created
     * during chunk pre-load. Instead, they are created on-demand when first accessed.</p>
     *
     * <p>This significantly reduces chunk generation time by avoiding the 327,680 block
     * iteration per chunk. Block entities will still be created when:</p>
     * <ul>
     *   <li>A player interacts with the block</li>
     *   <li>A neighboring block updates</li>
     *   <li>The block is explicitly queried</li>
     * </ul>
     *
     * <p><b>Note:</b> This is a safe optimization - blocks will still function correctly.
     * The only difference is when the block entity is initialized.</p>
     */
    public static volatile boolean LAZY_BLOCK_ENTITIES_ENABLED = false;  // Disabled by default for safety

    /**
     * Enables lazy block tick discovery.
     *
     * <p>When enabled, ticking blocks (crops, saplings, etc.) are NOT discovered
     * during chunk pre-load. Instead, they must be discovered later or manually.</p>
     *
     * <p>This significantly reduces chunk generation time by avoiding the 32,768 block
     * iteration per section.</p>
     *
     * <p><b>Warning:</b> This may cause crops, saplings, and other ticking blocks to not
     * grow automatically in newly generated chunks. Use with caution on farming servers.</p>
     */
    public static volatile boolean LAZY_BLOCK_TICK_ENABLED = false;  // Disabled by default for safety

    /**
     * Enables lazy fluid pre-processing.
     *
     * <p>When enabled, fluid simulation (water, lava, etc.) is NOT run during chunk pre-load.
     * Instead, fluids will process normally during regular ticks.</p>
     *
     * <p>This MASSIVELY reduces chunk generation time by avoiding:</p>
     * <ul>
     *   <li>32,768 block iteration per section</li>
     *   <li>Up to 100 simulation ticks per chunk</li>
     *   <li>Complex fluid spread calculations</li>
     * </ul>
     *
     * <p><b>Note:</b> Fluids will still update normally during gameplay. The only difference
     * is they won't be pre-simulated during chunk generation, meaning water/lava may take
     * a moment to start flowing after loading.</p>
     */
    public static volatile boolean LAZY_FLUID_ENABLED = false;  // Disabled by default for safety

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
}
