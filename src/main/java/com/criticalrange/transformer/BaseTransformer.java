package com.criticalrange.transformer;

import com.hypixel.hytale.plugin.early.ClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Base class for all Catalyst performance transformers.
 *
 * <p>Provides common functionality for bytecode manipulation including
 * transformation tracking, error handling, and metrics collection.</p>
 *
 * <p>All transformers should extend this class and implement:</p>
 * <ul>
 *   <li>{@link #shouldTransform(String)} - Determine if a class should be transformed</li>
 *   <li>{@link #createClassVisitor(ClassWriter, String)} - Create the visitor for transformation</li>
 *   <li>{@link #getName()} - Return a human-readable name for logging</li>
 * </ul>
 */
public abstract class BaseTransformer implements ClassTransformer {

    /** ASM version to use for bytecode generation (ASM9 for Java 11+) */
    protected static final int ASM_VERSION = Opcodes.ASM9;

    /** Debug mode - set to true for verbose logging */
    protected static final boolean DEBUG = false;

    /** Whether this transformer is enabled */
    protected boolean enabled = true;

    /** Count of classes transformed by this transformer */
    protected int transformedCount = 0;

    /** Total time spent transforming (in nanoseconds) */
    protected long totalTransformTime = 0;

    /**
     * Checks if this transformer should process the given class.
     *
     * @param className Binary class name (e.g., "com/example/MyClass")
     * @return true if this transformer should process the class
     */
    protected abstract boolean shouldTransform(String className);

    /**
     * Creates a ClassVisitor to transform the class.
     *
     * @param classWriter The ClassWriter to write transformed bytecode
     * @param className The binary class name being transformed
     * @return ClassVisitor that performs the transformation
     */
    protected abstract ClassVisitor createClassVisitor(ClassWriter classWriter, String className);

    /**
     * Gets the name of this transformer for logging.
     *
     * @return Human-readable name of this transformer
     */
    public abstract String getName();

    @Override
    public byte[] transform(String className, String classLoaderName, byte[] classBytes) {
        // Debug: Log every class being checked (limited to avoid spam)


        // Skip if disabled
        if (!enabled) {
            return classBytes;
        }

        // Check if we should transform this class
        if (!shouldTransform(className)) {
            return classBytes;
        }

        if (DEBUG) {
            System.out.println("[Catalyst:" + getName() + "] Transforming class: " + className);
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
     * Logs that a class was transformed.
     *
     * @param className The binary class name that was transformed
     */
    protected void logTransformation(String className) {
            // Only log successful transformations if debug is enabled
            if (DEBUG) {
                System.out.println("[Catalyst] " + getName() + " applied to " + className.replace('/', '.'));
            }
    }

    /**
     * Gets the number of classes transformed by this transformer.
     *
     * @return The count of transformed classes
     */
    public int getTransformedCount() {
        return transformedCount;
    }

    /**
     * Gets the total time spent transforming (in milliseconds).
     *
     * @return Total transformation time in milliseconds
     */
    public double getTotalTransformTimeMs() {
        return totalTransformTime / 1_000_000.0;
    }

    /**
     * Enables or disables this transformer.
     *
     * @param enabled true to enable, false to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Checks if this transformer is enabled.
     *
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }
}
