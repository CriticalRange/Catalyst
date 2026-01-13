package com.criticalrange.transformer;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages all Catalyst class transformers and collects metrics.
 */
public class CatalystTransformerManager {
    
    private final List<BaseTransformer> transformers = new ArrayList<>();
    
    public CatalystTransformerManager() {
        // Register all transformers
        // They are loaded via ServiceLoader, but we track them here for metrics
    }
    
    /**
     * Register a transformer for metrics tracking.
     */
    public void registerTransformer(BaseTransformer transformer) {
        transformers.add(transformer);
    }
    
    /**
     * Log metrics for all transformers.
     */
    public void logMetrics() {
        System.out.println("[Catalyst] ═══════════════════════════════════════════════");
        System.out.println("[Catalyst]          Catalyst Performance Metrics           ");
        System.out.println("[Catalyst] ═══════════════════════════════════════════════");
        
        int totalTransformed = 0;
        double totalTime = 0;
        
        for (BaseTransformer transformer : transformers) {
            int count = transformer.getTransformedCount();
            double time = transformer.getTotalTransformTimeMs();
            
            totalTransformed += count;
            totalTime += time;
            
            System.out.println(String.format("[Catalyst]   %s: %d classes in %.2fms",
                transformer.getName(), count, time));
        }
        
        System.out.println("[Catalyst] ───────────────────────────────────────────────");
        System.out.println(String.format("[Catalyst]   Total: %d classes transformed in %.2fms",
            totalTransformed, totalTime));
        System.out.println("[Catalyst] ═══════════════════════════════════════════════");
    }
    
    /**
     * Get all registered transformers.
     */
    public List<BaseTransformer> getTransformers() {
        return new ArrayList<>(transformers);
    }
}
