package com.criticalrange.transformer;

import org.objectweb.asm.*;

/**
 * Lighting System Optimization Transformer
 *
 * Injects simple counters into lighting operations.
 * Uses inline bytecode to avoid external class dependencies.
 */
public class LightingOptimizationTransformer extends BaseTransformer {

    private static final boolean DEBUG = true;

    @Override
    public String getName() {
        return "LightingOptimization";
    }

    @Override
    public int priority() {
        return -75;
    }

    @Override
    protected boolean shouldTransform(String className) {
        return className.equals("com.hypixel.hytale.server.core.universe.world.lighting.ChunkLightingManager") ||
               className.equals("com.hypixel.hytale.server.core.universe.world.lighting.FloodLightCalculation");
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new LightingClassVisitor(classWriter, className);
    }

    private static class LightingClassVisitor extends ClassVisitor {

        private final String className;
        private final String fieldName;

        public LightingClassVisitor(ClassWriter classWriter, String className) {
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

            if (name.contains("propagate") || name.contains("calculate") || name.contains("update")) {
                if (DEBUG) {
                    System.out.println("[Catalyst:LightingOptimization] Injecting counter into " + className + "." + name);
                }
                return new LightMethodVisitor(mv, className, fieldName);
            }

            return mv;
        }
    }

    private static class LightMethodVisitor extends MethodVisitor {
        private final String className;
        private final String fieldName;

        public LightMethodVisitor(MethodVisitor mv, String className, String fieldName) {
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
