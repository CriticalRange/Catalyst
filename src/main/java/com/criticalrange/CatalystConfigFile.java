package com.criticalrange;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Handles loading and saving Catalyst configuration to a JSON file.
 *
 * <p>Configuration is stored in the plugin's data directory as {@code catalyst.json}.</p>
 *
 * <p>The file uses a simple JSON structure with all configuration options:</p>
 * <pre>
 * {
 *   "entityDistanceEnabled": false,
 *   "entityViewMultiplier": 32,
 *   "chunkRateEnabled": false,
 *   "chunksPerTick": 4,
 *   "pathfindingEnabled": false,
 *   "maxPathLength": 200,
 *   "openNodesLimit": 80,
 *   "totalNodesLimit": 400
 * }
 * </pre>
 */
public class CatalystConfigFile {

    private static final String CONFIG_FILE_NAME = "catalyst.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path configPath;

    /**
     * Creates a config file handler for the given data directory.
     *
     * @param dataDirectory The plugin's data directory from {@code getDataDirectory()}
     */
    public CatalystConfigFile(Path dataDirectory) {
        this.configPath = dataDirectory.resolve(CONFIG_FILE_NAME);
    }

    /**
     * Loads configuration from the file into {@link CatalystConfig}.
     *
     * <p>If the file doesn't exist, creates a default configuration file.</p>
     *
     * @return true if config was loaded successfully, false if defaults were used
     */
    public boolean load() {
        if (!Files.exists(configPath)) {
            System.out.println("[Catalyst] No config file found, creating default: " + configPath);
            save(); // Create default config
            return false;
        }

        try {
            String json = Files.readString(configPath);
            ConfigData data = GSON.fromJson(json, ConfigData.class);

            if (data != null) {
                applyToConfig(data);
                System.out.println("[Catalyst] Configuration loaded from: " + configPath);
                return true;
            }
        } catch (IOException e) {
            System.err.println("[Catalyst] Failed to load config: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[Catalyst] Invalid config file format: " + e.getMessage());
        }

        return false;
    }

    /**
     * Saves current {@link CatalystConfig} values to the config file.
     */
    public void save() {
        try {
            // Ensure parent directory exists
            Files.createDirectories(configPath.getParent());

            ConfigData data = createFromConfig();
            String json = GSON.toJson(data);
            Files.writeString(configPath, json);

            System.out.println("[Catalyst] Configuration saved to: " + configPath);
        } catch (IOException e) {
            System.err.println("[Catalyst] Failed to save config: " + e.getMessage());
        }
    }

