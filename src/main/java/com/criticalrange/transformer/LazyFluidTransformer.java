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

    private static class FluidPluginClassVisitor extends ClassVisitor {

        private boolean addedField = false;

        public FluidPluginClassVisitor(ClassWriter classWriter) {
            super(ASM_VERSION, classWriter);
        }

        @Override
        public void visitEnd() {
            if (!addedField) {
                FieldVisitor fv = cv.visitField(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    "$catalystLazyFluid",
                    "Z",
                    null,
                    false  // Default: disabled for safety
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

            if (name.equals("onChunkPreProcess")) {
                return new LazyCheckMethodVisitor(mv);
            }

            return mv;
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
                "com/hypixel/hytale/builtin/fluid/FluidPlugin",
                "$catalystLazyFluid",
                "Z"
            );

            Label continueLabel = new Label();
            mv.visitJumpInsn(Opcodes.IFEQ, continueLabel);

            mv.visitInsn(Opcodes.RETURN);

            mv.visitLabel(continueLabel);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        }
    }
}
