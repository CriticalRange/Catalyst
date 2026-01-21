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

    private static final String TARGET_CLASS_INTERNAL = "com/hypixel/hytale/builtin/blocktick/BlockTickPlugin";
    private static final String LAZY_FIELD = "$catalystLazyBlockTick";

    private static class BlockTickPluginClassVisitor extends ClassVisitor {

        private boolean addedField = false;
        private boolean hasStaticInit = false;

        public BlockTickPluginClassVisitor(ClassWriter classWriter) {
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

            // Target the version that returns int (block count)
            if (name.equals("discoverTickingBlocks") && descriptor.endsWith(")I")) {
                return new LazyCheckMethodVisitor(mv);
            }

            return mv;
        }

        @Override
        public void visitEnd() {
            if (!addedField) {
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
                TARGET_CLASS_INTERNAL,
                LAZY_FIELD,
                "Z"
            );

            Label continueLabel = new Label();
            mv.visitJumpInsn(Opcodes.IFEQ, continueLabel);

            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitInsn(Opcodes.IRETURN);

            mv.visitLabel(continueLabel);
            // Frame computed automatically by COMPUTE_FRAMES
        }
    }
}