    /**
     * Applies loaded config data to the static {@link CatalystConfig} fields.
     */
    private void applyToConfig(ConfigData data) {
        // Runtime optimizations
        CatalystConfig.ENTITY_DISTANCE_ENABLED = data.entityDistanceEnabled;
        CatalystConfig.ENTITY_VIEW_MULTIPLIER = clamp(data.entityViewMultiplier, 8, 64);
        CatalystConfig.CHUNK_RATE_ENABLED = data.chunkRateEnabled;
        CatalystConfig.CHUNKS_PER_TICK = clamp(data.chunksPerTick, 1, 16);

        // Pathfinding
        CatalystConfig.PATHFINDING_ENABLED = data.pathfindingEnabled;
        CatalystConfig.MAX_PATH_LENGTH = clamp(data.maxPathLength, 50, 500);
        CatalystConfig.OPEN_NODES_LIMIT = clamp(data.openNodesLimit, 20, 200);
        CatalystConfig.TOTAL_NODES_LIMIT = clamp(data.totalNodesLimit, 100, 1000);

        // Chunk generation
        CatalystConfig.CHUNK_POOL_SIZE_ENABLED = data.chunkPoolSizeEnabled;
        CatalystConfig.CHUNK_POOL_SIZE = clamp(data.chunkPoolSize, 1, 32);
        CatalystConfig.CHUNK_CACHE_SIZE_ENABLED = data.chunkCacheSizeEnabled;
        CatalystConfig.GENERATOR_CACHE_SIZE = clamp(data.generatorCacheSize, 10000, 200000);
        CatalystConfig.CAVE_CACHE_SIZE = clamp(data.caveCacheSize, 1000, 50000);
        CatalystConfig.PREFAB_CACHE_SIZE = clamp(data.prefabCacheSize, 10, 500);
        CatalystConfig.CHUNK_THREAD_PRIORITY_ENABLED = data.chunkThreadPriorityEnabled;
        CatalystConfig.CHUNK_THREAD_PRIORITY = clamp(data.chunkThreadPriority, Thread.MIN_PRIORITY, Thread.MAX_PRIORITY);

        // Lighting
        CatalystConfig.LIGHT_SKIP_EMPTY_ENABLED = data.lightSkipEmptyEnabled;

        // Visual effects
        CatalystConfig.PARTICLES_ENABLED = data.particlesEnabled;
        CatalystConfig.ANIMATIONS_ENABLED = data.animationsEnabled;

        // Caching optimizations
        CatalystConfig.BLOCK_SECTION_CACHE_ENABLED = data.blockSectionCacheEnabled;
        CatalystConfig.BLOCK_TYPE_CACHE_ENABLED = data.blockTypeCacheEnabled;
        CatalystConfig.BLOCK_TYPE_CACHE_SIZE = clamp(data.blockTypeCacheSize, 64, 1024);
        CatalystConfig.LOCAL_CHUNK_CACHE_ENABLED = data.localChunkCacheEnabled;

        // Advanced optimizations
        CatalystConfig.BLOCK_ENTITY_SLEEP_ENABLED = data.blockEntitySleepEnabled;
        CatalystConfig.BLOCK_ENTITY_SLEEP_INTERVAL = clamp(data.blockEntitySleepInterval, 1, 100);
        CatalystConfig.STAT_RECALC_THROTTLE_ENABLED = data.statRecalcThrottleEnabled;
        CatalystConfig.STAT_RECALC_INTERVAL = clamp(data.statRecalcInterval, 1, 40);
        CatalystConfig.BLOCK_UPDATE_BATCHING_ENABLED = data.blockUpdateBatchingEnabled;
        CatalystConfig.BLOCK_UPDATE_BATCH_SIZE = clamp(data.blockUpdateBatchSize, 16, 256);
        CatalystConfig.FLOOD_FILL_LIMIT_ENABLED = data.floodFillLimitEnabled;
        CatalystConfig.FLOOD_FILL_MAX_ITERATIONS = clamp(data.floodFillMaxIterations, 1000, 20000);
        CatalystConfig.PATHFINDING_POOL_ENABLED = data.pathfindingPoolEnabled;
        CatalystConfig.PATHFINDING_POOL_SIZE = clamp(data.pathfindingPoolSize, 128, 2048);
    }

