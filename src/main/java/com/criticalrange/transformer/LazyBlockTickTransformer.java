package com.criticalrange.transformer;

import org.objectweb.asm.*;

/**
 * Lazy Block Tick Discovery Transformer
 *
 * Optimizes chunk loading by skipping the expensive BlockTickPlugin discoverTickingBlocks
 * that iterates through 32,768 blocks per section to find ticking blocks.
 *
 * Target: BlockTickPlugin.discoverTickingBlocks
 *
 * This transformer injects a static boolean flag directly into BlockTickPlugin
 * to allow runtime toggling of the optimization.
 */
public class LazyBlockTickTransformer extends BaseTransformer {

    private static final boolean DEBUG = true;

    // Static initializer to verify loading
    static {
        System.out.println("[Catalyst:LazyBlockTick] LazyBlockTickTransformer loaded!");
    }

    @Override
    public String getName() {
        return "LazyBlockTick";
    }

    @Override
    public int priority() {
        // Run early to prevent expensive processing
        return -140;
    }

    @Override
    protected boolean shouldTransform(String className) {
        // Target the BlockTickPlugin class
        return className.equals("com.hypixel.hytale.builtin.blocktick.BlockTickPlugin");
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new BlockTickPluginClassVisitor(classWriter);
    }

    /**
     * ClassVisitor that:
     * 1. Adds a static boolean field $catalystLazyBlockTick to BlockTickPlugin
     * 2. Wraps discoverTickingBlocks with the flag check
     */
    private static class BlockTickPluginClassVisitor extends ClassVisitor {

        private boolean addedField = false;

        public BlockTickPluginClassVisitor(ClassWriter classWriter) {
            super(ASM_VERSION, classWriter);
        }

        @Override
        public void visitEnd() {
            // Add a static field to control lazy tick discovery
            if (!addedField) {
                FieldVisitor fv = cv.visitField(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    "$catalystLazyBlockTick",
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

            // Target: discoverTickingBlocks(Holder, WorldChunk)I - the one that returns int
            // Must match the exact signature that returns int (ends with )I)
            // Skip the void event handler version
            if (name.equals("discoverTickingBlocks") && descriptor.endsWith(")I")) {
                if (DEBUG) {
                    System.out.println("[Catalyst:LazyBlockTick] Found discoverTickingBlocks (returns int) - injecting lazy check");
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
     * 1. Get BlockTickPlugin.$catalystLazyBlockTick
     * 2. If true, return 0 early (skip tick discovery)
     * 3. Otherwise, continue with original method
     *
     * This avoids the expensive 32,768 block iteration per section.
     */
    private static class LazyCheckMethodVisitor extends MethodVisitor {

        public LazyCheckMethodVisitor(MethodVisitor mv) {
            super(ASM_VERSION, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();

            // Inject: if (BlockTickPlugin.$catalystLazyBlockTick) return 0;

            // Get the static field from BlockTickPlugin
            mv.visitFieldInsn(Opcodes.GETSTATIC,
                "com/hypixel/hytale/builtin/blocktick/BlockTickPlugin",
                "$catalystLazyBlockTick",
                "Z"
            );

            // If false (not enabled), skip to original code
            Label continueLabel = new Label();
            mv.visitJumpInsn(Opcodes.IFEQ, continueLabel);

            // If enabled, return 0 (no ticking blocks discovered)
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitInsn(Opcodes.IRETURN);

            // Label for continuing with original method
            mv.visitLabel(continueLabel);
            mv.visitFrame(Opcodes.F_APPEND, 0, null, 0, null);

            if (DEBUG) {
                System.out.println("[Catalyst:LazyBlockTick] Injected lazy check - use /catalyst toggle ticklazy to enable");
            }
        }
    }
}
