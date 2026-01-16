package com.criticalrange.util;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Movement and location update optimization helper.
 *
 * <p>Called by injected bytecode in movement-related systems.</p>
 *
 * <p>Optimizations implemented:</p>
 * <ul>
 *   <li>Distributes entity updates across ticks to prevent tick spikes</li>
 *   <li>Rate-limits updates under heavy server load</li>
 *   <li>Tracks metrics for performance monitoring</li>
 * </ul>
 */
public class CatalystMovementHelper {

    // ========== Configuration ==========

    /** Number of tick groups to distribute entities across */
    private static final int TICK_GROUPS = 4;

    /** TPS threshold for heavy load mode */
    private static final double HEAVY_LOAD_TPS = 18.0;

    /** TPS threshold for critical load mode */
    private static final double CRITICAL_LOAD_TPS = 15.0;

    /** Maximum updates per tick before throttling */
    private static final int MAX_UPDATES_PER_TICK = 2000;

    // ========== State ==========

    /** Current tick counter */
    private static final AtomicLong tickCounter = new AtomicLong(0);

    /** Update counter for this tick */
    private static final AtomicInteger updateCount = new AtomicInteger(0);

    // ========== Statistics ==========

    private static final AtomicLong locationUpdatesProcessed = new AtomicLong(0);
    private static final AtomicLong locationUpdatesSkipped = new AtomicLong(0);
    private static final AtomicLong movementStatesProcessed = new AtomicLong(0);
    private static final AtomicLong movementStatesSkipped = new AtomicLong(0);
    private static final AtomicLong movementTicks = new AtomicLong(0);

    // ========== Optimization Logic ==========

    /**
     * Determines if a location update should proceed for this entity.
     *
     * @param index The entity index within the ArchetypeChunk
     * @return true if update should proceed, false to skip
     */
    public static boolean shouldUpdateEntityLocation(int index) {
        if (!com.criticalrange.CatalystConfig.MOVEMENT_OPTIMIZATION_ENABLED) {
            locationUpdatesProcessed.incrementAndGet();
            return true;
        }

        int count = updateCount.incrementAndGet();
        long tick = tickCounter.get();
        double tps = CatalystMetrics.getCurrentTPS();

        // Always allow some updates
        if (count <= MAX_UPDATES_PER_TICK / 2) {
            locationUpdatesProcessed.incrementAndGet();
            return true;
        }

        // Under critical load, use aggressive distribution
        if (tps < CRITICAL_LOAD_TPS) {
            // Only process 1/4 of entities per tick
            if ((index + tick) % 4 != 0) {
                locationUpdatesSkipped.incrementAndGet();
                return false;
            }
        } else if (tps < HEAVY_LOAD_TPS) {
            // Under heavy load, process 1/2 of entities per tick
            if ((index + tick) % 2 != 0) {
                locationUpdatesSkipped.incrementAndGet();
                return false;
            }
        } else if (count > MAX_UPDATES_PER_TICK) {
            // Over limit - skip every other
            if ((index + tick) % 2 != 0) {
                locationUpdatesSkipped.incrementAndGet();
                return false;
            }
        }

        locationUpdatesProcessed.incrementAndGet();
        return true;
    }

    /**
     * Determines if movement state update should proceed for this entity.
     *
     * @param index The entity index within the ArchetypeChunk
     * @return true if update should proceed, false to skip
     */
    public static boolean shouldUpdateMovementStates(int index) {
        if (!com.criticalrange.CatalystConfig.MOVEMENT_OPTIMIZATION_ENABLED) {
            movementStatesProcessed.incrementAndGet();
            return true;
        }

        long tick = tickCounter.get();
        double tps = CatalystMetrics.getCurrentTPS();

        // Movement states are less critical than location updates
        // They're mainly for visual sync and network updates

        // Under critical load, process only 1/4 of entities per tick
        if (tps < CRITICAL_LOAD_TPS) {
            if ((index + tick) % 4 != 0) {
                movementStatesSkipped.incrementAndGet();
                return false;
            }
        } else if (tps < HEAVY_LOAD_TPS) {
            // Under heavy load, process 1/2 of entities per tick
            if ((index + tick) % 2 != 0) {
                movementStatesSkipped.incrementAndGet();
                return false;
            }
        }

        movementStatesProcessed.incrementAndGet();
        return true;
    }

    /**
     * Legacy method - kept for backward compatibility.
     *
     * @param system The system object (not used)
     * @return true to proceed
     */
    public static boolean shouldUpdateLocation(Object system) {
        return shouldUpdateEntityLocation(0);
    }

    /**
     * Legacy method - kept for backward compatibility.
     *
     * @param system The system object (not used)
     * @return true to proceed
     */
    public static boolean shouldUpdateMovementState(Object system) {
        return shouldUpdateMovementStates(0);
    }

    /**
     * Called for generic movement tick tracking.
     */
    public static void onMovementTick() {
        movementTicks.incrementAndGet();
    }

    /**
     * Called once per server tick to reset per-tick counters.
     */
    public static void onServerTick() {
        tickCounter.incrementAndGet();
        updateCount.set(0);
    }

    // ========== Statistics ==========

    /**
     * Gets the number of location updates processed.
     *
     * @return Total processed location updates
     */
    public static long getLocationUpdatesProcessed() {
        return locationUpdatesProcessed.get();
    }

    /**
     * Gets the number of location updates skipped.
     *
     * @return Total skipped location updates
     */
    public static long getLocationUpdatesSkipped() {
        return locationUpdatesSkipped.get();
    }

    /**
     * Gets the number of movement state updates processed.
     *
     * @return Total processed movement state updates
     */
    public static long getMovementStatesProcessed() {
        return movementStatesProcessed.get();
    }

    /**
     * Gets the number of movement state updates skipped.
     *
     * @return Total skipped movement state updates
     */
    public static long getMovementStatesSkipped() {
        return movementStatesSkipped.get();
    }

    /**
     * Gets the number of movement ticks recorded.
     *
     * @return Total movement ticks
     */
    public static long getMovementTicks() {
        return movementTicks.get();
    }

    /**
     * Gets the cache size (legacy method - always returns 0).
     *
     * @return Cache size (always 0)
     */
    public static int getCacheSize() {
        return 0; // Not using cache anymore
    }

    /**
     * Gets the location update skip ratio as a percentage.
     *
     * @return Skip ratio (0-100)
     */
    public static double getLocationSkipRatio() {
        long processed = locationUpdatesProcessed.get();
        long skipped = locationUpdatesSkipped.get();
        long total = processed + skipped;
        if (total == 0) return 0;
        return (skipped * 100.0) / total;
    }

    /**
     * Gets the movement state skip ratio as a percentage.
     *
     * @return Skip ratio (0-100)
     */
    public static double getMovementSkipRatio() {
        long processed = movementStatesProcessed.get();
        long skipped = movementStatesSkipped.get();
        long total = processed + skipped;
        if (total == 0) return 0;
        return (skipped * 100.0) / total;
    }

    /**
     * Resets all statistics to zero.
     */
    public static void resetStats() {
        locationUpdatesProcessed.set(0);
        locationUpdatesSkipped.set(0);
        movementStatesProcessed.set(0);
        movementStatesSkipped.set(0);
        movementTicks.set(0);
    }
}
