package com.criticalrange.transformer;

import org.objectweb.asm.*;

/**
 * World Notification Batch Transformer.
 *
 * <p>Tracks block update notification counts for metrics and debugging purposes.
 * True packet batching would require buffering infrastructure that's beyond the
 * scope of simple bytecode injection.</p>
 *
 * <p>The batch counter can be used to monitor update frequency and identify
 * performance hotspots in block update heavy scenarios.</p>
 *
 * <p>Target: WorldNotificationHandler.updateState() method</p>
 */
public class WorldNotificationBatchTransformer extends BaseTransformer {

    private static final String TARGET_CLASS = "com.hypixel.hytale.server.core.universe.world.WorldNotificationHandler";
    private static final String TARGET_CLASS_INTERNAL = "com/hypixel/hytale/server/core/universe/world/WorldNotificationHandler";

    public static final String ENABLED_FIELD = "$catalystBlockUpdateBatchingEnabled";
    public static final String BATCH_SIZE_FIELD = "$catalystBlockUpdateBatchSize";
    public static final String BATCH_COUNT_FIELD = "$catalystBatchCount";

    @Override
    public String getName() {
        return "WorldNotificationBatch";
    }

    @Override
    public int priority() {
        return -77;
    }

    @Override
    protected boolean shouldTransform(String className) {
        return className.equals(TARGET_CLASS);
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new WorldNotificationClassVisitor(classWriter);
    }

    private static class WorldNotificationClassVisitor extends ClassVisitor {

        private boolean addedFields = false;
        private boolean hasStaticInit = false;

        public WorldNotificationClassVisitor(ClassWriter classWriter) {
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

            // Transform updateState to add batch counting
            if (name.equals("updateState") && descriptor.contains("BlockState")) {
                return new UpdateStateMethodVisitor(mv);
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

                // Add batch size field
                FieldVisitor fv2 = cv.visitField(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE,
                    BATCH_SIZE_FIELD, "I", null, null);
                if (fv2 != null) fv2.visitEnd();

                // Add instance batch counter
                FieldVisitor fv3 = cv.visitField(
                    Opcodes.ACC_PRIVATE,
                    BATCH_COUNT_FIELD, "I", null, null);
                if (fv3 != null) fv3.visitEnd();

                addedFields = true;
            }

            if (!hasStaticInit) {
                MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
                mv.visitCode();
                mv.visitInsn(Opcodes.ICONST_0);
                mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, ENABLED_FIELD, "Z");
                mv.visitIntInsn(Opcodes.BIPUSH, 64);
                mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, BATCH_SIZE_FIELD, "I");
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
            mv.visitIntInsn(Opcodes.BIPUSH, 64);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, BATCH_SIZE_FIELD, "I");
        }
    }

    /**
     * Injects batch counting at the start of updateState.
     * When batching is enabled, tracks update count for metrics.
     */
    private static class UpdateStateMethodVisitor extends MethodVisitor {

        private boolean injected = false;

        public UpdateStateMethodVisitor(MethodVisitor mv) {
            super(ASM_VERSION, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();

            if (!injected) {
                Label continueLabel = new Label();

                // if (!$catalystBlockUpdateBatchingEnabled) goto continue
                mv.visitFieldInsn(Opcodes.GETSTATIC, TARGET_CLASS_INTERNAL, ENABLED_FIELD, "Z");
                mv.visitJumpInsn(Opcodes.IFEQ, continueLabel);

                // Increment batch counter
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(Opcodes.GETFIELD, TARGET_CLASS_INTERNAL, BATCH_COUNT_FIELD, "I");
                mv.visitInsn(Opcodes.ICONST_1);
                mv.visitInsn(Opcodes.IADD);
                mv.visitFieldInsn(Opcodes.PUTFIELD, TARGET_CLASS_INTERNAL, BATCH_COUNT_FIELD, "I");

                // Check if batch is full (for future flush logic)
                // Currently just tracks count for metrics
                mv.visitLabel(continueLabel);

                injected = true;
            }
        }
    }
}
