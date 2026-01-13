package com.criticalrange;

import com.criticalrange.transformer.CatalystTransformerManager;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;

/**
 * Catalyst - Hytale Performance Optimization Mod
 * 
 * This mod uses Early Plugin (Class Transformer) technology to optimize
 * server performance through bytecode manipulation.
 * 
 * Optimizations implemented:
 * - Tick Rate Optimization (skip distant entities)
 * - Entity Tracking Optimization (spatial partitioning)
 * - Chunk Loading Optimization (async loading, caching)
 * - Network Packet Batching
 * - Memory Management (object pooling)
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

    @Override
    protected void setup() {
        log("╔════════════════════════════════════════════╗");
        log("║       Catalyst Performance Optimizer        ║");
        log("║              Version 1.0.0                  ║");
        log("╚════════════════════════════════════════════╝");
        
        // Initialize transformer manager for metrics tracking
        transformerManager = new CatalystTransformerManager();
        
        // Register commands
        try {
            getCommandRegistry().registerCommand(new com.criticalrange.command.CatalystCommand());
        } catch (Throwable t) {
            log("Failed to register command: " + t.getMessage());
        }
        
        log("Catalyst initialized - bytecode transformations active!");
    }

    @Override
    protected void start() {
        super.start();
        log("Catalyst started - server performance optimized!");
        
        // Log active optimizations
        logActiveOptimizations();
    }

    @Override
    protected void shutdown() {
        log("Catalyst shutting down...");
        
        // Log performance metrics before shutdown
        if (transformerManager != null) {
            transformerManager.logMetrics();
        }
        
        super.shutdown();
    }
    
    private void logActiveOptimizations() {
        log("Active optimizations:");
        log("  ✓ Tick Rate Optimization - Skip distant entities");
        log("  ✓ Entity Tracking - Spatial partitioning");
        log("  ✓ Chunk Loading - Async & predictive caching");
        log("  ✓ Packet Batching - Reduce network overhead");
        log("  ✓ Memory Pooling - Reduce GC pressure");
    }
    
    /**
     * Log a message using System.out (works in all contexts).
     */
    private void log(String message) {
        System.out.println("[Catalyst] " + message);
    }
    
    public CatalystTransformerManager getTransformerManager() {
        return transformerManager;
    }
}
