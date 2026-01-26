package com.criticalrange.transformer;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Consolidated transformer for ChunkGenerator class.
 * 
 * <p>Combines all ChunkGenerator-related field injections into a single transformer
 * to avoid bytecode corruption from multiple transformers modifying the same class.</p>
 * 
 * <p>Includes fields for:</p>
 * <ul>
 *   <li>Pool size configuration</li>
 *   <li>Cache size configuration</li>
 *   <li>Tint interpolation</li>
 *   <li>Height search optimization</li>
 * </ul>
 */
public class ChunkGeneratorTransformer extends BaseTransformer {

    private static final String TARGET_CLASS = "com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator";
    private static final String TARGET_CLASS_INTERNAL = "com/hypixel/hytale/server/worldgen/chunk/ChunkGenerator";
    
    // Pool size fields
    public static final String POOL_SIZE_ENABLED_FIELD = "$catalystPoolSizeEnabled";
    public static final String POOL_SIZE_AUTO_FIELD = "$catalystPoolSizeAuto";
    public static final String POOL_SIZE_FIELD = "$catalystPoolSize";
    
    // Cache size fields
    public static final String CACHE_SIZE_ENABLED_FIELD = "$catalystCacheSizeEnabled";
    public static final String CACHE_SIZE_AUTO_FIELD = "$catalystCacheSizeAuto";
    public static final String GENERATOR_CACHE_SIZE_FIELD = "$catalystGeneratorCacheSize";
    public static final String CAVE_CACHE_SIZE_FIELD = "$catalystCaveCacheSize";
    public static final String PREFAB_CACHE_SIZE_FIELD = "$catalystPrefabCacheSize";
    
    // Tint interpolation fields
    public static final String TINT_INTERP_ENABLED_FIELD = "$catalystTintInterpEnabled";
    public static final String TINT_RADIUS_FIELD = "$catalystTintRadius";
    
    // Height search field
    public static final String HEIGHT_SEARCH_ENABLED_FIELD = "$catalystHeightSearchEnabled";

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
            addField(POOL_SIZE_ENABLED_FIELD, "Z");
            addField(POOL_SIZE_AUTO_FIELD, "Z");
            addField(POOL_SIZE_FIELD, "I");
            
            // Cache size fields
            addField(CACHE_SIZE_ENABLED_FIELD, "Z");
            addField(CACHE_SIZE_AUTO_FIELD, "Z");
            addField(GENERATOR_CACHE_SIZE_FIELD, "I");
            addField(CAVE_CACHE_SIZE_FIELD, "I");
            addField(PREFAB_CACHE_SIZE_FIELD, "I");
            
            // Tint interpolation fields
            addField(TINT_INTERP_ENABLED_FIELD, "Z");
            addField(TINT_RADIUS_FIELD, "I");
            
            // Height search field
            addField(HEIGHT_SEARCH_ENABLED_FIELD, "Z");
        }
        
        private void addField(String name, String descriptor) {
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
            
            return mv;
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
            // Pool size defaults
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, POOL_SIZE_ENABLED_FIELD, "Z");
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, POOL_SIZE_AUTO_FIELD, "Z");
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Runtime", "getRuntime", "()Ljava/lang/Runtime;", false);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Runtime", "availableProcessors", "()I", false);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, POOL_SIZE_FIELD, "I");
            
            // Cache size defaults
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, CACHE_SIZE_ENABLED_FIELD, "Z");
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, CACHE_SIZE_AUTO_FIELD, "Z");
            mv.visitIntInsn(Opcodes.SIPUSH, 1024);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, GENERATOR_CACHE_SIZE_FIELD, "I");
            mv.visitIntInsn(Opcodes.SIPUSH, 512);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, CAVE_CACHE_SIZE_FIELD, "I");
            mv.visitIntInsn(Opcodes.SIPUSH, 256);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, PREFAB_CACHE_SIZE_FIELD, "I");
            
            // Tint interpolation defaults
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, TINT_INTERP_ENABLED_FIELD, "Z");
            mv.visitIntInsn(Opcodes.BIPUSH, 4);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, TINT_RADIUS_FIELD, "I");
            
            // Height search default
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, HEIGHT_SEARCH_ENABLED_FIELD, "Z");
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
}
