package com.criticalrange.transformer;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Consolidated transformer for ChunkGenerator class.
 * 
 * <p>This transformer modifies the ChunkGenerator to use configurable values for:</p>
 * <ul>
 *   <li>Thread pool size - replaces POOL_SIZE static field usage</li>
 *   <li>Cache sizes - replaces hardcoded cache sizes in constructor</li>
 *   <li>Thread priority - configures worker thread priority</li>
 * </ul>
 * 
 * <p>Actual bytecode modifications:</p>
 * <ul>
 *   <li>Intercepts GETSTATIC POOL_SIZE and replaces with configurable value</li>
 *   <li>Modifies constructor to use configurable cache sizes</li>
 * </ul>
 */
public class ChunkGeneratorTransformer extends BaseTransformer {

    private static final String TARGET_CLASS = "com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator";
    private static final String TARGET_CLASS_INTERNAL = "com/hypixel/hytale/server/worldgen/chunk/ChunkGenerator";
    
    // Pool size configuration
    public static final String POOL_SIZE_ENABLED_FIELD = "$catalystPoolSizeEnabled";
    public static final String POOL_SIZE_FIELD = "$catalystPoolSize";
    
    // Cache size configuration
    public static final String CACHE_SIZE_ENABLED_FIELD = "$catalystCacheSizeEnabled";
    public static final String GENERATOR_CACHE_SIZE_FIELD = "$catalystGeneratorCacheSize";
    public static final String CAVE_CACHE_SIZE_FIELD = "$catalystCaveCacheSize";
    public static final String PREFAB_CACHE_SIZE_FIELD = "$catalystPrefabCacheSize";
    
    // Thread priority configuration
    public static final String THREAD_PRIORITY_ENABLED_FIELD = "$catalystThreadPriorityEnabled";
    public static final String THREAD_PRIORITY_FIELD = "$catalystThreadPriority";

    @Override
    protected boolean shouldTransform(String className) {
        return className.equals(TARGET_CLASS);
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new ChunkGeneratorClassVisitor(classWriter);
    }

    @Override
    public String getName() {
        return "ChunkGeneratorTransformer";
    }

    private static class ChunkGeneratorClassVisitor extends ClassVisitor {
        private boolean hasStaticInit = false;

        public ChunkGeneratorClassVisitor(ClassVisitor cv) {
            super(BaseTransformer.ASM_VERSION, cv);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            
            // Pool size fields
            addVolatileField(POOL_SIZE_ENABLED_FIELD, "Z");
            addVolatileField(POOL_SIZE_FIELD, "I");
            
            // Cache size fields
            addVolatileField(CACHE_SIZE_ENABLED_FIELD, "Z");
            addVolatileField(GENERATOR_CACHE_SIZE_FIELD, "I");
            addVolatileField(CAVE_CACHE_SIZE_FIELD, "I");
            addVolatileField(PREFAB_CACHE_SIZE_FIELD, "I");
            
            // Thread priority fields
            addVolatileField(THREAD_PRIORITY_ENABLED_FIELD, "Z");
            addVolatileField(THREAD_PRIORITY_FIELD, "I");
        }
        
        private void addVolatileField(String name, String descriptor) {
            FieldVisitor fv = cv.visitField(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE,
                name, descriptor, null, null);
            if (fv != null) fv.visitEnd();
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            
            if ("<clinit>".equals(name)) {
                hasStaticInit = true;
                return new StaticInitVisitor(mv);
            }
            
            // Intercept constructor to modify cache sizes
            if ("<init>".equals(name)) {
                return new ConstructorVisitor(mv);
            }
            
            // Intercept any method that reads POOL_SIZE
            return new PoolSizeInterceptor(mv);
        }

        @Override
        public void visitEnd() {
            if (!hasStaticInit) {
                MethodVisitor mv = cv.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
                mv.visitCode();
                initAllFields(mv);
                mv.visitInsn(Opcodes.RETURN);
                mv.visitMaxs(2, 0);
                mv.visitEnd();
            }
            super.visitEnd();
        }

