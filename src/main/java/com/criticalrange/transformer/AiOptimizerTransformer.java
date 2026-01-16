package com.criticalrange.transformer;

import org.objectweb.asm.*;

/**
 * AI Optimization Transformer for NPCs
 *
 * Injects simple counters into AI systems.
 * Uses inline bytecode to avoid external class dependencies.
 */
public class AiOptimizerTransformer extends BaseTransformer {

    private static final boolean DEBUG = true;

    @Override
    public String getName() {
        return "AiOptimization";
    }

    @Override
    protected boolean shouldTransform(String className) {
        return className.equals("com.hypixel.hytale.server.npc.systems.BlackboardSystems$TickingSystem") ||
               className.equals("com.hypixel.hytale.server.npc.navigation.PathFollower");
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new AiClassVisitor(classWriter, className);
    }

    private static class AiClassVisitor extends ClassVisitor {

        private final String className;
        private final String fieldName;

        public AiClassVisitor(ClassWriter classWriter, String className) {
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

            // Target tick-like methods (delayedTick, tick, smoothPath)
            if ((name.contains("tick") || name.equals("smoothPath")) && descriptor.endsWith(")V")) {
                if (DEBUG) {
                    System.out.println("[Catalyst:AI] Injecting counter into " + className + "." + name);
                }
                return new AiMethodVisitor(mv, className, fieldName);
            }

            return mv;
        }
    }

    private static class AiMethodVisitor extends MethodVisitor {
        private final String className;
        private final String fieldName;

        public AiMethodVisitor(MethodVisitor mv, String className, String fieldName) {
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
