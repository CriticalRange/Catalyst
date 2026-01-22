package com.criticalrange.transformer;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Injects configurable lighting batch processing to ChunkLightingManager.
 * 
 * <p>Allows processing multiple lighting sections per tick instead of one at a time.</p>
 */
public class LightingBatchTransformer extends BaseTransformer {

    private static final String TARGET_CLASS = "com/hypixel/hytale/server/core/universe/world/lighting/ChunkLightingManager";
    
    public static final String ENABLED_FIELD = "$catalystLightingBatchEnabled";
    public static final String BATCH_SIZE_FIELD = "$catalystLightingBatchSize";

    @Override
    protected boolean shouldTransform(String className) {
        return TARGET_CLASS.equals(className);
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new LightingBatchClassVisitor(classWriter);
    }

    @Override
    public String getName() {
        return "LightingBatchTransformer";
    }

    private static class LightingBatchClassVisitor extends ClassVisitor {
        private boolean hasStaticInit = false;

        public LightingBatchClassVisitor(ClassVisitor cv) {
            super(BaseTransformer.ASM_VERSION, cv);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            
            FieldVisitor fv = cv.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE,
                ENABLED_FIELD, "Z", null, null);
            if (fv != null) fv.visitEnd();
            
            fv = cv.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE,
                BATCH_SIZE_FIELD, "I", null, null);
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
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS, ENABLED_FIELD, "Z");
            
            mv.visitIntInsn(Opcodes.BIPUSH, 8);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS, BATCH_SIZE_FIELD, "I");
        }
    }

    private static class StaticInitVisitor extends MethodVisitor {
        public StaticInitVisitor(MethodVisitor mv) {
            super(BaseTransformer.ASM_VERSION, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();
            LightingBatchClassVisitor.initFields(mv);
        }
    }
}
