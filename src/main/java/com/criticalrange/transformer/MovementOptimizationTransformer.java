package com.criticalrange.transformer;

import org.objectweb.asm.*;

/**
 * Movement and Location Update Optimization Transformer
 *
 * Injects simple counters into movement systems.
 * Uses inline bytecode to avoid external class dependencies.
 */
public class MovementOptimizationTransformer extends BaseTransformer {

    private static final boolean DEBUG = true;

    @Override
    public String getName() {
        return "MovementOptimization";
    }

    @Override
    public int priority() {
        return -90;
    }

    @Override
    protected boolean shouldTransform(String className) {
        return className.equals("com.hypixel.hytale.server.core.modules.entity.system.UpdateLocationSystems$TickingSystem") ||
               className.equals("com.hypixel.hytale.server.core.entity.movement.MovementStatesSystems$TickingSystem");
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new MovementClassVisitor(classWriter, className);
    }

    private static class MovementClassVisitor extends ClassVisitor {

        private final String className;
        private final String fieldName;

        public MovementClassVisitor(ClassWriter classWriter, String className) {
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
                if (DEBUG) {
                    System.out.println("[Catalyst:MovementOptimization] Injecting counter into " + className);
                }
                return new MovementMethodVisitor(mv, className, fieldName);
            }

            return mv;
        }
    }

    private static class MovementMethodVisitor extends MethodVisitor {
        private final String className;
        private final String fieldName;

        public MovementMethodVisitor(MethodVisitor mv, String className, String fieldName) {
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
