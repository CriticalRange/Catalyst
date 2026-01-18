package com.criticalrange.transformer;

import org.objectweb.asm.*;

/**
 * Lazy Block Tick Discovery Transformer.
 *
 * <p>Optimizes chunk loading by deferring ticking block discovery.
 * Instead of iterating 32,768 blocks per section during pre-load,
 * ticking blocks are discovered on-demand or during normal gameplay.</p>
 *
 * <p>Target: BlockTickPlugin.discoverTickingBlocks</p>
 *
 * <p>This is a TRUE optimization because:</p>
 * <ul>
 *   <li>Chunks load faster without block-by-block iteration</li>
 *   <li>Ticking blocks will still be discovered during normal gameplay</li>
 *   <li>For servers focused on building/PvP, this has minimal gameplay impact</li>
 * </ul>
 *
 * <p><b>Warning:</b> May delay crop/sapling growth in newly loaded chunks.</p>
 */
public class LazyBlockTickTransformer extends BaseTransformer {

    @Override
    public String getName() {
        return "LazyBlockTick";
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
                    "$catalystLazyBlockTick",
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

            // Target the version that returns int (block count)
            if (name.equals("discoverTickingBlocks") && descriptor.endsWith(")I")) {
                return new LazyCheckMethodVisitor(mv);
            }

            return mv;
        }
    }

    /**
     * Injects a check to skip expensive tick discovery iteration.
     *
     * <p>When enabled, returns 0 immediately (no ticking blocks discovered).
     * Blocks will still tick when discovered through normal gameplay.</p>
     */
    private static class LazyCheckMethodVisitor extends MethodVisitor {

        public LazyCheckMethodVisitor(MethodVisitor mv) {
            super(ASM_VERSION, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();

            // if (BlockTickPlugin.$catalystLazyBlockTick) return 0;
            mv.visitFieldInsn(Opcodes.GETSTATIC,
                "com/hypixel/hytale/builtin/blocktick/BlockTickPlugin",
                "$catalystLazyBlockTick",
                "Z"
            );

            Label continueLabel = new Label();
            mv.visitJumpInsn(Opcodes.IFEQ, continueLabel);

            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitInsn(Opcodes.IRETURN);

            mv.visitLabel(continueLabel);
            mv.visitFrame(Opcodes.F_APPEND, 0, null, 0, null);
        }
    }
}
