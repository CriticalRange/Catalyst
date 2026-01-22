package com.criticalrange.transformer;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Injects configurable thread pool size for chunk generation.
 */
public class ChunkPoolSizeTransformer extends BaseTransformer {

    private static final String TARGET_CLASS = "com/hypixel/hytale/server/worldgen/chunk/ChunkGenerator";
    
    public static final String ENABLED_FIELD = "$catalystPoolSizeEnabled";
    public static final String AUTO_FIELD = "$catalystPoolSizeAuto";
    public static final String SIZE_FIELD = "$catalystPoolSize";

    @Override
    protected boolean shouldTransform(String className) {
        return TARGET_CLASS.equals(className);
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new PoolSizeClassVisitor(classWriter);
    }

    @Override
    public String getName() {
        return "ChunkPoolSizeTransformer";
    }

    private static class PoolSizeClassVisitor extends ClassVisitor {
        private boolean hasStaticInit = false;

        public PoolSizeClassVisitor(ClassVisitor cv) {
            super(BaseTransformer.ASM_VERSION, cv);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            
            // Add config fields
            FieldVisitor fv = cv.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE,
                ENABLED_FIELD, "Z", null, null);
            if (fv != null) fv.visitEnd();
            
            fv = cv.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE,
                AUTO_FIELD, "Z", null, null);
            if (fv != null) fv.visitEnd();
            
            fv = cv.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE,
                SIZE_FIELD, "I", null, null);
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
                mv.visitMaxs(1, 0);
                mv.visitEnd();
            }
            super.visitEnd();
        }

        static void initFields(MethodVisitor mv) {
            // $catalystPoolSizeEnabled = false
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS, ENABLED_FIELD, "Z");
            
            // $catalystPoolSizeAuto = true
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS, AUTO_FIELD, "Z");
            
            // $catalystPoolSize = Runtime.getRuntime().availableProcessors()
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Runtime", "getRuntime", "()Ljava/lang/Runtime;", false);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Runtime", "availableProcessors", "()I", false);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS, SIZE_FIELD, "I");
        }
    }

    private static class StaticInitVisitor extends MethodVisitor {
        public StaticInitVisitor(MethodVisitor mv) {
            super(BaseTransformer.ASM_VERSION, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();
            PoolSizeClassVisitor.initFields(mv);
        }
    }
}
