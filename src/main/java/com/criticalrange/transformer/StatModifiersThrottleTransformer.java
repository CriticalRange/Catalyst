package com.criticalrange.transformer;

import org.objectweb.asm.*;

/**
 * Stat Modifiers Throttle Transformer.
 *
 * <p>Throttles entity stat modifier recalculations by adding a minimum interval
 * between recalculations. This reduces CPU usage for entities with frequently
 * changing stats.</p>
 *
 * <p>Target: StatModifiersManager.recalculateEntityStatModifiers() method</p>
 *
 * <p>Injects a tick counter check at the start of recalculation to skip
 * processing if not enough time has passed since last recalc.</p>
 */
public class StatModifiersThrottleTransformer extends BaseTransformer {

    private static final String TARGET_CLASS = "com.hypixel.hytale.server.core.entity.StatModifiersManager";
    private static final String TARGET_CLASS_INTERNAL = "com/hypixel/hytale/server/core/entity/StatModifiersManager";

    public static final String ENABLED_FIELD = "$catalystStatRecalcThrottleEnabled";
    public static final String INTERVAL_FIELD = "$catalystStatRecalcInterval";
    public static final String LAST_RECALC_FIELD = "$catalystLastRecalcTick";

    @Override
    public String getName() {
        return "StatModifiersThrottle";
    }

    @Override
    public int priority() {
        return -76;
    }

    @Override
    protected boolean shouldTransform(String className) {
        return className.equals(TARGET_CLASS);
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new StatModifiersClassVisitor(classWriter);
    }

    private static class StatModifiersClassVisitor extends ClassVisitor {

        private boolean addedFields = false;
        private boolean hasStaticInit = false;

        public StatModifiersClassVisitor(ClassWriter classWriter) {
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

            // Transform recalculateEntityStatModifiers
            if (name.equals("recalculateEntityStatModifiers")) {
                return new RecalculateMethodVisitor(mv);
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

                // Add instance last recalc tick counter
                FieldVisitor fv3 = cv.visitField(
                    Opcodes.ACC_PRIVATE,
                    LAST_RECALC_FIELD, "I", null, null);
                if (fv3 != null) fv3.visitEnd();

                addedFields = true;
            }

            if (!hasStaticInit) {
                MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
                mv.visitCode();
                mv.visitInsn(Opcodes.ICONST_0);
                mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, ENABLED_FIELD, "Z");
                mv.visitInsn(Opcodes.ICONST_5);
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
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, ENABLED_FIELD, "Z");
            mv.visitInsn(Opcodes.ICONST_5);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, INTERVAL_FIELD, "I");
        }
    }

    /**
     * Injects throttle check at the start of recalculateEntityStatModifiers.
     * Uses a simple tick counter to skip recalculations within the interval.
     */
    private static class RecalculateMethodVisitor extends MethodVisitor {

        private boolean injected = false;

        public RecalculateMethodVisitor(MethodVisitor mv) {
            super(ASM_VERSION, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();

            if (!injected) {
                Label continueLabel = new Label();

                // if (!$catalystStatRecalcThrottleEnabled) goto continue
                mv.visitFieldInsn(Opcodes.GETSTATIC, TARGET_CLASS_INTERNAL, ENABLED_FIELD, "Z");
                mv.visitJumpInsn(Opcodes.IFEQ, continueLabel);

                // Increment tick counter
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(Opcodes.GETFIELD, TARGET_CLASS_INTERNAL, LAST_RECALC_FIELD, "I");
                mv.visitInsn(Opcodes.ICONST_1);
                mv.visitInsn(Opcodes.IADD);
                mv.visitFieldInsn(Opcodes.PUTFIELD, TARGET_CLASS_INTERNAL, LAST_RECALC_FIELD, "I");

                // if (lastRecalcTick < interval) return early
                Label doRecalc = new Label();
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(Opcodes.GETFIELD, TARGET_CLASS_INTERNAL, LAST_RECALC_FIELD, "I");
                mv.visitFieldInsn(Opcodes.GETSTATIC, TARGET_CLASS_INTERNAL, INTERVAL_FIELD, "I");
                mv.visitJumpInsn(Opcodes.IF_ICMPGE, doRecalc);

                // Return early (skip this recalculation)
                mv.visitInsn(Opcodes.RETURN);

                // Reset counter and continue with normal recalculation
                mv.visitLabel(doRecalc);
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitInsn(Opcodes.ICONST_0);
                mv.visitFieldInsn(Opcodes.PUTFIELD, TARGET_CLASS_INTERNAL, LAST_RECALC_FIELD, "I");

                mv.visitLabel(continueLabel);

                injected = true;
            }
        }
    }
}
