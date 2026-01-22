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
        int entityViewMultiplier = 32;
        boolean chunkRateEnabled = false;
        int chunksPerTick = 4;

        // Pathfinding
        boolean pathfindingEnabled = false;
        int maxPathLength = 200;
        int openNodesLimit = 80;
        int totalNodesLimit = 400;
    }
}
