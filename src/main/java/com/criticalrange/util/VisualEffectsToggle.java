package com.criticalrange.util;

/**
 * Centralized toggle holder for visual effects.
 * 
 * <p>This class is used by transformers to check if particles/animations
 * should be enabled. Using a separate class avoids issues with field injection
 * into classes that may be loaded before transformers run.</p>
 */
public class VisualEffectsToggle {
    
    /** Whether particle effects are enabled */
    public static volatile boolean particlesEnabled = true;
    
    /** Whether NPC animations are enabled */
    public static volatile boolean animationsEnabled = true;
    
    /**
     * Check if particles should be spawned.
     * @return true if particles are enabled
     */
    public static boolean areParticlesEnabled() {
        return particlesEnabled;
    }
    
    /**
     * Check if animations should be played.
     * @return true if animations are enabled
     */
    public static boolean areAnimationsEnabled() {
        return animationsEnabled;
    }
}