        static void initAllFields(MethodVisitor mv) {
            // Pool size defaults - disabled, default to available processors
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, POOL_SIZE_ENABLED_FIELD, "Z");
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Runtime", "getRuntime", "()Ljava/lang/Runtime;", false);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Runtime", "availableProcessors", "()I", false);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, POOL_SIZE_FIELD, "I");
            
            // Cache size defaults - disabled, use original values
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, CACHE_SIZE_ENABLED_FIELD, "Z");
            mv.visitLdcInsn(50000);  // Original generatorCache size
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, GENERATOR_CACHE_SIZE_FIELD, "I");
            mv.visitLdcInsn(5000);   // Original caveGeneratorCache size
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, CAVE_CACHE_SIZE_FIELD, "I");
            mv.visitIntInsn(Opcodes.BIPUSH, 50);  // Original uniquePrefabCache size
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, PREFAB_CACHE_SIZE_FIELD, "I");
            
            // Thread priority defaults - disabled, normal priority
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, THREAD_PRIORITY_ENABLED_FIELD, "Z");
            mv.visitIntInsn(Opcodes.BIPUSH, Thread.NORM_PRIORITY);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, THREAD_PRIORITY_FIELD, "I");
        }
    }

    private static class StaticInitVisitor extends MethodVisitor {
        public StaticInitVisitor(MethodVisitor mv) {
            super(BaseTransformer.ASM_VERSION, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();
            ChunkGeneratorClassVisitor.initAllFields(mv);
        }
    }
    
    /**
     * Intercepts GETSTATIC for POOL_SIZE and replaces with our configurable value.
     * 
     * Original: GETSTATIC ChunkGenerator.POOL_SIZE : I
     * Modified: if ($catalystPoolSizeEnabled) { GETSTATIC $catalystPoolSize } else { original }
     */
    private static class PoolSizeInterceptor extends MethodVisitor {
        public PoolSizeInterceptor(MethodVisitor mv) {
            super(BaseTransformer.ASM_VERSION, mv);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
            // Intercept reads of POOL_SIZE
            if (opcode == Opcodes.GETSTATIC && 
                owner.equals(TARGET_CLASS_INTERNAL) && 
                name.equals("POOL_SIZE") && 
                descriptor.equals("I")) {
                
                // Check if our optimization is enabled
                Label useOriginal = new Label();
                Label end = new Label();
                
                mv.visitFieldInsn(Opcodes.GETSTATIC, TARGET_CLASS_INTERNAL, POOL_SIZE_ENABLED_FIELD, "Z");
                mv.visitJumpInsn(Opcodes.IFEQ, useOriginal);
                
                // Use our configurable pool size
                mv.visitFieldInsn(Opcodes.GETSTATIC, TARGET_CLASS_INTERNAL, POOL_SIZE_FIELD, "I");
                mv.visitJumpInsn(Opcodes.GOTO, end);
                
                // Use original POOL_SIZE
                mv.visitLabel(useOriginal);
                super.visitFieldInsn(opcode, owner, name, descriptor);
                
                mv.visitLabel(end);
                return;
            }
            
            super.visitFieldInsn(opcode, owner, name, descriptor);
        }
    }
    
    /**
     * Modifies the constructor to use configurable cache sizes.
     * 
     * Targets these constructor lines:
     * - generatorCache = new ChunkGeneratorCache(..., 50000, 20L)
     * - caveGeneratorCache = new CaveGeneratorCache(..., 5000, 30L)
     * - uniquePrefabCache = new UniquePrefabCache(..., 50, 300L)
     */
    private static class ConstructorVisitor extends MethodVisitor {
        public ConstructorVisitor(MethodVisitor mv) {
            super(BaseTransformer.ASM_VERSION, mv);
        }

        @Override
        public void visitLdcInsn(Object value) {
            if (value instanceof Integer) {
                int intValue = (Integer) value;
                
                // Replace 50000 (generatorCache size)
                if (intValue == 50000) {
                    emitConfigurableCacheSize(GENERATOR_CACHE_SIZE_FIELD, intValue);
                    return;
                }
                
                // Replace 5000 (caveGeneratorCache size)
                if (intValue == 5000) {
                    emitConfigurableCacheSize(CAVE_CACHE_SIZE_FIELD, intValue);
                    return;
                }
            }
            
            super.visitLdcInsn(value);
        }
        
        @Override
        public void visitIntInsn(int opcode, int operand) {
            // Replace BIPUSH 50 (uniquePrefabCache size)
            if (opcode == Opcodes.BIPUSH && operand == 50) {
                emitConfigurableCacheSize(PREFAB_CACHE_SIZE_FIELD, operand);
                return;
            }
            
            super.visitIntInsn(opcode, operand);
        }
        
        private void emitConfigurableCacheSize(String fieldName, int originalValue) {
            Label useOriginal = new Label();
            Label end = new Label();
            
            // Check if cache size optimization is enabled
            mv.visitFieldInsn(Opcodes.GETSTATIC, TARGET_CLASS_INTERNAL, CACHE_SIZE_ENABLED_FIELD, "Z");
            mv.visitJumpInsn(Opcodes.IFEQ, useOriginal);
            
            // Use our configurable cache size
            mv.visitFieldInsn(Opcodes.GETSTATIC, TARGET_CLASS_INTERNAL, fieldName, "I");
            mv.visitJumpInsn(Opcodes.GOTO, end);
            
            // Use original value
            mv.visitLabel(useOriginal);
            if (originalValue >= -128 && originalValue <= 127) {
                mv.visitIntInsn(Opcodes.BIPUSH, originalValue);
            } else if (originalValue >= -32768 && originalValue <= 32767) {
                mv.visitIntInsn(Opcodes.SIPUSH, originalValue);
            } else {
                mv.visitLdcInsn(originalValue);
            }
            
            mv.visitLabel(end);
        }
    }
}
