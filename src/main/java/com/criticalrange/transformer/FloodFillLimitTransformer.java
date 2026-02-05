package com.criticalrange.transformer;

import org.objectweb.asm.*;

/**
 * Flood Fill Limit Transformer.
 *
 * <p>Limits the depth of flood fill algorithm used for spawn position selection.
 * Prevents lag spikes in complex terrain by capping iterations.</p>
 *
 * <p>Target: FloodFillPositionSelector.floodFill() method</p>
 *
 * <p>Injects an iteration counter that breaks out of the flood fill loop
 * when the maximum is reached.</p>
 */
public class FloodFillLimitTransformer extends BaseTransformer {

    private static final String TARGET_CLASS = "com.hypixel.hytale.server.spawning.util.FloodFillPositionSelector";
    private static final String TARGET_CLASS_INTERNAL = "com/hypixel/hytale/server/spawning/util/FloodFillPositionSelector";

    public static final String ENABLED_FIELD = "$catalystFloodFillLimitEnabled";
    public static final String MAX_ITERATIONS_FIELD = "$catalystFloodFillMaxIterations";

    @Override
    public String getName() {
        return "FloodFillLimit";
    }

    @Override
    public int priority() {
        return -78;
    }

    @Override
    protected boolean shouldTransform(String className) {
        return className.equals(TARGET_CLASS);
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new FloodFillClassVisitor(classWriter);
    }

    private static class FloodFillClassVisitor extends ClassVisitor {

        private boolean addedFields = false;
        private boolean hasStaticInit = false;

        public FloodFillClassVisitor(ClassWriter classWriter) {
            super(ASM_VERSION, classWriter);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

            if (name.equals("<clinit>")) {
                hasStaticInit = true;
                return new StaticInitMethodVisitor(mv);
            }

            // Transform floodFill method
            if (name.equals("floodFill")) {
                return new FloodFillMethodVisitor(mv);
            }

            return mv;
        }

        @Override
        public void visitEnd() {
            if (!addedFields) {
                // Add enabled flag
                FieldVisitor fv1 = cv.visitField(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE,
                    ENABLED_FIELD, "Z", null, null);
                if (fv1 != null) fv1.visitEnd();

                // Add max iterations field
                FieldVisitor fv2 = cv.visitField(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE,
                    MAX_ITERATIONS_FIELD, "I", null, null);
                if (fv2 != null) fv2.visitEnd();

                addedFields = true;
            }

            if (!hasStaticInit) {
                MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
                mv.visitCode();
                mv.visitInsn(Opcodes.ICONST_0);
                mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, ENABLED_FIELD, "Z");
                mv.visitLdcInsn(5000);
                mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, MAX_ITERATIONS_FIELD, "I");
                mv.visitInsn(Opcodes.RETURN);
                mv.visitMaxs(1, 0);
                mv.visitEnd();
            }

            super.visitEnd();
        }
    }

    private static class StaticInitMethodVisitor extends MethodVisitor {

        public StaticInitMethodVisitor(MethodVisitor mv) {
            super(ASM_VERSION, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, ENABLED_FIELD, "Z");
            mv.visitLdcInsn(5000);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, MAX_ITERATIONS_FIELD, "I");
        }
    }

    /**
     * Injects iteration limit check in the floodFill while loop.
     * 
     * Strategy: Add an iteration counter at method start, and after each
     * floodFillQueue.poll() call, increment and check against max iterations.
     * If exceeded, clear the queue and return early.
     * 
     * The floodFill method signature is:
     * private void floodFill(int x, int y, int z, int dx, int dz, FloodFillEntryPoolSimple pool)
     * Local vars: 0=this, 1=x, 2=y, 3=z, 4=dx, 5=dz, 6=pool, 7+=internal
     * We'll use a high local var slot (20) for our counter to avoid conflicts.
     */
    private static class FloodFillMethodVisitor extends MethodVisitor {

        private static final int ITERATION_COUNTER_SLOT = 20;
        private boolean injectedInit = false;
        private boolean afterPoll = false;

        public FloodFillMethodVisitor(MethodVisitor mv) {
            super(ASM_VERSION, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();

            if (!injectedInit) {
                // Initialize iteration counter to 0 at method start
                mv.visitInsn(Opcodes.ICONST_0);
                mv.visitVarInsn(Opcodes.ISTORE, ITERATION_COUNTER_SLOT);
                injectedInit = true;
            }
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            
            // After floodFillQueue.poll() call, inject iteration limit check
            // poll() returns the next entry from the queue - this is called once per iteration
            if (name.equals("poll") && owner.contains("Deque")) {
                afterPoll = true;
            }
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            super.visitTypeInsn(opcode, type);
            
            // After CHECKCAST following poll(), inject our iteration check
            // The bytecode is: poll() -> CHECKCAST "[I" -> ASTORE
            if (afterPoll && opcode == Opcodes.CHECKCAST && type.equals("[I")) {
                afterPoll = false;
                
                // At this point, the polled int[] is on the stack
                // We need to inject our check AFTER the ASTORE that follows
                // So we'll do it in visitVarInsn for ASTORE
            }
        }

        @Override
        public void visitVarInsn(int opcode, int var) {
            super.visitVarInsn(opcode, var);
            
            // After ASTORE of the polled entry (var 10 based on bytecode analysis),
            // inject iteration limit check
            if (afterPoll && opcode == Opcodes.ASTORE) {
                afterPoll = false;
                injectIterationCheck();
            }
        }

        private void injectIterationCheck() {
            Label continueLoop = new Label();
            
            // if (!$catalystFloodFillLimitEnabled) goto continueLoop
            mv.visitFieldInsn(Opcodes.GETSTATIC, TARGET_CLASS_INTERNAL, ENABLED_FIELD, "Z");
            mv.visitJumpInsn(Opcodes.IFEQ, continueLoop);
            
            // iterationCounter++
            mv.visitIincInsn(ITERATION_COUNTER_SLOT, 1);
            
            // if (iterationCounter < maxIterations) goto continueLoop
            mv.visitVarInsn(Opcodes.ILOAD, ITERATION_COUNTER_SLOT);
            mv.visitFieldInsn(Opcodes.GETSTATIC, TARGET_CLASS_INTERNAL, MAX_ITERATIONS_FIELD, "I");
            mv.visitJumpInsn(Opcodes.IF_ICMPLT, continueLoop);
            
            // Max iterations reached - clear the queue and return
            // this.floodFillQueue.clear()
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, TARGET_CLASS_INTERNAL, "floodFillQueue", "Ljava/util/Deque;");
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Deque", "clear", "()V", true);
            
            // return (void method)
            mv.visitInsn(Opcodes.RETURN);
            
            mv.visitLabel(continueLoop);
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            // Ensure we have enough local variable slots
            super.visitMaxs(Math.max(maxStack, 4), Math.max(maxLocals, ITERATION_COUNTER_SLOT + 1));
        }
    }
}
