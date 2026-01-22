package com.criticalrange;

import com.criticalrange.util.VisualEffectsToggle;
import java.nio.file.Path;

/**
 * Early initialization for Catalyst when running from earlyplugins/ only.
 * 
 * <p>This class handles config loading and field syncing without requiring
 * the JavaPlugin lifecycle.</p>
 */
public class CatalystEarlyInit {
    
    private static boolean initialized = false;
    private static CatalystConfigFile configFile;
    
    /**
     * Initialize Catalyst from early plugin context.
     * Safe to call multiple times - will only initialize once.
     */
    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        
        System.out.println("[Catalyst] Early initialization starting...");
        
        // Load configuration from file
        Path dataDir = Path.of("mods/com.criticalrange_Catalyst");
        configFile = new CatalystConfigFile(dataDir);
        configFile.load();
        
        // Sync config values to injected fields
        syncConfigToInjectedFields();
        
        System.out.println("[Catalyst] Early initialization complete.");
    }
    
    /**
     * Save configuration to file.
     */
    public static void saveConfig() {
        if (configFile != null) {
            configFile.save();
        }
    }
    
    /**
     * Syncs CatalystConfig values to the injected fields in transformed Hytale classes.
     */
    private static void syncConfigToInjectedFields() {
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

        // Chunk generation fields
        setStaticField("com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator",
            "$catalystPoolSizeEnabled", CatalystConfig.CHUNK_POOL_SIZE_ENABLED);
        setStaticField("com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator",
            "$catalystPoolSizeAuto", CatalystConfig.CHUNK_POOL_SIZE_AUTO);
        setStaticField("com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator",
            "$catalystPoolSize", CatalystConfig.CHUNK_POOL_SIZE);
        setStaticField("com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator",
            "$catalystCacheSizeEnabled", CatalystConfig.CHUNK_CACHE_SIZE_ENABLED);
        setStaticField("com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator",
            "$catalystCacheSizeAuto", CatalystConfig.CHUNK_CACHE_SIZE_AUTO);
        setStaticField("com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator",
            "$catalystGeneratorCacheSize", CatalystConfig.GENERATOR_CACHE_SIZE);
        setStaticField("com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator",
            "$catalystCaveCacheSize", CatalystConfig.CAVE_CACHE_SIZE);
        setStaticField("com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator",
            "$catalystPrefabCacheSize", CatalystConfig.PREFAB_CACHE_SIZE);
        setStaticField("com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator",
            "$catalystTintInterpEnabled", CatalystConfig.TINT_INTERPOLATION_ENABLED);
        setStaticField("com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator",
            "$catalystTintRadius", CatalystConfig.TINT_INTERPOLATION_RADIUS);
        setStaticField("com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator",
            "$catalystHeightSearchEnabled", CatalystConfig.HEIGHT_SEARCH_ENABLED);

        // Biome interpolation fields
        setStaticField("com.hypixel.hytale.server.worldgen.chunk.HeightThresholdInterpolator",
            "$catalystBiomeInterpEnabled", CatalystConfig.BIOME_INTERPOLATION_ENABLED);
        setStaticField("com.hypixel.hytale.server.worldgen.chunk.HeightThresholdInterpolator",
            "$catalystBiomeRadius", CatalystConfig.BIOME_INTERPOLATION_RADIUS);

        // Lighting optimization fields
        setStaticField("com.hypixel.hytale.server.core.universe.world.lighting.ChunkLightingManager",
            "$catalystLightingBatchEnabled", CatalystConfig.LIGHTING_BATCH_ENABLED);
        setStaticField("com.hypixel.hytale.server.core.universe.world.lighting.ChunkLightingManager",
            "$catalystLightingBatchSize", CatalystConfig.LIGHTING_BATCH_SIZE);
        setStaticField("com.hypixel.hytale.server.core.universe.world.lighting.FloodLightCalculation",
            "$catalystLightingDistanceEnabled", CatalystConfig.LIGHTING_DISTANCE_ENABLED);
        setStaticField("com.hypixel.hytale.server.core.universe.world.lighting.FloodLightCalculation",
            "$catalystLightingMaxDistance", CatalystConfig.LIGHTING_MAX_DISTANCE);

        // Chunk thread priority fields
        setStaticField("com.hypixel.hytale.server.worldgen.chunk.ChunkWorkerThreadFactory",
            "$catalystChunkThreadPriorityEnabled", CatalystConfig.CHUNK_THREAD_PRIORITY_ENABLED);
        setStaticField("com.hypixel.hytale.server.worldgen.chunk.ChunkWorkerThreadFactory",
            "$catalystChunkThreadPriority", CatalystConfig.CHUNK_THREAD_PRIORITY);

        // Visual effects - use VisualEffectsToggle directly
        VisualEffectsToggle.particlesEnabled = CatalystConfig.PARTICLES_ENABLED;
        VisualEffectsToggle.animationsEnabled = CatalystConfig.ANIMATIONS_ENABLED;
    }

    private static void setStaticField(String className, String fieldName, boolean value) {
        try {
            Class<?> clazz = Class.forName(className);
            java.lang.reflect.Field field = clazz.getField(fieldName);
            field.setBoolean(null, value);
        } catch (ClassNotFoundException e) {
            // Class not loaded yet - this is fine, field will use default
        } catch (NoSuchFieldException e) {
            // Field doesn't exist - transformer may not have run
        } catch (Exception e) {
            System.err.println("[Catalyst] Failed to set " + fieldName + ": " + e.getMessage());
        }
    }

    private static void setStaticField(String className, String fieldName, int value) {
        try {
            Class<?> clazz = Class.forName(className);
            java.lang.reflect.Field field = clazz.getField(fieldName);
            field.setInt(null, value);
        } catch (ClassNotFoundException e) {
            // Class not loaded yet - this is fine, field will use default
        } catch (NoSuchFieldException e) {
            // Field doesn't exist - transformer may not have run
        } catch (Exception e) {
            System.err.println("[Catalyst] Failed to set " + fieldName + ": " + e.getMessage());
        }
    }
}
