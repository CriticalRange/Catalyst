package com.criticalrange.transformer;

import org.objectweb.asm.*;

/**
 * Lazy Fluid Processing Transformer.
 *
 * <p>Optimizes chunk loading by deferring fluid pre-simulation.
 * Instead of running up to 100 simulation ticks per chunk during pre-load,
 * fluids are processed normally during regular gameplay.</p>
 *
 * <p>Target: FluidPlugin.onChunkPreProcess</p>
 *
 * <p>This is a TRUE optimization because:</p>
 * <ul>
 *   <li>Chunk generation is MASSIVELY faster (100+ simulation ticks saved)</li>
 *   <li>Fluids still flow correctly during normal gameplay</li>
 *   <li>32,768 block iteration per section is avoided</li>
 *   <li>Complex fluid spread calculations are deferred</li>
 * </ul>
 *
 * <p><b>Note:</b> Water/lava may take a moment to start flowing in newly loaded chunks.</p>
 */
public class LazyFluidTransformer extends BaseTransformer {

    @Override
    public String getName() {
        return "LazyFluid";
    }

    @Override
    public int priority() {
        return -130;
    }

    @Override
    protected boolean shouldTransform(String className) {
        return className.equals("com.hypixel.hytale.builtin.fluid.FluidPlugin");
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new FluidPluginClassVisitor(classWriter);
    }

    private static final String TARGET_CLASS_INTERNAL = "com/hypixel/hytale/builtin/fluid/FluidPlugin";
    private static final String LAZY_FIELD = "$catalystLazyFluid";

    private static class FluidPluginClassVisitor extends ClassVisitor {

        private boolean addedField = false;
        private boolean hasStaticInit = false;

        public FluidPluginClassVisitor(ClassWriter classWriter) {
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

            if (name.equals("onChunkPreProcess")) {
                return new LazyCheckMethodVisitor(mv);
            }

            return mv;
        }

        @Override
        public void visitEnd() {
            if (!addedField) {
                // Add the field (value will be set in <clinit>)
                FieldVisitor fv = cv.visitField(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    LAZY_FIELD,
                    "Z",
                    null,
                    null
                );
                if (fv != null) {
                    fv.visitEnd();
                }
                addedField = true;
            }

            // If class has no static initializer, create one with default value (false)
            if (!hasStaticInit) {
                MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_STATIC,
                    "<clinit>",
                    "()V",
                    null,
                    null
                );
                mv.visitCode();
                // Default: false (disabled) - will be updated via reflection when plugin loads
                mv.visitInsn(Opcodes.ICONST_0);
                mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, LAZY_FIELD, "Z");
                mv.visitInsn(Opcodes.RETURN);
                mv.visitMaxs(1, 0);
                mv.visitEnd();
            }

            super.visitEnd();
        }
    }

    /**
     * Injects field initialization into existing static initializer.
     * Uses literal default value (false) - updated via reflection when plugin loads.
     */
    private static class StaticInitMethodVisitor extends MethodVisitor {

        public StaticInitMethodVisitor(MethodVisitor mv) {
            super(ASM_VERSION, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();

            // Default: false (disabled) - will be updated via reflection when plugin loads
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, LAZY_FIELD, "Z");
        }
    }

    /**
     * Injects a check to skip expensive fluid pre-simulation.
     *
     * <p>When enabled, returns immediately without running fluid simulation.
     * Fluids will process normally during regular gameplay ticks.</p>
     */
    private static class LazyCheckMethodVisitor extends MethodVisitor {

        public LazyCheckMethodVisitor(MethodVisitor mv) {
            super(ASM_VERSION, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();

            // if (FluidPlugin.$catalystLazyFluid) return;
            mv.visitFieldInsn(Opcodes.GETSTATIC,
                TARGET_CLASS_INTERNAL,
                LAZY_FIELD,
                "Z"
            );

            Label continueLabel = new Label();
            mv.visitJumpInsn(Opcodes.IFEQ, continueLabel);

            mv.visitInsn(Opcodes.RETURN);

            mv.visitLabel(continueLabel);
            // Frame computed automatically by COMPUTE_FRAMES
        }
    }
}
