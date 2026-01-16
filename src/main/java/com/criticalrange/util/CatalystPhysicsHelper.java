package com.criticalrange.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Physics optimization helper.
 * Called by injected bytecode in physics-related systems.
 * 
 * Optimizations:
 * 1. Throttle item physics under heavy load
 * 2. Reduce repulsion calculation frequency
 * 3. Use entity index distribution for load balancing
 */
public class CatalystPhysicsHelper {
    
    // ========== Configuration ==========
    
    /** TPS threshold for heavy load */
    private static final double HEAVY_LOAD_TPS = 18.0;
    
    /** TPS threshold for critical load */
    private static final double CRITICAL_LOAD_TPS = 15.0;
    
    // ========== State ==========
    
    private static final AtomicLong tickCounter = new AtomicLong(0);
    
    // ========== Statistics ==========
    
    private static final AtomicLong physicsSkipped = new AtomicLong(0);
    private static final AtomicLong physicsProcessed = new AtomicLong(0);
    private static final AtomicLong itemPhysicsSkipped = new AtomicLong(0);
    private static final AtomicLong repulsionSkipped = new AtomicLong(0);
    
    // ========== Optimization Logic ==========
    
    /**
     * Determine if physics should be processed for this entity.
     * 
     * @param index Entity index within the ArchetypeChunk
     * @param className The system class name for system-specific rules
     * @return true to process physics, false to skip
     */
    public static boolean shouldTickPhysics(int index, String className) {
        if (!com.criticalrange.CatalystConfig.PHYSICS_OPTIMIZATION_ENABLED) {
            physicsProcessed.incrementAndGet();
            return true;
        }
        
        long tick = tickCounter.get();
        double tps = CatalystMetrics.getCurrentTPS();
        
        // Item physics - can be throttled aggressively
        if (className.contains("ItemPhysicsSystem")) {
            return shouldTickItemPhysics(index, tick, tps);
        }
        
        // Repulsion - expensive O(n²) typically, throttle heavily
        if (className.contains("RepulsionTicker") || className.contains("Repulsion")) {
            return shouldTickRepulsion(index, tick, tps);
        }
        
        // Default: normal entity physics
        return shouldTickDefaultPhysics(index, tick, tps);
    }
    
    private static boolean shouldTickItemPhysics(int index, long tick, double tps) {
        // Items on ground don't need frequent physics updates
        // Use aggressive throttling based on load
        
        if (tps < CRITICAL_LOAD_TPS) {
            // Critical load: only 1/8 of items update per tick
            if ((index + tick) % 8 != 0) {
                physicsSkipped.incrementAndGet();
                itemPhysicsSkipped.incrementAndGet();
                return false;
            }
        } else if (tps < HEAVY_LOAD_TPS) {
            // Heavy load: only 1/4 of items update per tick
            if ((index + tick) % 4 != 0) {
                physicsSkipped.incrementAndGet();
                itemPhysicsSkipped.incrementAndGet();
                return false;
            }
        } else {
            // Normal load: still throttle 1/2 for items
            if ((index + tick) % 2 != 0) {
                physicsSkipped.incrementAndGet();
                itemPhysicsSkipped.incrementAndGet();
                return false;
            }
        }
        
        physicsProcessed.incrementAndGet();
        return true;
    }
    
    private static boolean shouldTickRepulsion(int index, long tick, double tps) {
        // Repulsion is very expensive - always throttle
        // Entities overlapping slightly is acceptable for performance
        
        if (tps < CRITICAL_LOAD_TPS) {
            // Critical load: only 1/10 of entities checked per tick
            if ((index + tick) % 10 != 0) {
                physicsSkipped.incrementAndGet();
                repulsionSkipped.incrementAndGet();
                return false;
            }
        } else if (tps < HEAVY_LOAD_TPS) {
            // Heavy load: 1/6 of entities
            if ((index + tick) % 6 != 0) {
                physicsSkipped.incrementAndGet();
                repulsionSkipped.incrementAndGet();
                return false;
            }
        } else {
            // Normal load: 1/4 of entities (still throttle repulsion)
            if ((index + tick) % 4 != 0) {
                physicsSkipped.incrementAndGet();
                repulsionSkipped.incrementAndGet();
                return false;
            }
        }
        
        physicsProcessed.incrementAndGet();
        return true;
    }
    
    private static boolean shouldTickDefaultPhysics(int index, long tick, double tps) {
        // Default physics: only throttle under load
        
        if (tps < CRITICAL_LOAD_TPS) {
            if ((index + tick) % 4 != 0) {
                physicsSkipped.incrementAndGet();
                return false;
            }
        } else if (tps < HEAVY_LOAD_TPS) {
            if ((index + tick) % 2 != 0) {
                physicsSkipped.incrementAndGet();
                return false;
            }
        }
        
        physicsProcessed.incrementAndGet();
        return true;
    }
    
    /**
     * Called once per server tick.
     */
    public static void onServerTick() {
        tickCounter.incrementAndGet();
    }
    
    // ========== Statistics ==========
    
    public static long getSkipped() {
        return physicsSkipped.get();
    }
    
    public static long getProcessed() {
        return physicsProcessed.get();
    }
    
    public static long getItemPhysicsSkipped() {
        return itemPhysicsSkipped.get();
    }
    
    public static long getRepulsionSkipped() {
        return repulsionSkipped.get();
    }
    
    public static double getSkipRatio() {
        long skipped = physicsSkipped.get();
        long processed = physicsProcessed.get();
        long total = skipped + processed;
        if (total == 0) return 0;
        return (skipped * 100.0) / total;
    }
    
    public static void resetStats() {
        physicsSkipped.set(0);
        physicsProcessed.set(0);
        itemPhysicsSkipped.set(0);
        repulsionSkipped.set(0);
    }
}
