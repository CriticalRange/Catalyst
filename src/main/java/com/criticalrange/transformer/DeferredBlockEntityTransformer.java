package com.criticalrange.transformer;

import org.objectweb.asm.*;

/**
 * Deferred Block Entity Initialization Transformer
 *
 * Optimizes chunk loading by skipping the expensive BlockModule pre-load hook.
 *
 * Target: BlockModule.onChunkPreLoadProcessEnsureBlockEntity
 */
public class DeferredBlockEntityTransformer extends BaseTransformer {

    @Override
    public String getName() {
        return "DeferredBlockEntity";
    }

    @Override
    public int priority() {
        return -150;
    }

    @Override
    protected boolean shouldTransform(String className) {
        return className.equals("com.hypixel.hytale.server.core.modules.block.BlockModule");
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new BlockModuleClassVisitor(classWriter);
    }

    private static class BlockModuleClassVisitor extends ClassVisitor {
        private boolean addedField = false;

        public BlockModuleClassVisitor(ClassWriter classWriter) {
            super(ASM_VERSION, classWriter);
        }

        @Override
        public void visitEnd() {
            if (!addedField) {
                FieldVisitor fv = cv.visitField(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    "$catalystLazyBlockEntities",
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
            if (name.equals("onChunkPreLoadProcessEnsureBlockEntity")) {
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

            // Inject: if (BlockModule.$catalystLazyBlockEntities) return;
            mv.visitFieldInsn(Opcodes.GETSTATIC,
                "com/hypixel/hytale/server/core/modules/block/BlockModule",
                "$catalystLazyBlockEntities", "Z");

            Label continueLabel = new Label();
            mv.visitJumpInsn(Opcodes.IFEQ, continueLabel);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitLabel(continueLabel);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        }
    }
}
