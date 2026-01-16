package com.criticalrange.transformer;

import org.objectweb.asm.*;

/**
 * Entity Tracking Optimization Transformer
 *
 * Injects simple counters into entity tracker operations.
 * Uses inline bytecode to avoid external class dependencies.
 *
 * Target Classes:
 * - EntityTrackerSystems$CollectVisible (visibility collection)
 * - EntityTrackerSystems$SendPackets (network updates)
 * - EntityTrackerSystems$ClearEntityViewers (cleanup operations)
 * - EntityTrackerSystems$AddToVisible (visibility additions)
 */
public class EntityTrackerTransformer extends BaseTransformer {

    private static final boolean DEBUG = true;

    @Override
    public String getName() {
        return "EntityTracker";
    }

    @Override
    public int priority() {
        return -50;
    }

    @Override
    protected boolean shouldTransform(String className) {
        // className comes in dot format (e.g., com.example.Class)
        return className.equals("com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems$CollectVisible") ||
               className.equals("com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems$SendPackets") ||
               className.equals("com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems$ClearEntityViewers") ||
               className.equals("com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems$AddToVisible");
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new EntityTrackerClassVisitor(classWriter, className);
    }

    private static class EntityTrackerClassVisitor extends ClassVisitor {

        private final String className;
        private final String fieldName;

        public EntityTrackerClassVisitor(ClassWriter classWriter, String className) {
            super(ASM_VERSION, classWriter);
            this.className = className;
            this.fieldName = "catalyst$" + className.replace('.', '_').replace('$', '_') + "$count";
        }

        @Override
        public void visitEnd() {
            // Add a static field to track operation count
            FieldVisitor fv = cv.visitField(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                fieldName,
                "J",
                null,
                null
            );
            if (fv != null) {
                fv.visitEnd();
            }
            super.visitEnd();
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

            // Target tick methods - signature: tick(float, int, ArchetypeChunk, Store, CommandBuffer)
            if (name.equals("tick") && descriptor.startsWith("(FI")) {
                if (DEBUG) {
                    System.out.println("[Catalyst:EntityTracker] Injecting counter into " + className);
                }
                return new TrackerTickMethodVisitor(mv, className, fieldName);
            }

            return mv;
        }
    }

    /**
     * Injects a simple counter increment at the start of each tracker tick().
     */
    private static class TrackerTickMethodVisitor extends MethodVisitor {

        private final String className;
        private final String fieldName;

        public TrackerTickMethodVisitor(MethodVisitor mv, String className, String fieldName) {
            super(ASM_VERSION, mv);
            this.className = className;
            this.fieldName = fieldName;
        }

        @Override
        public void visitCode() {
            super.visitCode();

            // Convert className to internal form (dots to slashes)
            String internalName = className.replace('.', '/');

            // Inject: <ClassName>.fieldName++;
            mv.visitFieldInsn(Opcodes.GETSTATIC, internalName, fieldName, "J");
            mv.visitInsn(Opcodes.LCONST_1);
            mv.visitInsn(Opcodes.LADD);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, internalName, fieldName, "J");
        }
    }
}
