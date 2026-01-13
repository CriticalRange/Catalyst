package com.criticalrange.transformer;

import com.hypixel.hytale.plugin.early.ClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Base class for all Catalyst performance transformers.
 * Provides common functionality for bytecode manipulation.
 */
public abstract class BaseTransformer implements ClassTransformer {
    
    protected static final int ASM_VERSION = Opcodes.ASM9;
    
    /** Whether this transformer is enabled */
    protected boolean enabled = true;
    
    /** Count of classes transformed by this transformer */
    protected int transformedCount = 0;
    
    /** Total time spent transforming (nanoseconds) */
    protected long totalTransformTime = 0;
    
    /**
     * Check if this transformer should process the given class.
     * @param className Binary class name (e.g., "com/example/MyClass")
     * @return true if this transformer should process the class
     */
    protected abstract boolean shouldTransform(String className);
    
    /**
     * Create a ClassVisitor to transform the class.
     * @param classWriter The ClassWriter to write transformed bytecode
     * @param className The binary class name being transformed
     * @return ClassVisitor that performs the transformation
     */
    protected abstract ClassVisitor createClassVisitor(ClassWriter classWriter, String className);
    
    /**
     * Get the name of this transformer for logging.
     */
    public abstract String getName();
    
    @Override
    public byte[] transform(String className, String classLoaderName, byte[] classBytes) {
        // Skip if disabled
        if (!enabled) {
            return classBytes;
        }
        
        // Check if we should transform this class
        if (!shouldTransform(className)) {
            return classBytes;
        }
        
        long startTime = System.nanoTime();
        
        try {
            ClassReader classReader = new ClassReader(classBytes);
            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
            ClassVisitor classVisitor = createClassVisitor(classWriter, className);
            
            classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
            
            byte[] transformedBytes = classWriter.toByteArray();
            
            // Update metrics
            transformedCount++;
            totalTransformTime += System.nanoTime() - startTime;
            
            logTransformation(className);
            
            return transformedBytes;
            
        } catch (Throwable t) {
            // On any error, return original bytes to prevent crashes
            System.err.println("[Catalyst:" + getName() + "] Error transforming " + className + ": " + t.getMessage());
            t.printStackTrace();
            return classBytes;
        }
    }
    
    @Override
    public int priority() {
        // Default priority - can be overridden
        return 0;
    }
    
    /**
     * Log that a class was transformed.
     */
    protected void logTransformation(String className) {
        System.out.println("[Catalyst:" + getName() + "] Transformed: " + className.replace('/', '.'));
    }
    
    /**
     * Get the number of classes transformed.
     */
    public int getTransformedCount() {
        return transformedCount;
    }
    
    /**
     * Get total transformation time in milliseconds.
     */
    public double getTotalTransformTimeMs() {
        return totalTransformTime / 1_000_000.0;
    }
    
    /**
     * Enable or disable this transformer.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * Check if this transformer is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }
}
