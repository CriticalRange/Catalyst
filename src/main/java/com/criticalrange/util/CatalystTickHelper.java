package com.criticalrange.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Helper class for tick optimization.
 * Called by injected bytecode to determine if an entity should be ticked.
 */
public class CatalystTickHelper {
    
    /** Distance in blocks beyond which entities skip ticks */
    private static final int TICK_DISTANCE = 128; // 8 chunks
    
    /** Distance for reduced tick rate (tick every N ticks) */
    private static final int REDUCED_TICK_DISTANCE = 64; // 4 chunks
    
    /** Current server TPS (updated by monitoring) */
    private static volatile double currentTPS = 20.0;
    
    /** Tick counter for reduced tick rate calculations */
    private static final AtomicLong tickCounter = new AtomicLong(0);
    
    /** Stats tracking */
    private static final AtomicLong ticksSkipped = new AtomicLong(0);
    private static final AtomicLong ticksProcessed = new AtomicLong(0);
    
    /**
     * Determine if an entity tick should proceed.
     * This is called by injected bytecode at the start of tick methods.
     * 
     * @param entity The entity being ticked (could be any object)
     * @return true if the tick should proceed, false to skip
     */
    public static boolean shouldTick(Object entity) {
        if (entity == null) {
            return true; // Don't interfere with null checks
        }
        
        try {
            // Get distance to nearest player
            double distance = getDistanceToNearestPlayer(entity);
            
            // Always tick entities close to players
            if (distance < REDUCED_TICK_DISTANCE) {
                ticksProcessed.incrementAndGet();
                return true;
            }
            
            // Skip entities very far from players
            if (distance > TICK_DISTANCE) {
                ticksSkipped.incrementAndGet();
                return false;
            }
            
            // Reduced tick rate for entities in the middle distance
            // Tick every 2nd tick
            long currentTick = tickCounter.get();
            if (currentTick % 2 == 0) {
                ticksProcessed.incrementAndGet();
                return true;
            } else {
                ticksSkipped.incrementAndGet();
                return false;
            }
            
        } catch (Throwable t) {
            // On any error, allow the tick to proceed (fail-safe)
            return true;
        }
    }
    
    /**
     * Get the distance from an entity to the nearest player.
     * Uses reflection to work with any entity type.
     */
    private static double getDistanceToNearestPlayer(Object entity) {
        try {
            // Try to get position from the entity
            // This uses reflection since we don't have direct access to entity classes
            
            // Try common getter methods
            Object position = tryGetPosition(entity);
            if (position == null) {
                return 0; // Can't determine distance, tick anyway
            }
            
            // For now, return a default that allows ticking
            // This will be enhanced when we have access to player tracking
            return 0;
            
        } catch (Throwable t) {
            return 0; // Fail-safe: tick the entity
        }
    }
    
    /**
     * Try to get position from an entity using reflection.
     */
    private static Object tryGetPosition(Object entity) {
        try {
            // Try getPosition()
            var method = entity.getClass().getMethod("getPosition");
            return method.invoke(entity);
        } catch (NoSuchMethodException e) {
            // Try position()
            try {
                var method = entity.getClass().getMethod("position");
                return method.invoke(entity);
            } catch (Exception e2) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Increment the global tick counter.
     * Should be called once per server tick.
     */
    public static void onServerTick() {
        tickCounter.incrementAndGet();
    }
    
    /**
     * Update the current TPS measurement.
     */
    public static void updateTPS(double tps) {
        currentTPS = tps;
    }
    
    /**
     * Get current TPS.
     */
    public static double getCurrentTPS() {
        return currentTPS;
    }
    
    /**
     * Get count of ticks that were skipped.
     */
    public static long getTicksSkipped() {
        return ticksSkipped.get();
    }
    
    /**
     * Get count of ticks that were processed.
     */
    public static long getTicksProcessed() {
        return ticksProcessed.get();
    }
    
    /**
     * Get skip ratio as a percentage.
     */
    public static double getSkipRatio() {
        long skipped = ticksSkipped.get();
        long processed = ticksProcessed.get();
        long total = skipped + processed;
        
        if (total == 0) return 0;
        return (skipped * 100.0) / total;
    }
    
    /**
     * Reset statistics.
     */
    public static void resetStats() {
        ticksSkipped.set(0);
        ticksProcessed.set(0);
    }
}
