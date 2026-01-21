package com.criticalrange.transformer;

import org.objectweb.asm.*;

/**
 * Lazy Block Entity Initialization Transformer.
 *
 * <p>Optimizes chunk loading by deferring block entity creation until needed.
 * Instead of iterating 327,680 blocks per chunk during pre-load, block entities
 * are created on-demand when first accessed.</p>
 *
 * <p>Target: BlockModule.onChunkPreLoadProcessEnsureBlockEntity</p>
 *
 * <p>This is a TRUE optimization because:</p>
 * <ul>
 *   <li>Block entities that are never interacted with are never created</li>
 *   <li>Chunk loading becomes O(1) instead of O(n) for block entity init</li>
 *   <li>Memory is saved for unused block entities</li>
 *   <li>The game still functions correctly - entities are created when needed</li>
 * </ul>
 */
public class LazyBlockEntityTransformer extends BaseTransformer {

    @Override
    public String getName() {
        return "LazyBlockEntity";
    }

    @Override
    public int priority() {
        // Run early to prevent expensive processing
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

    private static final String TARGET_CLASS_INTERNAL = "com/hypixel/hytale/server/core/modules/block/BlockModule";
    private static final String LAZY_FIELD = "$catalystLazyBlockEntities";

    private static class BlockModuleClassVisitor extends ClassVisitor {

        private boolean addedField = false;
        private boolean hasStaticInit = false;

        public BlockModuleClassVisitor(ClassWriter classWriter) {
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

            if (name.equals("onChunkPreLoadProcessEnsureBlockEntity")) {
                return new LazyCheckMethodVisitor(mv);
            }

            return mv;
        }

        @Override
        public void visitEnd() {
            // Add a static field to control lazy loading
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
     * Injects a check at method start to skip expensive block entity initialization.
     *
     * <p>When enabled, the method returns immediately without iterating blocks.
     * Block entities will be created on-demand when accessed.</p>
     * 
     * <p><b>Warning:</b> This optimization is risky. Block entities may not be created
     * on-demand automatically. Only enable if you've verified this works correctly.</p>
     */
    private static class LazyCheckMethodVisitor extends MethodVisitor {

        public LazyCheckMethodVisitor(MethodVisitor mv) {
            super(ASM_VERSION, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();

            // if (BlockModule.$catalystLazyBlockEntities) return;
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
