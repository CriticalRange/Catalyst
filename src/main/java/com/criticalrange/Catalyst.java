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
 * @author CriticalRange
 * @version 2.0.0
 */
public class Catalyst extends JavaPlugin {

    public static final String VERSION = "2.0.0";
    private static Catalyst instance;
    private CatalystTransformerManager transformerManager;

    public Catalyst(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    public static Catalyst getInstance() {
        return instance;
    }

    @Override
    protected void setup() {
        transformerManager = new CatalystTransformerManager();

        try {
            getCommandRegistry().registerCommand(new com.criticalrange.command.CatalystCommand());
        } catch (Throwable t) {
            log("Failed to register command: " + t.getMessage());
        }

        log("Catalyst " + VERSION + " initialized.");
    }

    @Override
    protected void start() {
        super.start();
        logConfig();
    }

    @Override
    protected void shutdown() {
        log("Catalyst shutting down...");
        log("\n" + CatalystMetrics.generateReport());
        if (transformerManager != null) {
            transformerManager.logMetrics();
        }
        super.shutdown();
    }

    private void logConfig() {
        log("Active Optimizations:");
        log("  [+] Core: Tick(" + s(CatalystConfig.TICK_OPTIMIZATION_ENABLED) + 
            "), Entity(" + s(CatalystConfig.ENTITY_TRACKING_ENABLED) + 
            "), Chunk(" + s(CatalystConfig.CHUNK_CACHE_ENABLED) + ")");
            
        log("  [+] Advanced: Light(" + s(CatalystConfig.LIGHTING_OPTIMIZATION_ENABLED) + 
            "), Move(" + s(CatalystConfig.MOVEMENT_OPTIMIZATION_ENABLED) + 
            "), Net(" + s(CatalystConfig.NETWORK_OPTIMIZATION_ENABLED) + ")");
            
        log("  [+] Aggressive: Phys(" + s(CatalystConfig.PHYSICS_OPTIMIZATION_ENABLED) + 
            "), AI(" + s(CatalystConfig.AI_OPTIMIZATION_ENABLED) + ")");
            
        log("  [+] Deferred: Entities(" + s(CatalystConfig.LAZY_BLOCK_ENTITIES_ENABLED) + 
            "), Tick(" + s(CatalystConfig.LAZY_BLOCK_TICK_ENABLED) + 
            "), Fluid(" + s(CatalystConfig.LAZY_FLUID_ENABLED) + ")");
    }

    private String s(boolean enabled) {
        return enabled ? "ON" : "OFF";
    }

    private void log(String message) {
        System.out.println("[Catalyst] " + message);
    }

    public CatalystTransformerManager getTransformerManager() {
        return transformerManager;
    }
}
