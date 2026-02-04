package com.criticalrange.transformer;

import com.hypixel.hytale.plugin.early.ClassTransformer;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Manages all Catalyst class transformers and collects metrics.
 * 
 * <p>Transformers are discovered via ServiceLoader from META-INF/services.
 * This class tracks them for metrics reporting.</p>
 */
public class CatalystTransformerManager {
    
    private final List<BaseTransformer> transformers = new ArrayList<>();
    private boolean initialized = false;
    
    public CatalystTransformerManager() {
        discoverTransformers();
    }
    
    /**
     * Discovers and registers all transformers via ServiceLoader.
     */
    private void discoverTransformers() {
        if (initialized) {
            return;
        }
        
        try {
            ServiceLoader<ClassTransformer> loader = ServiceLoader.load(ClassTransformer.class);
            for (ClassTransformer transformer : loader) {
                if (transformer instanceof BaseTransformer baseTransformer) {
                    transformers.add(baseTransformer);
                }
            }
            initialized = true;
        } catch (Exception e) {
            System.err.println("[Catalyst] Failed to discover transformers: " + e.getMessage());
        }
    }
    
    /**
     * Register a transformer for metrics tracking.
     * 
     * @param transformer The transformer to register
     */
    public void registerTransformer(BaseTransformer transformer) {
        if (!transformers.contains(transformer)) {
            transformers.add(transformer);
        }
    }
    
    /**
     * Log metrics for all transformers.
     */
    public void logMetrics() {
        System.out.println("[Catalyst] ═══════════════════════════════════════════════");
        System.out.println("[Catalyst]          Transformer Metrics                    ");
        System.out.println("[Catalyst] ═══════════════════════════════════════════════");
        
        if (transformers.isEmpty()) {
            System.out.println("[Catalyst]   No transformers registered");
            System.out.println("[Catalyst] ═══════════════════════════════════════════════");
            return;
        }
        
        int totalTransformed = 0;
        double totalTime = 0;
        int activeCount = 0;
        
        for (BaseTransformer transformer : transformers) {
            int count = transformer.getTransformedCount();
            double time = transformer.getTotalTransformTimeMs();
            boolean enabled = transformer.isEnabled();
            
            totalTransformed += count;
            totalTime += time;
            if (enabled) activeCount++;
            
            String status = enabled ? "ON " : "OFF";
            if (count > 0) {
                System.out.println(String.format("[Catalyst]   [%s] %-30s %3d classes in %6.2fms",
                    status, transformer.getName(), count, time));
            } else {
                System.out.println(String.format("[Catalyst]   [%s] %-30s (no transforms)",
                    status, transformer.getName()));
            }
        }
        
        System.out.println("[Catalyst] ───────────────────────────────────────────────");
        System.out.println(String.format("[Catalyst]   Transformers: %d total, %d active",
            transformers.size(), activeCount));
        System.out.println(String.format("[Catalyst]   Classes: %d transformed in %.2fms",
            totalTransformed, totalTime));
        System.out.println("[Catalyst] ═══════════════════════════════════════════════");
    }
    
    /**
     * Get all registered transformers.
     * 
     * @return A copy of the transformer list
     */
    public List<BaseTransformer> getTransformers() {
        return new ArrayList<>(transformers);
    }
    
    /**
     * Get the count of registered transformers.
     * 
     * @return Number of registered transformers
     */
    public int getTransformerCount() {
        return transformers.size();
    }
    
    /**
     * Get the count of active (enabled) transformers.
     * 
     * @return Number of enabled transformers
     */
    public int getActiveTransformerCount() {
        return (int) transformers.stream().filter(BaseTransformer::isEnabled).count();
    }
    
    /**
     * Get total classes transformed across all transformers.
     * 
     * @return Total transformed class count
     */
    public int getTotalTransformedCount() {
        return transformers.stream().mapToInt(BaseTransformer::getTransformedCount).sum();
    }
    
    /**
     * Get total transformation time across all transformers.
     * 
     * @return Total time in milliseconds
     */
    public double getTotalTransformTimeMs() {
        return transformers.stream().mapToDouble(BaseTransformer::getTotalTransformTimeMs).sum();
    }
}
