package com.criticalrange.transformer;

import org.objectweb.asm.*;

/**
 * Lazy Block Entity Initialization Transformer
 *
 * Optimizes chunk loading by skipping the expensive BlockModule pre-load hook
 * that iterates through 327,680 blocks per chunk to initialize block entities.
 *
 * Target: BlockModule.onChunkPreLoadProcessEnsureBlockEntity
 *
 * This transformer injects a static boolean flag directly into BlockModule
 * to avoid classloader issues. The flag can be toggled at runtime.
 */
public class LazyBlockEntityTransformer extends BaseTransformer {

    private static final boolean DEBUG = true;

    // Static initializer to verify loading
    static {
        System.out.println("[Catalyst:LazyBlockEntity] LazyBlockEntityTransformer loaded!");
    }

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
        // Target the BlockModule class
        // className comes in dot format (e.g., com.example.Class)
        return className.equals("com.hypixel.hytale.server.core.modules.block.BlockModule");
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new BlockModuleClassVisitor(classWriter);
    }

    /**
     * ClassVisitor that:
     * 1. Adds a static boolean field $catalystLazyBlockEntities to BlockModule
     * 2. Wraps onChunkPreLoadProcessEnsureBlockEntity with the flag check
     */
    private static class BlockModuleClassVisitor extends ClassVisitor {

        private boolean addedField = false;

        public BlockModuleClassVisitor(ClassWriter classWriter) {
            super(ASM_VERSION, classWriter);
        }

        @Override
        public void visitEnd() {
            // Add a static field to control lazy loading
            if (!addedField) {
                FieldVisitor fv = cv.visitField(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    "$catalystLazyBlockEntities",
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

            // Target: onChunkPreLoadProcessEnsureBlockEntity(ChunkPreLoadProcessEvent)
            if (name.equals("onChunkPreLoadProcessEnsureBlockEntity")) {
                if (DEBUG) {
                    System.out.println("[Catalyst:LazyBlockEntity] Found onChunkPreLoadProcessEnsureBlockEntity - injecting lazy check");
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
     * 1. Get BlockModule.$catalystLazyBlockEntities
     * 2. If true, return early (skip block entity initialization)
     * 3. Otherwise, continue with original method
     *
     * This avoids the expensive 327,680 block iteration per chunk.
     */
    private static class LazyCheckMethodVisitor extends MethodVisitor {

        public LazyCheckMethodVisitor(MethodVisitor mv) {
            super(ASM_VERSION, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();

            // Inject: if (BlockModule.$catalystLazyBlockEntities) return;

            // Get the static field from BlockModule
            mv.visitFieldInsn(Opcodes.GETSTATIC,
                "com/hypixel/hytale/server/core/modules/block/BlockModule",
                "$catalystLazyBlockEntities",
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
                System.out.println("[Catalyst:LazyBlockEntity] Injected lazy check - use /catalyst lazy to toggle");
            }
        }
    }
}
