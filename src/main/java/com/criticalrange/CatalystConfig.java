package com.criticalrange;

/**
 * Central configuration for Catalyst optimizations.
 *
 * <p>Allows runtime toggling of all optimization features. All fields are volatile
 * to ensure thread-safe reads without explicit synchronization.</p>
 *
 * <p>Chunk Loading Optimizations (Lazy Loading):</p>
 * <ul>
 *   <li>{@link #LAZY_BLOCK_ENTITIES_ENABLED} - Defers block entity creation until accessed</li>
 *   <li>{@link #LAZY_BLOCK_TICK_ENABLED} - Defers ticking block discovery</li>
 *   <li>{@link #LAZY_FLUID_ENABLED} - Defers fluid pre-processing simulation</li>
 * </ul>
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
}
