package com.criticalrange.transformer;

import com.hypixel.hytale.plugin.early.ClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
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

    // ========== Shared Utility Methods ==========

    /**
     * Adds a public static volatile field to a class.
     * 
     * @param cv The ClassVisitor to add the field to
     * @param name The field name
     * @param descriptor The field descriptor (e.g., "Z" for boolean, "I" for int)
     */
    protected static void addVolatileStaticField(ClassVisitor cv, String name, String descriptor) {
        FieldVisitor fv = cv.visitField(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE,
            name, descriptor, null, null);
        if (fv != null) fv.visitEnd();
    }

    /**
     * Adds a public static field (non-volatile) to a class.
     * 
     * @param cv The ClassVisitor to add the field to
     * @param name The field name
     * @param descriptor The field descriptor
     */
    protected static void addStaticField(ClassVisitor cv, String name, String descriptor) {
        FieldVisitor fv = cv.visitField(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
            name, descriptor, null, null);
        if (fv != null) fv.visitEnd();
    }

    /**
     * Adds a private instance field to a class.
     * 
     * @param cv The ClassVisitor to add the field to
     * @param name The field name
     * @param descriptor The field descriptor
     */
    protected static void addPrivateField(ClassVisitor cv, String name, String descriptor) {
        FieldVisitor fv = cv.visitField(
            Opcodes.ACC_PRIVATE,
            name, descriptor, null, null);
        if (fv != null) fv.visitEnd();
    }

    /**
     * Emits bytecode to initialize a static boolean field to false.
     * 
     * @param mv The MethodVisitor to emit to
     * @param owner The internal class name
     * @param fieldName The field name
     */
    protected static void initStaticBooleanFalse(MethodVisitor mv, String owner, String fieldName) {
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, owner, fieldName, "Z");
    }

    /**
     * Emits bytecode to initialize a static boolean field to true.
     * 
     * @param mv The MethodVisitor to emit to
     * @param owner The internal class name
     * @param fieldName The field name
     */
    protected static void initStaticBooleanTrue(MethodVisitor mv, String owner, String fieldName) {
        mv.visitInsn(Opcodes.ICONST_1);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, owner, fieldName, "Z");
    }

    /**
     * Emits bytecode to initialize a static int field.
     * 
     * @param mv The MethodVisitor to emit to
     * @param owner The internal class name
     * @param fieldName The field name
     * @param value The initial value
     */
    protected static void initStaticInt(MethodVisitor mv, String owner, String fieldName, int value) {
        if (value == -1) {
            mv.visitInsn(Opcodes.ICONST_M1);
        } else if (value >= 0 && value <= 5) {
            mv.visitInsn(Opcodes.ICONST_0 + value);
        } else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            mv.visitIntInsn(Opcodes.BIPUSH, value);
        } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            mv.visitIntInsn(Opcodes.SIPUSH, value);
        } else {
            mv.visitLdcInsn(value);
        }
        mv.visitFieldInsn(Opcodes.PUTSTATIC, owner, fieldName, "I");
    }

    /**
     * Creates a simple static initializer that calls the provided initializer.
     * 
     * @param cv The ClassVisitor
     * @param initializer A Runnable that emits the initialization bytecode
     */
    protected static void createStaticInitializer(ClassVisitor cv, java.util.function.Consumer<MethodVisitor> initializer) {
        MethodVisitor mv = cv.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
        mv.visitCode();
        initializer.accept(mv);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(2, 0);
        mv.visitEnd();
    }
}
