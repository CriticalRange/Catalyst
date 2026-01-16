package com.criticalrange.util;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Helper class for tick optimization.
 * Called by injected bytecode to determine if an entity should be ticked.
 * 
 * This class implements a distributed tick scheduling approach:
 * - Entities are assigned to "tick groups" based on their index
 * - Only a subset of groups are processed each server tick
 * - Under load, more groups are skipped
 * 
 * This approach doesn't require knowing entity positions (which would
 * require complex reflection), but still reduces tick load effectively.
 */
public class CatalystTickHelper {
    
    // ========== Configuration ==========
    
    /** Number of tick groups to distribute entities across */
    private static final int TICK_GROUPS = 4;
    
    /** Under heavy load, only this many groups tick per server tick */
    private static final int HEAVY_LOAD_GROUPS = 2;
    
    /** TPS threshold below which we enable aggressive throttling */
    private static final double HEAVY_LOAD_TPS_THRESHOLD = 18.0;
    
    /** TPS threshold below which we enable maximum throttling */
    private static final double CRITICAL_LOAD_TPS_THRESHOLD = 15.0;
    
    // ========== State ==========
    
    /** Current server tick (updated by onServerTick) */
    private static final AtomicLong serverTickCounter = new AtomicLong(0);
    
    /** DoTick batch counter (updated by onDoTick) */
    private static final AtomicLong doTickCounter = new AtomicLong(0);
    
    /** Current server TPS (updated by updateTPS) */
    private static volatile double currentTPS = 20.0;
    
    // ========== Statistics ==========
    
    private static final AtomicLong ticksSkipped = new AtomicLong(0);
    private static final AtomicLong ticksProcessed = new AtomicLong(0);
    
    // Per-system statistics (simple counters, not per-class breakdown)
    private static final AtomicLong repulsionSkipped = new AtomicLong(0);
    private static final AtomicLong movementSkipped = new AtomicLong(0);
    private static final AtomicLong locationSkipped = new AtomicLong(0);
    
    // ========== Optimization Logic ==========
    
    /**
     * Called by injected code at the start of EntityTickingSystem.doTick().
     * Tracks that a new batch of entity ticks is starting.
     */
    public static void onDoTick() {
        doTickCounter.incrementAndGet();
    }
    
    /**
     * Determine if a specific entity tick should proceed.
     * Uses distributed tick scheduling based on entity index.
     * 
     * @param index The entity index within the ArchetypeChunk
     * @param className The system class name (for per-system tuning)
     * @return true if the tick should proceed, false to skip
     */
    public static boolean shouldTickEntity(int index, String className) {
        // Check if optimization is enabled
        if (!com.criticalrange.CatalystConfig.TICK_OPTIMIZATION_ENABLED) {
            ticksProcessed.incrementAndGet();
            return true;
        }
        
        long tick = serverTickCounter.get();
        double tps = currentTPS;
        
        // Determine how many groups to tick based on server load
        int activeGroups;
        if (tps < CRITICAL_LOAD_TPS_THRESHOLD) {
            // Critical load: only 1 group per tick (25% of entities)
            activeGroups = 1;
        } else if (tps < HEAVY_LOAD_TPS_THRESHOLD) {
            // Heavy load: 2 groups per tick (50% of entities)
            activeGroups = HEAVY_LOAD_GROUPS;
        } else {
            // Normal: all groups tick (100% of entities)
            activeGroups = TICK_GROUPS;
        }
        
        // Check if this entity's group is active this tick
        int entityGroup = index % TICK_GROUPS;
        boolean shouldTick = (entityGroup < activeGroups) || 
                            ((tick + entityGroup) % TICK_GROUPS < activeGroups);
        
        // Apply per-system tuning
        if (shouldTick) {
            shouldTick = applySystemSpecificRules(index, tick, tps, className);
        }
        
        // Record statistics
        if (shouldTick) {
            ticksProcessed.incrementAndGet();
        } else {
            ticksSkipped.incrementAndGet();
            recordSystemSkip(className);
        }
        
        return shouldTick;
    }
    
    /**
     * Apply system-specific optimization rules.
     * Some systems can be throttled more aggressively than others.
     */
    private static boolean applySystemSpecificRules(int index, long tick, double tps, String className) {
        // Repulsion system - can skip 75% under normal load, 90% under heavy load
        // Entities overlapping slightly is acceptable for performance
        if (className.contains("RepulsionTicker") || className.contains("Repulsion")) {
            if (tps < HEAVY_LOAD_TPS_THRESHOLD) {
                // Only tick 1 in 10 entities
                if (index % 10 != (tick % 10)) {
                    repulsionSkipped.incrementAndGet();
                    return false;
                }
            } else {
                // Only tick 1 in 4 entities
                if (index % 4 != (tick % 4)) {
                    repulsionSkipped.incrementAndGet();
                    return false;
                }
            }
        }
        
        // Movement states - can be delayed for non-visible entities
        // Visual state changes are batched anyway
        if (className.contains("MovementStatesSystems")) {
            if (tps < HEAVY_LOAD_TPS_THRESHOLD) {
                // Skip 50% even when "allowed" by group
                if (index % 2 == 0 && tick % 2 != 0) {
                    movementSkipped.incrementAndGet();
                    return false;
                }
            }
        }
        
        // UpdateLocation - be more careful, but still can batch
        if (className.contains("UpdateLocationSystems")) {
            if (tps < CRITICAL_LOAD_TPS_THRESHOLD) {
                // Skip 50% under critical load
                if (index % 2 == 0 && tick % 2 != 0) {
                    locationSkipped.incrementAndGet();
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private static void recordSystemSkip(String className) {
        // Simple tracking without creating per-class maps
        if (className.contains("Repulsion")) {
            repulsionSkipped.incrementAndGet();
        } else if (className.contains("Movement")) {
            movementSkipped.incrementAndGet();
        } else if (className.contains("Location")) {
            locationSkipped.incrementAndGet();
        }
    }
    
    // ========== Server Tick Tracking ==========
    
    /**
     * Called once per server tick to update the tick counter.
     */
    public static void onServerTick() {
        serverTickCounter.incrementAndGet();
    }
    
    /**
     * Update the current TPS measurement.
     * @param tps Current ticks per second
     */
    public static void updateTPS(double tps) {
        currentTPS = tps;
    }
    
    // ========== Getters ==========
    
    public static double getCurrentTPS() {
        return currentTPS;
    }
    
    public static long getTicksSkipped() {
        return ticksSkipped.get();
    }
    
    public static long getTicksProcessed() {
        return ticksProcessed.get();
    }
    
    public static double getSkipRatio() {
        long skipped = ticksSkipped.get();
        long processed = ticksProcessed.get();
        long total = skipped + processed;
        
        if (total == 0) return 0;
        return (skipped * 100.0) / total;
    }
    
    public static long getRepulsionSkipped() {
        return repulsionSkipped.get();
    }
    
    public static long getMovementSkipped() {
        return movementSkipped.get();
    }
    
    public static long getLocationSkipped() {
        return locationSkipped.get();
    }
    
    /**
     * Reset statistics.
     */
    public static void resetStats() {
        ticksSkipped.set(0);
        ticksProcessed.set(0);
        repulsionSkipped.set(0);
        movementSkipped.set(0);
        locationSkipped.set(0);
    }
}
