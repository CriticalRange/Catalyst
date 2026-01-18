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
 * </ul>
 *
 * @author CriticalRange
 */
public class Catalyst extends JavaPlugin {

    private static Catalyst instance;
    private CatalystTransformerManager transformerManager;

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
