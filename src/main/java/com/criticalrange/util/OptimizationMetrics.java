package com.criticalrange.util;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reads runtime metrics from injected fields in transformed classes.
 * 
 * <p>Since transformed classes run in a different classloader, we use reflection
 * to read the counter fields that were injected by our transformers.</p>
 */
public class OptimizationMetrics {
    
    /**
     * Gets all optimization metrics by reading injected fields via reflection.
     * 
     * @return Map of metric name to value
     */
    public static Map<String, MetricData> getAllMetrics() {
        Map<String, MetricData> metrics = new LinkedHashMap<>();
        
        // Lazy loading metrics
        metrics.put("LazyBlockEntity", getLazyBlockEntityMetrics());
        metrics.put("LazyBlockTick", getLazyBlockTickMetrics());
        metrics.put("LazyFluid", getLazyFluidMetrics());
        
        // Runtime optimization metrics
        metrics.put("EntityDistance", getEntityDistanceMetrics());
        metrics.put("ChunkRate", getChunkRateMetrics());
        
        // Pathfinding metrics
        metrics.put("Pathfinding", getPathfindingMetrics());
        
        return metrics;
    }
    
    private static MetricData getEntityDistanceMetrics() {
        try {
            Class<?> clazz = Class.forName("com.hypixel.hytale.server.core.universe.Universe");
            
            boolean enabled = getBoolean(clazz, "$catalystEntityDistEnabled");
            int multiplier = getInt(clazz, "$catalystEntityViewMultiplier");
            
            return new MetricData(enabled, true)
                .add("multiplier", multiplier);
                
        } catch (ClassNotFoundException e) {
            return new MetricData(false, false).add("error", "Class not loaded");
        } catch (Exception e) {
            return new MetricData(false, true).add("error", e.getMessage());
        }
    }
    
    private static MetricData getChunkRateMetrics() {
        try {
            Class<?> clazz = Class.forName("com.hypixel.hytale.server.core.modules.entity.player.ChunkTracker");
            
            boolean enabled = getBoolean(clazz, "$catalystChunkRateEnabled");
            int chunksPerTick = getInt(clazz, "$catalystChunksPerTick");
            
            return new MetricData(enabled, true)
                .add("chunksPerTick", chunksPerTick);
                
        } catch (ClassNotFoundException e) {
            return new MetricData(false, false).add("error", "Class not loaded");
        } catch (Exception e) {
            return new MetricData(false, true).add("error", e.getMessage());
        }
    }
    
    private static MetricData getLazyBlockEntityMetrics() {
        try {
            Class<?> clazz = Class.forName("com.hypixel.hytale.server.core.modules.block.BlockModule");
            
            boolean enabled = getBoolean(clazz, "$catalystLazyBlockEntities");
            
            return new MetricData(enabled, true);
                
        } catch (ClassNotFoundException e) {
            return new MetricData(false, false).add("error", "Class not loaded");
        } catch (Exception e) {
            return new MetricData(false, true).add("error", e.getMessage());
        }
    }
    
    private static MetricData getLazyBlockTickMetrics() {
        try {
            Class<?> clazz = Class.forName("com.hypixel.hytale.builtin.blocktick.BlockTickPlugin");
            
            boolean enabled = getBoolean(clazz, "$catalystLazyBlockTick");
            
            return new MetricData(enabled, true);
                
        } catch (ClassNotFoundException e) {
            return new MetricData(false, false).add("error", "Class not loaded");
        } catch (Exception e) {
            return new MetricData(false, true).add("error", e.getMessage());
        }
    }
    
    private static MetricData getLazyFluidMetrics() {
        try {
            Class<?> clazz = Class.forName("com.hypixel.hytale.builtin.fluid.FluidPlugin");
            
            boolean enabled = getBoolean(clazz, "$catalystLazyFluid");
            
            return new MetricData(enabled, true);
                
        } catch (ClassNotFoundException e) {
            return new MetricData(false, false).add("error", "Class not loaded");
        } catch (Exception e) {
            return new MetricData(false, true).add("error", e.getMessage());
        }
    }
    
    private static MetricData getPathfindingMetrics() {
        try {
            Class<?> clazz = Class.forName("com.hypixel.hytale.server.npc.navigation.AStarBase");
            
            boolean enabled = getBoolean(clazz, "$catalystPathfindingEnabled");
            int maxPathLength = getInt(clazz, "$catalystMaxPathLength");
            int openNodesLimit = getInt(clazz, "$catalystOpenNodesLimit");
            int totalNodesLimit = getInt(clazz, "$catalystTotalNodesLimit");
            
            return new MetricData(enabled, true)
                .add("maxPathLength", maxPathLength)
                .add("openNodesLimit", openNodesLimit)
                .add("totalNodesLimit", totalNodesLimit);
                
        } catch (ClassNotFoundException e) {
            return new MetricData(false, false).add("error", "Class not loaded");
        } catch (Exception e) {
            return new MetricData(false, true).add("error", e.getMessage());
        }
    }
    
    // Helper methods for reflection
    
    private static boolean getBoolean(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getField(fieldName);
            return field.getBoolean(null);
        } catch (Exception e) {
            return false;
        }
    }
    
    private static int getInt(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getField(fieldName);
            return field.getInt(null);
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Container for metric data about an optimization.
     */
    public static class MetricData {
        public final boolean enabled;
        public final boolean transformed;
        private final Map<String, Object> values = new LinkedHashMap<>();
        
        public MetricData(boolean enabled, boolean transformed) {
            this.enabled = enabled;
            this.transformed = transformed;
        }
        
        public MetricData add(String key, Object value) {
            values.put(key, value);
            return this;
        }
        
        public Map<String, Object> getValues() {
            return values;
        }
        
        public String getStatus() {
            if (!transformed) return "NOT LOADED";
            return enabled ? "ACTIVE" : "DISABLED";
        }
    }
}
