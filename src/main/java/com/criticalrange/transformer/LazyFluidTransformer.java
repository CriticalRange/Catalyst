package com.criticalrange.transformer;

import org.objectweb.asm.*;

/**
 * Lazy Fluid Processing Transformer
 *
 * Optimizes chunk loading by skipping the expensive FluidPlugin onChunkPreProcess
 * that iterates through 32,768 blocks per section AND runs up to 100 simulation ticks.
 *
 * Target: FluidPlugin.onChunkPreProcess
 *
 * This transformer injects a static boolean flag directly into FluidPlugin
 * to allow runtime toggling of the optimization.
 */
public class LazyFluidTransformer extends BaseTransformer {

    private static final boolean DEBUG = true;

    // Static initializer to verify loading
    static {
        System.out.println("[Catalyst:LazyFluid] LazyFluidTransformer loaded!");
    }

    @Override
    public String getName() {
        return "LazyFluid";
    }

    @Override
    public int priority() {
        // Run early to prevent expensive processing
        return -130;
    }

    @Override
    protected boolean shouldTransform(String className) {
        // Target the FluidPlugin class
        return className.equals("com.hypixel.hytale.builtin.fluid.FluidPlugin");
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new FluidPluginClassVisitor(classWriter);
    }

    /**
     * ClassVisitor that:
     * 1. Adds a static boolean field $catalystLazyFluid to FluidPlugin
     * 2. Wraps onChunkPreProcess with the flag check
     */
    private static class FluidPluginClassVisitor extends ClassVisitor {

        private boolean addedField = false;

        public FluidPluginClassVisitor(ClassWriter classWriter) {
            super(ASM_VERSION, classWriter);
        }

        @Override
        public void visitEnd() {
            // Add a static field to control lazy fluid processing
            if (!addedField) {
                FieldVisitor fv = cv.visitField(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    "$catalystLazyFluid",
                    "Z",
                    null,
                    false  // Default value: false (disabled by default for safety)
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

            // Target: onChunkPreProcess(ChunkPreLoadProcessEvent)
            if (name.equals("onChunkPreProcess")) {
                if (DEBUG) {
                    System.out.println("[Catalyst:LazyFluid] Found onChunkPreProcess - injecting lazy check");
                }
                return new LazyCheckMethodVisitor(mv);
            }

            return mv;
        }
    }

    /**
     * MethodVisitor that injects a check at the start of the method.
     *
     * Injected bytecode:
     * 1. Get FluidPlugin.$catalystLazyFluid
     * 2. If true, return early (skip fluid pre-processing)
     * 3. Otherwise, continue with original method
     *
     * This avoids the expensive 32,768 block iteration per section + 100 tick simulation.
     */
    private static class LazyCheckMethodVisitor extends MethodVisitor {

        public LazyCheckMethodVisitor(MethodVisitor mv) {
            super(ASM_VERSION, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();

            // Inject: if (FluidPlugin.$catalystLazyFluid) return;

            // Get the static field from FluidPlugin
            mv.visitFieldInsn(Opcodes.GETSTATIC,
                "com/hypixel/hytale/builtin/fluid/FluidPlugin",
                "$catalystLazyFluid",
                "Z"
            );

            // If false (not enabled), skip to original code
            Label continueLabel = new Label();
            mv.visitJumpInsn(Opcodes.IFEQ, continueLabel);

            // If enabled, return early
            mv.visitInsn(Opcodes.RETURN);

            // Label for continuing with original method
            mv.visitLabel(continueLabel);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

            if (DEBUG) {
                System.out.println("[Catalyst:LazyFluid] Injected lazy check - use /catalyst toggle fluidlazy to enable");
            }
        }
    }
}
