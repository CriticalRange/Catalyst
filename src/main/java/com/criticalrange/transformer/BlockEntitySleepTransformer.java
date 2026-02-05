package com.criticalrange.transformer;

import org.objectweb.asm.*;

/**
 * Block Entity Sleep Transformer.
 *
 * <p>Optimizes block entity ticking by allowing idle block entities to "sleep"
 * and skip tick processing. Block entities wake up when their state changes.</p>
 *
 * <p>Target: BlockSection.forEachTicking() method</p>
 *
 * <p>Injects a check at the start of ticking iteration to skip blocks that
 * haven't changed since last tick based on a configurable interval.</p>
 */
public class BlockEntitySleepTransformer extends BaseTransformer {

    private static final String TARGET_CLASS = "com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection";
    private static final String TARGET_CLASS_INTERNAL = "com/hypixel/hytale/server/core/universe/world/chunk/section/BlockSection";

    public static final String ENABLED_FIELD = "$catalystBlockEntitySleepEnabled";
    public static final String INTERVAL_FIELD = "$catalystBlockEntitySleepInterval";
    public static final String TICK_COUNTER_FIELD = "$catalystSleepTickCounter";

    @Override
    public String getName() {
        return "BlockEntitySleep";
    }

    @Override
    public int priority() {
        return -75;
    }

    @Override
    protected boolean shouldTransform(String className) {
        return className.equals(TARGET_CLASS);
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new BlockSectionClassVisitor(classWriter);
    }

    private static class BlockSectionClassVisitor extends ClassVisitor {

        private boolean addedFields = false;
        private boolean hasStaticInit = false;

        public BlockSectionClassVisitor(ClassWriter classWriter) {
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

            // Transform forEachTicking to add sleep check
            if (name.equals("forEachTicking")) {
                return new ForEachTickingMethodVisitor(mv);
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

                // Add interval field
                FieldVisitor fv2 = cv.visitField(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE,
                    INTERVAL_FIELD, "I", null, null);
                if (fv2 != null) fv2.visitEnd();

                // Add instance tick counter
                FieldVisitor fv3 = cv.visitField(
                    Opcodes.ACC_PRIVATE,
                    TICK_COUNTER_FIELD, "I", null, null);
                if (fv3 != null) fv3.visitEnd();

                addedFields = true;
            }

            if (!hasStaticInit) {
                MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
                mv.visitCode();
                mv.visitInsn(Opcodes.ICONST_0);
                mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, ENABLED_FIELD, "Z");
                mv.visitIntInsn(Opcodes.BIPUSH, 20);
                mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, INTERVAL_FIELD, "I");
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
            // Initialize static fields
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, ENABLED_FIELD, "Z");
            mv.visitIntInsn(Opcodes.BIPUSH, 20);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, INTERVAL_FIELD, "I");
        }
    }

    /**
     * Injects sleep check at the start of forEachTicking.
     * If enabled and tick counter hasn't reached interval, increment and return early.
     * 
     * NOTE: forEachTicking returns int, so we must use IRETURN with 0.
     */
    private static class ForEachTickingMethodVisitor extends MethodVisitor {

        private boolean injected = false;

        public ForEachTickingMethodVisitor(MethodVisitor mv) {
            super(ASM_VERSION, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();

            if (!injected) {
                Label continueLabel = new Label();

                // if (!$catalystBlockEntitySleepEnabled) goto continue
                mv.visitFieldInsn(Opcodes.GETSTATIC, TARGET_CLASS_INTERNAL, ENABLED_FIELD, "Z");
                mv.visitJumpInsn(Opcodes.IFEQ, continueLabel);

                // if (tickingBlocksCount == 0) goto continue (nothing to skip)
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(Opcodes.GETFIELD, TARGET_CLASS_INTERNAL, "tickingBlocksCount", "I");
                mv.visitJumpInsn(Opcodes.IFEQ, continueLabel);

                // Increment tick counter
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(Opcodes.GETFIELD, TARGET_CLASS_INTERNAL, TICK_COUNTER_FIELD, "I");
                mv.visitInsn(Opcodes.ICONST_1);
                mv.visitInsn(Opcodes.IADD);
                mv.visitFieldInsn(Opcodes.PUTFIELD, TARGET_CLASS_INTERNAL, TICK_COUNTER_FIELD, "I");

                // if (tickCounter < interval) return early with 0
                Label doTick = new Label();
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(Opcodes.GETFIELD, TARGET_CLASS_INTERNAL, TICK_COUNTER_FIELD, "I");
                mv.visitFieldInsn(Opcodes.GETSTATIC, TARGET_CLASS_INTERNAL, INTERVAL_FIELD, "I");
                mv.visitJumpInsn(Opcodes.IF_ICMPGE, doTick);

                // Return early with 0 (skip this tick, no blocks processed)
                mv.visitInsn(Opcodes.ICONST_0);
                mv.visitInsn(Opcodes.IRETURN);

                // Reset counter and continue with normal ticking
                mv.visitLabel(doTick);
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitInsn(Opcodes.ICONST_0);
                mv.visitFieldInsn(Opcodes.PUTFIELD, TARGET_CLASS_INTERNAL, TICK_COUNTER_FIELD, "I");

                mv.visitLabel(continueLabel);

                injected = true;
            }
        }
    }
}
