package com.criticalrange;

import java.nio.file.Path;

/**
 * Early initialization for Catalyst configuration and field syncing.
 * 
 * <p>This class handles config loading and syncing values to injected fields
 * in transformed Hytale classes.</p>
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

        // Chunk generation fields (ChunkGeneratorTransformer)
        setStaticField("com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator",
            "$catalystPoolSizeEnabled", CatalystConfig.CHUNK_POOL_SIZE_ENABLED);
        setStaticField("com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator",
            "$catalystPoolSize", CatalystConfig.CHUNK_POOL_SIZE);
        setStaticField("com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator",
            "$catalystCacheSizeEnabled", CatalystConfig.CHUNK_CACHE_SIZE_ENABLED);
        setStaticField("com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator",
            "$catalystGeneratorCacheSize", CatalystConfig.GENERATOR_CACHE_SIZE);
        setStaticField("com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator",
            "$catalystCaveCacheSize", CatalystConfig.CAVE_CACHE_SIZE);
        setStaticField("com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator",
            "$catalystPrefabCacheSize", CatalystConfig.PREFAB_CACHE_SIZE);
        setStaticField("com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator",
            "$catalystThreadPriorityEnabled", CatalystConfig.CHUNK_THREAD_PRIORITY_ENABLED);
        setStaticField("com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator",
            "$catalystThreadPriority", CatalystConfig.CHUNK_THREAD_PRIORITY);

        // Lighting optimization fields (FloodLightCalculationTransformer)
        // Skip empty sections - skips processing sections that are solid air with no light
        setStaticField("com.hypixel.hytale.server.core.universe.world.lighting.FloodLightCalculation",
            "$catalystSkipEmptyEnabled", CatalystConfig.LIGHT_SKIP_EMPTY_ENABLED);

        // Visual effects - sync to injected fields in Hytale classes
        setStaticField("com.hypixel.hytale.server.core.universe.world.ParticleUtil",
            "$catalystParticlesEnabled", CatalystConfig.PARTICLES_ENABLED);
        setStaticField("com.hypixel.hytale.server.core.entity.AnimationUtils",
            "$catalystAnimationsEnabled", CatalystConfig.ANIMATIONS_ENABLED);

        // Caching optimizations
        // BlockChunk section cache - caches last accessed section for Y-iteration patterns
        setStaticField("com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk",
            "$catalystSectionCacheEnabled", CatalystConfig.BLOCK_SECTION_CACHE_ENABLED);

        // BlockType lookup cache - caches BlockType by block ID
        setStaticField("com.hypixel.hytale.assetstore.map.BlockTypeAssetMap",
            "$catalystBlockTypeCacheEnabled", CatalystConfig.BLOCK_TYPE_CACHE_ENABLED);
        setStaticField("com.hypixel.hytale.assetstore.map.BlockTypeAssetMap",
            "$catalystBlockTypeCacheSize", CatalystConfig.BLOCK_TYPE_CACHE_SIZE);

        // LocalCachedChunkAccessor - caches last 2 accessed chunks during lighting
        setStaticField("com.hypixel.hytale.server.core.universe.world.accessor.LocalCachedChunkAccessor",
            "$catalystLocalChunkCacheEnabled", CatalystConfig.LOCAL_CHUNK_CACHE_ENABLED);
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
