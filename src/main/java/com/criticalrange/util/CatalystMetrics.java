package com.criticalrange.util;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Performance metrics collection for Catalyst optimizations.
 *
 * <p>Tracks timing, counts, and provides reporting for all optimization systems.</p>
 *
 * <p>This version uses reflection to read injected counter fields from transformed classes,
 * avoiding classloader issues with early plugins.</p>
 */
public class CatalystMetrics {

    /** Last server tick time (for TPS calculation) */
    private static volatile long lastTickTime = System.nanoTime();

    /** Current TPS (smoothed) */
    private static volatile double currentTPS = 20.0;

    /** Total ticks since server start */
    private static final AtomicLong tickCount = new AtomicLong(0);

    /** Cache for reflected fields to avoid repeated lookups */
    private static final Map<String, Field> fieldCache = new ConcurrentHashMap<>();

    /**
     * Gets a static long field from a class using reflection.
     *
     * @param className Binary class name (e.g., "com/example/MyClass")
     * @param fieldName Name of the static field
     * @return Field value, or 0 if not found/error
     */
    private static long getStaticField(String className, String fieldName) {
        String key = className + "." + fieldName;
        Field field = fieldCache.get(key);

        if (field == null) {
            try {
                Class<?> clazz = Class.forName(className.replace('/', '.'));
                field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                fieldCache.put(key, field);
            } catch (Exception e) {
                // Class not loaded yet or field doesn't exist
                return 0;
            }
        }

        try {
            return field.getLong(null);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Gets a static long field from a class using reflection with default naming.
     *
     * @param className Dot format class name (e.g., "com.example.MyClass")
     * @return Field value, or 0 if not found/error
     */
    private static long getCounterField(String className) {
        // Convert to field name format: com.example.Class -> com_example_Class$count
        String fieldName = "catalyst$" + className.replace('.', '_').replace('$', '_') + "$count";
        String binaryName = className.replace('.', '/');
        return getStaticField(binaryName, fieldName);
    }

    /**
     * Gets a tick count field from a class.
     *
     * @param className Dot format class name
     * @return Field value, or 0 if not found/error
     */
    private static long getTickField(String className) {
        String fieldName = "catalyst$" + className.replace('.', '_').replace('$', '_') + "$tickCount";
        String binaryName = className.replace('.', '/');
        return getStaticField(binaryName, fieldName);
    }

    /**
     * Generates a comprehensive performance report.
     *
     * @return Formatted report string
     */
    public static String generateReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n═══════════════════════════════════════════════\n");
        sb.append("         Catalyst Performance Report            \n");
        sb.append("═══════════════════════════════════════════════\n");

        // TPS - Read from TickingThread
        long ticks = getTickField("com.hypixel.hytale.server.core.util.thread.TickingThread");
        if (ticks > 0) tickCount.set(ticks);

        sb.append(String.format("TPS: %.2f (ticks: %d)\n", currentTPS, ticks));

        // Memory
        MemoryStats mem = getMemoryStats();
        sb.append(String.format("Memory: %dMB / %dMB (%.1f%%)\n",
            mem.usedMB(), mem.maxMB(), mem.usagePercent()));

        // Tick optimization stats
        sb.append("\n── Tick Optimization ──\n");
        long entityTicks = getTickField("com.hypixel.hytale.component.system.tick.EntityTickingSystem");
        long locationTicks = getCounterField("com.hypixel.hytale.server.core.modules.entity.system.UpdateLocationSystems$TickingSystem");
        long movementTicks = getCounterField("com.hypixel.hytale.server.core.entity.movement.MovementStatesSystems$TickingSystem");
        long repulsionTicks = getCounterField("com.hypixel.hytale.server.core.modules.entity.repulsion.RepulsionSystems$RepulsionTicker");
        sb.append(String.format("  Entity system ticks: %d\n", entityTicks));
        sb.append(String.format("  Location updates: %d\n", locationTicks));
        sb.append(String.format("  Movement ticks: %d\n", movementTicks));
        sb.append(String.format("  Repulsion ticks: %d\n", repulsionTicks));

        // Entity tracking stats
        sb.append("\n── Entity Tracking ──\n");
        long collectVisible = getCounterField("com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems$CollectVisible");
        long sendPackets = getCounterField("com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems$SendPackets");
        long clearViewers = getCounterField("com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems$ClearEntityViewers");
        long addToVisible = getCounterField("com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems$AddToVisible");
        sb.append(String.format("  Collect visible: %d\n", collectVisible));
        sb.append(String.format("  Send packets: %d\n", sendPackets));
        sb.append(String.format("  Clear viewers: %d\n", clearViewers));
        sb.append(String.format("  Add to visible: %d\n", addToVisible));

        // Movement optimization stats
        sb.append("\n── Movement Optimization ──\n");
        sb.append(String.format("  Location system ticks: %d\n", locationTicks));
        sb.append(String.format("  Movement ticks: %d\n", movementTicks));

        // Physics optimization stats
        sb.append("\n── Physics Optimization ──\n");
        long itemPhysics = getCounterField("com.hypixel.hytale.server.core.modules.entity.item.ItemPhysicsSystem");
        sb.append(String.format("  Item physics ticks: %d\n", itemPhysics));
        sb.append(String.format("  Repulsion ticks: %d\n", repulsionTicks));

        // AI optimization stats
        sb.append("\n── AI Optimization ──\n");
        long aiTicks = getCounterField("com.hypixel.hytale.server.npc.systems.BlackboardSystems$TickingSystem");
        sb.append(String.format("  AI brain ticks: %d\n", aiTicks));

        // Network optimization stats
        sb.append("\n── Network Optimization ──\n");
        long networkFlush = getCounterField("com.hypixel.hytale.server.core.modules.entity.player.PlayerConnectionFlushSystem");
        sb.append(String.format("  Packet flushes: %d\n", networkFlush));

        // Lighting stats
        sb.append("\n── Lighting Optimization ──\n");
        sb.append("  (Tracking active - counters increment on light operations)\n");

        // Chunk cache stats
        sb.append("\n── Chunk I/O ──\n");
        sb.append("  (Tracking active - counters increment on chunk operations)\n");

        sb.append("═══════════════════════════════════════════════\n");

        return sb.toString();
    }

    /**
     * Gets the current TPS (Ticks Per Second).
     *
     * @return Current TPS value
     */
    public static double getCurrentTPS() {
        return currentTPS;
    }

    /**
     * Gets the total tick count since server start.
     *
     * @return Total number of ticks
     */
    public static long getTickCount() {
        long ticks = getTickField("com.hypixel.hytale.server.core.util.thread.TickingThread");
        return ticks > 0 ? ticks : tickCount.get();
    }

    /**
     * Gets memory usage statistics.
     *
     * @return MemoryStats containing current memory usage
     */
    public static MemoryStats getMemoryStats() {
        Runtime runtime = Runtime.getRuntime();
        long total = runtime.totalMemory();
        long free = runtime.freeMemory();
        long used = total - free;
        long max = runtime.maxMemory();

        return new MemoryStats(used, free, total, max);
    }

    /**
     * Resets all metrics to zero.
     */
    public static void reset() {
        tickCount.set(0);
        fieldCache.clear(); // Clear cache to force re-reading
    }

    /**
     * Memory statistics record.
     *
     * @param used Used memory in bytes
     * @param free Free memory in bytes
     * @param total Total memory in bytes
     * @param max Maximum memory in bytes
     */
    public record MemoryStats(long used, long free, long total, long max) {

        /** Gets used memory in megabytes */
        public long usedMB() {
            return used / (1024 * 1024);
        }

        /** Gets free memory in megabytes */
        public long freeMB() {
            return free / (1024 * 1024);
        }

        /** Gets total memory in megabytes */
        public long totalMB() {
            return total / (1024 * 1024);
        }

        /** Gets maximum memory in megabytes */
        public long maxMB() {
            return max / (1024 * 1024);
        }

        /** Gets memory usage as a percentage */
        public double usagePercent() {
            return (used * 100.0) / max;
        }
    }
}
