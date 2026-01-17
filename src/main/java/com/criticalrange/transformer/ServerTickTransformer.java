package com.criticalrange.transformer;

import org.objectweb.asm.*;

/**
 * Server Tick Tracking Transformer
 *
 * Injects tick tracking into TickingThread for TPS monitoring.
 *
 * Target: com.hypixel.hytale.server.core.util.thread.TickingThread
 */
public class ServerTickTransformer extends BaseTransformer {

    @Override
    public String getName() {
        return "ServerTick";
    }

    @Override
    public int priority() {
        return -200;
    }

    @Override
    protected boolean shouldTransform(String className) {
        return className.equals("com.hypixel.hytale.server.core.util.thread.TickingThread");
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new TickingThreadClassVisitor(classWriter);
    }

    private static class TickingThreadClassVisitor extends ClassVisitor {
        private boolean addedField = false;

        public TickingThreadClassVisitor(ClassWriter classWriter) {
            super(ASM_VERSION, classWriter);
        }

        @Override
        public void visitEnd() {
            if (!addedField) {
                FieldVisitor fv = cv.visitField(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    "catalystTickCounter", "J", null, null
                );
                if (fv != null) fv.visitEnd();
                addedField = true;
            }
            super.visitEnd();
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            if (name.equals("run") && descriptor.equals("()V")) {
                return new RunMethodVisitor(mv);
            }
            return mv;
        }
    }

    private static class RunMethodVisitor extends MethodVisitor {
        public RunMethodVisitor(MethodVisitor mv) {
            super(ASM_VERSION, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name,
                                    String descriptor, boolean isInterface) {
            if (name.equals("tick") && descriptor.equals("(F)V")) {
                // Inject: TickingThread.catalystTickCounter++;
                mv.visitFieldInsn(Opcodes.GETSTATIC, 
                    "com/hypixel/hytale/server/core/util/thread/TickingThread", 
                    "catalystTickCounter", "J");
                mv.visitInsn(Opcodes.LCONST_1);
                mv.visitInsn(Opcodes.LADD);
                mv.visitFieldInsn(Opcodes.PUTSTATIC, 
                    "com/hypixel/hytale/server/core/util/thread/TickingThread", 
                    "catalystTickCounter", "J");
            }
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }
    }
}
