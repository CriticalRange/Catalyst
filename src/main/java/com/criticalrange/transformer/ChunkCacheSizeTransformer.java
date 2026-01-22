package com.criticalrange.transformer;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Injects configurable cache sizes for chunk generation.
 */
public class ChunkCacheSizeTransformer extends BaseTransformer {

    private static final String TARGET_CLASS = "com/hypixel/hytale/server/worldgen/chunk/ChunkGenerator";
    
    public static final String ENABLED_FIELD = "$catalystCacheSizeEnabled";
    public static final String AUTO_FIELD = "$catalystCacheSizeAuto";
    public static final String GENERATOR_CACHE_SIZE_FIELD = "$catalystGeneratorCacheSize";
    public static final String CAVE_CACHE_SIZE_FIELD = "$catalystCaveCacheSize";
    public static final String PREFAB_CACHE_SIZE_FIELD = "$catalystPrefabCacheSize";

    @Override
    protected boolean shouldTransform(String className) {
        return TARGET_CLASS.equals(className);
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new CacheSizeClassVisitor(classWriter);
    }

    @Override
    public String getName() {
        return "ChunkCacheSizeTransformer";
    }

    private static class CacheSizeClassVisitor extends ClassVisitor {
        private boolean hasStaticInit = false;

        public CacheSizeClassVisitor(ClassVisitor cv) {
            super(BaseTransformer.ASM_VERSION, cv);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            
            FieldVisitor fv = cv.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE,
                ENABLED_FIELD, "Z", null, null);
            if (fv != null) fv.visitEnd();
            
            fv = cv.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE,
                AUTO_FIELD, "Z", null, null);
            if (fv != null) fv.visitEnd();
            
            fv = cv.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE,
                GENERATOR_CACHE_SIZE_FIELD, "I", null, null);
            if (fv != null) fv.visitEnd();
            
            fv = cv.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE,
                CAVE_CACHE_SIZE_FIELD, "I", null, null);
            if (fv != null) fv.visitEnd();
            
            fv = cv.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE,
                PREFAB_CACHE_SIZE_FIELD, "I", null, null);
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
                initFields(mv);
                mv.visitInsn(Opcodes.RETURN);
                mv.visitMaxs(2, 0);
                mv.visitEnd();
            }
            super.visitEnd();
        }

        static void initFields(MethodVisitor mv) {
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS, ENABLED_FIELD, "Z");
            
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS, AUTO_FIELD, "Z");
            
            mv.visitIntInsn(Opcodes.SIPUSH, 1024);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS, GENERATOR_CACHE_SIZE_FIELD, "I");
            
            mv.visitIntInsn(Opcodes.SIPUSH, 512);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS, CAVE_CACHE_SIZE_FIELD, "I");
            
            mv.visitIntInsn(Opcodes.SIPUSH, 256);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS, PREFAB_CACHE_SIZE_FIELD, "I");
        }
    }

    private static class StaticInitVisitor extends MethodVisitor {
        public StaticInitVisitor(MethodVisitor mv) {
            super(BaseTransformer.ASM_VERSION, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();
            CacheSizeClassVisitor.initFields(mv);
        }
    }
}
