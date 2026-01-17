package com.criticalrange.transformer;

import org.objectweb.asm.*;

/**
 * World Gen Network Optimization Transformer
 *
 * Tracks network packet flushes for metrics.
 *
 * Target: com.hypixel.hytale.server.core.modules.entity.player.PlayerConnectionFlushSystem
 */
public class WorldGenNetworkTransformer extends BaseTransformer {

    @Override
    public String getName() {
        return "WorldGenNetwork";
    }

    @Override
    public int priority() {
        return -50;
    }

    @Override
    protected boolean shouldTransform(String className) {
        return className.equals("com.hypixel.hytale.server.core.modules.entity.player.PlayerConnectionFlushSystem");
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new OptimizerClassVisitor(classWriter, className);
    }

    private static class OptimizerClassVisitor extends ClassVisitor {
        private final String className;
        private final String fieldName;

        public OptimizerClassVisitor(ClassWriter classWriter, String className) {
            super(ASM_VERSION, classWriter);
            this.className = className;
            this.fieldName = "catalyst$" + className.replace('.', '_').replace('$', '_') + "$count";
        }

        @Override
        public void visitEnd() {
            FieldVisitor fv = cv.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, fieldName, "J", null, null);
            if (fv != null) {
                fv.visitEnd();
            }
            super.visitEnd();
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            if (name.equals("tick") && descriptor.startsWith("(FI")) {
                return new FlushMethodVisitor(mv, className, fieldName);
            }
            return mv;
        }
    }

    private static class FlushMethodVisitor extends MethodVisitor {
        private final String className;
        private final String fieldName;

        public FlushMethodVisitor(MethodVisitor mv, String className, String fieldName) {
            super(ASM_VERSION, mv);
            this.className = className;
            this.fieldName = fieldName;
        }

        @Override
        public void visitCode() {
            super.visitCode();
            String internalName = className.replace('.', '/');
            mv.visitFieldInsn(Opcodes.GETSTATIC, internalName, fieldName, "J");
            mv.visitInsn(Opcodes.LCONST_1);
            mv.visitInsn(Opcodes.LADD);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, internalName, fieldName, "J");
        }
    }
}
