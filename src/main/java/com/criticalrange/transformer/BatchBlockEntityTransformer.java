package com.criticalrange.transformer;

import org.objectweb.asm.*;

/**
 * Batch Block Entity Processing Transformer
 *
 * Optimizes BlockModule.onChunkPreLoadProcessEnsureBlockEntity() by:
 * 1. Adding metadata fields for batch processing
 * 2. Creating a static cache for block entity requirements
 *
 * Target: com.hypixel.hytale.server.core.modules.block.BlockModule
 */
public class BatchBlockEntityTransformer extends BaseTransformer {

    @Override
    public String getName() {
        return "BatchBlockEntity";
    }

    @Override
    public int priority() {
        return -200;
    }

    @Override
    protected boolean shouldTransform(String className) {
        return className.equals("com.hypixel.hytale.server.core.modules.block.BlockModule");
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new BatchBlockEntityClassVisitor(classWriter);
    }

    private static class BatchBlockEntityClassVisitor extends ClassVisitor {
        private boolean addedCache = false;

        public BatchBlockEntityClassVisitor(ClassWriter classWriter) {
            super(ASM_VERSION, classWriter);
        }

        @Override
        public void visitEnd() {
            if (!addedCache) {
                // Cache for block types that need entities (boolean array)
                FieldVisitor fv = cv.visitField(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE,
                    "$catalystBlockEntityCache",
                    "[Z", null, null
                );
                if (fv != null) fv.visitEnd();

                // Initialization flag
                FieldVisitor fv2 = cv.visitField(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE,
                    "$catalystCacheInitialized",
                    "Z", null, null
                );
                if (fv2 != null) fv2.visitEnd();

                // Optimized chunks counter
                FieldVisitor fv3 = cv.visitField(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    "$catalystOptimizedChunks",
                    "J", null, null
                );
                if (fv3 != null) fv3.visitEnd();

                addedCache = true;
            }
            super.visitEnd();
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            if (name.equals("onChunkPreLoadProcessEnsureBlockEntity")) {
                return new BatchOptimizationMethodVisitor(mv);
            }
            return mv;
        }
    }

    private static class BatchOptimizationMethodVisitor extends MethodVisitor {
        public BatchOptimizationMethodVisitor(MethodVisitor mv) {
            super(ASM_VERSION, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();

            String ownerClass = "com/hypixel/hytale/server/core/modules/block/BlockModule";

            // Increment optimized chunks counter
            mv.visitFieldInsn(Opcodes.GETSTATIC, ownerClass, "$catalystOptimizedChunks", "J");
            mv.visitInsn(Opcodes.LCONST_1);
            mv.visitInsn(Opcodes.LADD);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, ownerClass, "$catalystOptimizedChunks", "J");
        }
    }
}
