package com.criticalrange.transformer;

import org.objectweb.asm.*;

/**
 * Deferred Fluid Processing Transformer
 *
 * Optimizes chunk loading by skipping the expensive FluidPlugin onChunkPreProcess.
 *
 * Target: FluidPlugin.onChunkPreProcess
 */
public class DeferredFluidTransformer extends BaseTransformer {

    @Override
    public String getName() {
        return "DeferredFluid";
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
                    "$catalystDeferredFluid",
                    "Z", null, false
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
            if (name.equals("onChunkPreProcess")) {
                return new LazyCheckMethodVisitor(mv);
            }
            return mv;
        }
    }

    private static class LazyCheckMethodVisitor extends MethodVisitor {
        public LazyCheckMethodVisitor(MethodVisitor mv) {
            super(ASM_VERSION, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();

            // Inject: if (FluidPlugin.$catalystDeferredFluid) return;
            mv.visitFieldInsn(Opcodes.GETSTATIC,
                "com/hypixel/hytale/builtin/fluid/FluidPlugin",
                "$catalystDeferredFluid", "Z");

            Label continueLabel = new Label();
            mv.visitJumpInsn(Opcodes.IFEQ, continueLabel);
            
            mv.visitInsn(Opcodes.RETURN);
            
            mv.visitLabel(continueLabel);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        }
    }
}
