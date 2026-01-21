package com.criticalrange;

import com.criticalrange.transformer.CatalystTransformerManager;
import com.criticalrange.util.CatalystMetrics;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;

/**
 * Catalyst - Hytale Performance Optimization Mod
 *
 * <p>Optimizes server performance through bytecode manipulation.</p>
 *
 * <p>Current optimizations:</p>
 * <ul>
 *   <li>Lazy block entity initialization - defers creation until needed</li>
 *   <li>Lazy block tick discovery - defers ticking block detection</li>
 *   <li>Lazy fluid pre-processing - defers fluid simulation during chunk load</li>
 *   <li>Entity view distance - configurable entity sync distance multiplier</li>
 *   <li>Chunk rate - configurable chunks per tick</li>
 *   <li>Pathfinding limits - configurable A* pathfinding parameters</li>
 * </ul>
 *
 * @author CriticalRange
 */
public class Catalyst extends JavaPlugin {

    private static Catalyst instance;
    private CatalystTransformerManager transformerManager;
    private CatalystConfigFile configFile;

    public Catalyst(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    public static Catalyst getInstance() {
        return instance;
    }

    /**
     * Gets the version from the plugin manifest (set by gradle.properties).
     *
     * @return The version string, or "unknown" if not available
     */
    public String getVersion() {
        try {
            return getManifest().getVersion().toString();
        } catch (Exception e) {
            return "unknown";
        }
    }

    @Override
    protected void setup() {
        // Load configuration from file
        configFile = new CatalystConfigFile(getDataDirectory());
        configFile.load();

        // Sync config values to injected fields in transformed classes
        syncConfigToInjectedFields();

        transformerManager = new CatalystTransformerManager();

        try {
            getCommandRegistry().registerCommand(new com.criticalrange.command.CatalystCommand());
        } catch (Throwable t) {
            log("Failed to register command: " + t.getMessage());
        }

        log("Catalyst " + getVersion() + " initialized.");
    }

    @Override
    protected void start() {
        super.start();
        logConfig();
    }

    @Override
    protected void shutdown() {
        log("Catalyst shutting down...");

        // Save configuration to file
        if (configFile != null) {
            configFile.save();
        }

        log("\n" + CatalystMetrics.generateReport());
        if (transformerManager != null) {
            transformerManager.logMetrics();
        }
        super.shutdown();
    }

    private void logConfig() {
        log("Configuration:");
        log("  Lazy Loading:");
        log("    - Block Entities: " + s(CatalystConfig.LAZY_BLOCK_ENTITIES_ENABLED));
        log("    - Block Tick: " + s(CatalystConfig.LAZY_BLOCK_TICK_ENABLED));
        log("    - Fluid: " + s(CatalystConfig.LAZY_FLUID_ENABLED));
        log("  Runtime Optimizations:");
        log("    - Entity Distance: " + s(CatalystConfig.ENTITY_DISTANCE_ENABLED) + 
            " (multiplier: " + CatalystConfig.ENTITY_VIEW_MULTIPLIER + ")");
        log("    - Chunk Rate: " + s(CatalystConfig.CHUNK_RATE_ENABLED) + 
            " (chunks/tick: " + CatalystConfig.CHUNKS_PER_TICK + ")");
        log("  Pathfinding:");
        log("    - Custom Limits: " + s(CatalystConfig.PATHFINDING_ENABLED));
        log("    - Max Path Length: " + CatalystConfig.MAX_PATH_LENGTH);
        log("    - Open Nodes: " + CatalystConfig.OPEN_NODES_LIMIT);
        log("    - Total Nodes: " + CatalystConfig.TOTAL_NODES_LIMIT);
    }

    private String s(boolean enabled) {
        return enabled ? "ON" : "OFF";
    }

    private void log(String message) {
        System.out.println("[Catalyst] " + message);
    }

    /**
     * Syncs CatalystConfig values to the injected fields in transformed Hytale classes.
     * This must be called after loading config but before the server starts using these values.
     */
    private void syncConfigToInjectedFields() {
        // Lazy loading fields
        setStaticField("com.hypixel.hytale.server.core.modules.block.BlockModule",
            "$catalystLazyBlockEntities", CatalystConfig.LAZY_BLOCK_ENTITIES_ENABLED);
        setStaticField("com.hypixel.hytale.builtin.blocktick.BlockTickPlugin",
            "$catalystLazyBlockTick", CatalystConfig.LAZY_BLOCK_TICK_ENABLED);
        setStaticField("com.hypixel.hytale.builtin.fluid.FluidPlugin",
            "$catalystLazyFluid", CatalystConfig.LAZY_FLUID_ENABLED);

        // Entity distance fields
        setStaticField("com.hypixel.hytale.server.core.universe.Universe",
            "$catalystEntityDistEnabled", CatalystConfig.ENTITY_DISTANCE_ENABLED);
        setStaticField("com.hypixel.hytale.server.core.universe.Universe",
            "$catalystEntityViewMultiplier", CatalystConfig.ENTITY_VIEW_MULTIPLIER);

        // Chunk rate fields
        setStaticField("com.hypixel.hytale.server.core.modules.entity.player.ChunkTracker",
            "$catalystChunkRateEnabled", CatalystConfig.CHUNK_RATE_ENABLED);
        setStaticField("com.hypixel.hytale.server.core.modules.entity.player.ChunkTracker",
            "$catalystChunksPerTick", CatalystConfig.CHUNKS_PER_TICK);

        // Pathfinding fields
        setStaticField("com.hypixel.hytale.server.npc.navigation.AStarBase",
            "$catalystPathfindingEnabled", CatalystConfig.PATHFINDING_ENABLED);
        setStaticField("com.hypixel.hytale.server.npc.navigation.AStarBase",
            "$catalystMaxPathLength", CatalystConfig.MAX_PATH_LENGTH);
        setStaticField("com.hypixel.hytale.server.npc.navigation.AStarBase",
            "$catalystOpenNodesLimit", CatalystConfig.OPEN_NODES_LIMIT);
        setStaticField("com.hypixel.hytale.server.npc.navigation.AStarBase",
            "$catalystTotalNodesLimit", CatalystConfig.TOTAL_NODES_LIMIT);
    }

    private void setStaticField(String className, String fieldName, boolean value) {
        try {
            Class<?> clazz = Class.forName(className);
            java.lang.reflect.Field field = clazz.getField(fieldName);
            field.setBoolean(null, value);
        } catch (ClassNotFoundException e) {
            // Class not loaded yet - this is fine, field will use default
            log("  [Sync] " + className.substring(className.lastIndexOf('.') + 1) + " not loaded yet");
        } catch (NoSuchFieldException e) {
            // Field doesn't exist - transformer may not have run
            log("  [Sync] Field " + fieldName + " not found in " + className);
        } catch (Exception e) {
            log("  [Sync] Failed to set " + fieldName + ": " + e.getMessage());
        }
    }

    private void setStaticField(String className, String fieldName, int value) {
        try {
            Class<?> clazz = Class.forName(className);
            java.lang.reflect.Field field = clazz.getField(fieldName);
            field.setInt(null, value);
        } catch (ClassNotFoundException e) {
            // Class not loaded yet - this is fine, field will use default
            log("  [Sync] " + className.substring(className.lastIndexOf('.') + 1) + " not loaded yet");
        } catch (NoSuchFieldException e) {
            // Field doesn't exist - transformer may not have run
            log("  [Sync] Field " + fieldName + " not found in " + className);
        } catch (Exception e) {
            log("  [Sync] Failed to set " + fieldName + ": " + e.getMessage());
        }
    }

    public CatalystTransformerManager getTransformerManager() {
        return transformerManager;
    }

    /**
     * Saves the current configuration to the config file.
     * Call this after making changes to {@link CatalystConfig} to persist them.
     */
    public void saveConfig() {
        if (configFile != null) {
            configFile.save();
        }
    }

    /**
     * Reloads configuration from the config file.
     *
     * @return true if config was reloaded successfully
     */
    public boolean reloadConfig() {
        if (configFile != null) {
            return configFile.load();
        }
        return false;
    }
}
