package com.criticalrange.transformer;

import org.objectweb.asm.*;

/**
 * Server Tick Tracking Transformer
 *
 * Injects simple tick tracking into TickingThread without external dependencies.
 * This avoids classloader issues with early plugins.
 *
 * Target Classes:
 * - com.hypixel.hytale.server.core.util.thread.TickingThread (the main tick loop)
 *
 * Instead of calling external classes, we inject a static field increment directly.
 * The mod can then read this field through reflection to track TPS.
 */
public class ServerTickTransformer extends BaseTransformer {

    private static final boolean DEBUG = true;

    // Static initializer to verify loading
    static {
        System.out.println("[Catalyst:ServerTick] ServerTickTransformer loaded!");
    }

    @Override
    public String getName() {
        return "ServerTickTracker";
    }

    @Override
    public int priority() {
        // Run first since other transformers depend on tick tracking
        return -200;
    }

    @Override
    protected boolean shouldTransform(String className) {
        // Target TickingThread which contains the main tick loop
        // className comes in dot format (e.g., com.example.Class)
        return className.equals("com.hypixel.hytale.server.core.util.thread.TickingThread");
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new TickingThreadClassVisitor(classWriter);
    }

    /**
     * Adds a static tick counter field to TickingThread and increments it on each tick.
     */
    private static class TickingThreadClassVisitor extends ClassVisitor {

        private boolean addedField = false;

        public TickingThreadClassVisitor(ClassWriter classWriter) {
            super(ASM_VERSION, classWriter);
        }

        @Override
        public void visitEnd() {
            // Add a static field to track tick count if we haven't already
            if (!addedField) {
                FieldVisitor fv = cv.visitField(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    "catalystTickCounter",
                    "J",
                    null,
                    null
                );
                if (fv != null) {
                    fv.visitEnd();
                }
                addedField = true;
            }
            super.visitEnd();
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

            // Target the run() method - main loop entry point
            if (name.equals("run") && descriptor.equals("()V")) {
                if (DEBUG) {
                    System.out.println("[Catalyst:ServerTick] Found TickingThread.run() - injecting tick tracker");
                }
                return new RunMethodVisitor(mv);
            }

            return mv;
        }
    }

    /**
     * Injects a simple field increment before each tick() call.
     * Uses GETSTATIC to load the field, LCONST_1 to add 1, and PUTSTATIC to store it back.
     */
    private static class RunMethodVisitor extends MethodVisitor {

        public RunMethodVisitor(MethodVisitor mv) {
            super(ASM_VERSION, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name,
                                    String descriptor, boolean isInterface) {
            // Look for calls to tick(float) method
            // We only want to track the abstract tick(float) call in TickingThread.run(),
            // not other tick() calls that might be in subclasses.
            if (name.equals("tick") && descriptor.equals("(F)V")) {
                // Inject: TickingThread.catalystTickCounter++;
                // This uses purely bytecode operations with no external dependencies

                // Get the static field
                mv.visitFieldInsn(Opcodes.GETSTATIC, "com/hypixel/hytale/server/core/util/thread/TickingThread", "catalystTickCounter", "J");

                // Load 1
                mv.visitInsn(Opcodes.LCONST_1);

                // Add
                mv.visitInsn(Opcodes.LADD);

                // Put back to static field
                mv.visitFieldInsn(Opcodes.PUTSTATIC, "com/hypixel/hytale/server/core/util/thread/TickingThread", "catalystTickCounter", "J");

                if (DEBUG) {
                    System.out.println("[Catalyst:ServerTick] Injected tick counter before call to " + owner + ".tick()");
                }
            }

            // Then visit the original instruction
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }
    }
}