    /**
     * Creates config data from current {@link CatalystConfig} values.
     */
    private ConfigData createFromConfig() {
        ConfigData data = new ConfigData();

        // Runtime optimizations
        data.entityDistanceEnabled = CatalystConfig.ENTITY_DISTANCE_ENABLED;
        data.entityViewMultiplier = CatalystConfig.ENTITY_VIEW_MULTIPLIER;
        data.chunkRateEnabled = CatalystConfig.CHUNK_RATE_ENABLED;
        data.chunksPerTick = CatalystConfig.CHUNKS_PER_TICK;

        // Pathfinding
        data.pathfindingEnabled = CatalystConfig.PATHFINDING_ENABLED;
        data.maxPathLength = CatalystConfig.MAX_PATH_LENGTH;
        data.openNodesLimit = CatalystConfig.OPEN_NODES_LIMIT;
        data.totalNodesLimit = CatalystConfig.TOTAL_NODES_LIMIT;

        // Chunk generation
        data.chunkPoolSizeEnabled = CatalystConfig.CHUNK_POOL_SIZE_ENABLED;
        data.chunkPoolSize = CatalystConfig.CHUNK_POOL_SIZE;
        data.chunkCacheSizeEnabled = CatalystConfig.CHUNK_CACHE_SIZE_ENABLED;
        data.generatorCacheSize = CatalystConfig.GENERATOR_CACHE_SIZE;
        data.caveCacheSize = CatalystConfig.CAVE_CACHE_SIZE;
        data.prefabCacheSize = CatalystConfig.PREFAB_CACHE_SIZE;
        data.chunkThreadPriorityEnabled = CatalystConfig.CHUNK_THREAD_PRIORITY_ENABLED;
        data.chunkThreadPriority = CatalystConfig.CHUNK_THREAD_PRIORITY;

        // Lighting
        data.lightSkipEmptyEnabled = CatalystConfig.LIGHT_SKIP_EMPTY_ENABLED;

        // Visual effects
        data.particlesEnabled = CatalystConfig.PARTICLES_ENABLED;
        data.animationsEnabled = CatalystConfig.ANIMATIONS_ENABLED;

        // Caching optimizations
        data.blockSectionCacheEnabled = CatalystConfig.BLOCK_SECTION_CACHE_ENABLED;
        data.blockTypeCacheEnabled = CatalystConfig.BLOCK_TYPE_CACHE_ENABLED;
        data.blockTypeCacheSize = CatalystConfig.BLOCK_TYPE_CACHE_SIZE;
        data.localChunkCacheEnabled = CatalystConfig.LOCAL_CHUNK_CACHE_ENABLED;

        // Advanced optimizations
        data.blockEntitySleepEnabled = CatalystConfig.BLOCK_ENTITY_SLEEP_ENABLED;
        data.blockEntitySleepInterval = CatalystConfig.BLOCK_ENTITY_SLEEP_INTERVAL;
        data.statRecalcThrottleEnabled = CatalystConfig.STAT_RECALC_THROTTLE_ENABLED;
        data.statRecalcInterval = CatalystConfig.STAT_RECALC_INTERVAL;
        data.blockUpdateBatchingEnabled = CatalystConfig.BLOCK_UPDATE_BATCHING_ENABLED;
        data.blockUpdateBatchSize = CatalystConfig.BLOCK_UPDATE_BATCH_SIZE;
        data.floodFillLimitEnabled = CatalystConfig.FLOOD_FILL_LIMIT_ENABLED;
        data.floodFillMaxIterations = CatalystConfig.FLOOD_FILL_MAX_ITERATIONS;
        data.pathfindingPoolEnabled = CatalystConfig.PATHFINDING_POOL_ENABLED;
        data.pathfindingPoolSize = CatalystConfig.PATHFINDING_POOL_SIZE;

        return data;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Internal data class for JSON serialization.
     */
    private static class ConfigData {
        // Runtime optimizations
        boolean entityDistanceEnabled = false;
        int entityViewMultiplier = CatalystConfig.DEFAULT_BLOCKS_PER_CHUNK;
        boolean chunkRateEnabled = false;
        int chunksPerTick = CatalystConfig.DEFAULT_CHUNKS_PER_TICK;

        // Pathfinding
        boolean pathfindingEnabled = false;
        int maxPathLength = CatalystConfig.DEFAULT_MAX_PATH_LENGTH;
        int openNodesLimit = CatalystConfig.DEFAULT_OPEN_NODES_LIMIT;
        int totalNodesLimit = CatalystConfig.DEFAULT_TOTAL_NODES_LIMIT;

        // Chunk generation
        boolean chunkPoolSizeEnabled = false;
        int chunkPoolSize = Runtime.getRuntime().availableProcessors();
        boolean chunkCacheSizeEnabled = false;
        int generatorCacheSize = CatalystConfig.DEFAULT_GENERATOR_CACHE_SIZE;
        int caveCacheSize = CatalystConfig.DEFAULT_CAVE_CACHE_SIZE;
        int prefabCacheSize = CatalystConfig.DEFAULT_PREFAB_CACHE_SIZE;
        boolean chunkThreadPriorityEnabled = false;
        int chunkThreadPriority = Thread.NORM_PRIORITY;

        // Lighting
        boolean lightSkipEmptyEnabled = false;

        // Visual effects
        boolean particlesEnabled = true;
        boolean animationsEnabled = true;

        // Caching optimizations
        boolean blockSectionCacheEnabled = false;
        boolean blockTypeCacheEnabled = false;
        int blockTypeCacheSize = 256;
        boolean localChunkCacheEnabled = false;

        // Advanced optimizations
        boolean blockEntitySleepEnabled = false;
        int blockEntitySleepInterval = 20;
        boolean statRecalcThrottleEnabled = false;
        int statRecalcInterval = 5;
        boolean blockUpdateBatchingEnabled = false;
        int blockUpdateBatchSize = 64;
        boolean floodFillLimitEnabled = false;
        int floodFillMaxIterations = 5000;
        boolean pathfindingPoolEnabled = false;
        int pathfindingPoolSize = 512;
    }
}
