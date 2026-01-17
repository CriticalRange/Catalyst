package com.criticalrange.transformer;

import org.objectweb.asm.*;

/**
 * Deferred Block Tick Discovery Transformer
 *
 * Optimizes chunk loading by skipping the expensive BlockTickPlugin discoverTickingBlocks.
 *
 * Target: BlockTickPlugin.discoverTickingBlocks
 */
public class DeferredBlockTickTransformer extends BaseTransformer {

    @Override
    public String getName() {
        return "DeferredBlockTick";
    }

    @Override
    public int priority() {
        return -140;
    }

    @Override
    protected boolean shouldTransform(String className) {
        return className.equals("com.hypixel.hytale.builtin.blocktick.BlockTickPlugin");
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new BlockTickPluginClassVisitor(classWriter);
    }

    private static class BlockTickPluginClassVisitor extends ClassVisitor {
        private boolean addedField = false;

        public BlockTickPluginClassVisitor(ClassWriter classWriter) {
            super(ASM_VERSION, classWriter);
        }

        @Override
        public void visitEnd() {
            if (!addedField) {
                FieldVisitor fv = cv.visitField(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    "$catalystDeferredBlockTick",
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
            if (name.equals("discoverTickingBlocks") && descriptor.endsWith(")I")) {
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

            // Inject: if (BlockTickPlugin.$catalystDeferredBlockTick) return 0;
            mv.visitFieldInsn(Opcodes.GETSTATIC,
                "com/hypixel/hytale/builtin/blocktick/BlockTickPlugin",
                "$catalystDeferredBlockTick", "Z");

            Label continueLabel = new Label();
            mv.visitJumpInsn(Opcodes.IFEQ, continueLabel);
            
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitInsn(Opcodes.IRETURN);
            
            mv.visitLabel(continueLabel);
            mv.visitFrame(Opcodes.F_APPEND, 0, null, 0, null);
        }
    }
}
