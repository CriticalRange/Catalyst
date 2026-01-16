package com.criticalrange;

import com.criticalrange.transformer.CatalystTransformerManager;
import com.criticalrange.util.CatalystMetrics;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;

/**
 * Catalyst - Hytale Performance Optimization Mod
 *
 * <p>This mod uses Early Plugin (Class Transformer) technology to optimize
 * server performance through bytecode manipulation.</p>
 *
 * <p>Optimizations implemented:</p>
 * <ul>
 *   <li>Tick Rate Optimization (distributed entity updates)</li>
 *   <li>Entity Tracking Optimization (packet batching)</li>
 *   <li>Chunk Loading Metrics (I/O tracking)</li>
 *   <li>Movement Optimization (location/physics throttling)</li>
 *   <li>Lighting Optimization (rate-limited propagation)</li>
 *   <li>AI Optimization (distributed NPC updates)</li>
 *   <li>Physics Optimization (collision throttling)</li>
 *   <li>Network Optimization (adaptive flush batching)</li>
 * </ul>
 *
 * @author CriticalRange
 * @version 2.0.0
 */
public class Catalyst extends JavaPlugin {

    /** Current version of Catalyst */
    public static final String VERSION = "2.0.0";

    /** Singleton instance of the Catalyst plugin */
    private static Catalyst instance;

    /** Manager for all bytecode transformers */
    private CatalystTransformerManager transformerManager;

    /**
     * Creates a new Catalyst plugin instance.
     *
     * @param init The plugin initialization context provided by Hytale
     */
    public Catalyst(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    /**
     * Gets the singleton instance of Catalyst.
     *
     * @return The Catalyst plugin instance, or null if not yet initialized
     */
    public static Catalyst getInstance() {
        return instance;
    }

    @Override
    protected void setup() {
        log("╔════════════════════════════════════════════╗");
        log("║       Catalyst Performance Optimizer        ║");
        log("║              Version " + VERSION + "                  ║");
        log("╚════════════════════════════════════════════╝");

        // Initialize transformer manager for metrics tracking
        transformerManager = new CatalystTransformerManager();

        // Register commands
        try {
            getCommandRegistry().registerCommand(new com.criticalrange.command.CatalystCommand());
            log("Commands registered successfully");
        } catch (Throwable t) {
            log("Failed to register command: " + t.getMessage());
            t.printStackTrace();
        }

        log("Catalyst initialized - bytecode transformations active!");
    }

    @Override
    protected void start() {
        super.start();
        log("Catalyst started - server performance optimized!");

        // Log active optimizations
        logActiveOptimizations();
        logOptimizationConfig();
    }

    @Override
    protected void shutdown() {
        log("Catalyst shutting down...");

        // Log final performance metrics
        log("\n" + CatalystMetrics.generateReport());

        // Log transformer metrics
        if (transformerManager != null) {
            transformerManager.logMetrics();
        }

        super.shutdown();
    }

    /**
     * Logs all active optimizations to the console.
     */
    private void logActiveOptimizations() {
        log("Active optimizations:");
        log("  ✓ Server Tick Tracking - TPS monitoring & helper updates");
        log("  ✓ Tick Rate Optimization - Distributed entity updates");
        log("  ✓ Entity Tracking - Packet batching under load");
        log("  ✓ Chunk I/O Tracking - Load/save metrics");
        log("  ✓ Movement Optimization - Location/state throttling");
        log("  ✓ Lighting Optimization - Rate-limited propagation");
        log("  ✓ Physics Optimization - Item/repulsion throttling");
        log("  ✓ AI Optimization - Distributed NPC updates");
        log("  ✓ Network Optimization - Adaptive flush batching");
    }

    /**
     * Logs the current optimization configuration.
     */
    private void logOptimizationConfig() {
        log("Configuration:");
        log("  Tick Optimization: " + (CatalystConfig.TICK_OPTIMIZATION_ENABLED ? "ENABLED" : "DISABLED"));
        log("  Entity Tracking: " + (CatalystConfig.ENTITY_TRACKING_ENABLED ? "ENABLED" : "DISABLED"));
        log("  Chunk Cache: " + (CatalystConfig.CHUNK_CACHE_ENABLED ? "ENABLED" : "DISABLED"));
        log("  Lighting Optimization: " + (CatalystConfig.LIGHTING_OPTIMIZATION_ENABLED ? "ENABLED" : "DISABLED"));
        log("  Movement Optimization: " + (CatalystConfig.MOVEMENT_OPTIMIZATION_ENABLED ? "ENABLED" : "DISABLED"));
        log("  Network Optimization: " + (CatalystConfig.NETWORK_OPTIMIZATION_ENABLED ? "ENABLED" : "DISABLED"));
        log("  Physics Optimization: " + (CatalystConfig.PHYSICS_OPTIMIZATION_ENABLED ? "ENABLED" : "DISABLED"));
        log("  AI Optimization: " + (CatalystConfig.AI_OPTIMIZATION_ENABLED ? "ENABLED" : "DISABLED"));
    }

    /**
     * Logs a message using System.out (works in all contexts).
     *
     * @param message The message to log
     */
    private void log(String message) {
        System.out.println("[Catalyst] " + message);
    }

    /**
     * Gets the transformer manager.
     *
     * @return The CatalystTransformerManager instance
     */
    public CatalystTransformerManager getTransformerManager() {
        return transformerManager;
    }
}
