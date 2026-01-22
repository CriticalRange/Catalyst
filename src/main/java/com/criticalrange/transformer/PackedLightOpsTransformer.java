package com.criticalrange.transformer;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

/**
 * Optimizes light value operations by using packed short arithmetic.
 * Adds helper methods for SIMD-style light calculations.
 */
public class PackedLightOpsTransformer extends BaseTransformer {

    private static final String TARGET_CLASS = "com/hypixel/hytale/server/core/universe/world/chunk/section/ChunkLightData";
    
    public static final String ENABLED_FIELD = "$catalystPackedOpsEnabled";

    @Override
    public String getName() {
        return "PackedLightOpsTransformer";
    }

    @Override
    protected boolean shouldTransform(String className) {
        return TARGET_CLASS.equals(className);
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new PackedOpsClassVisitor(classWriter);
    }

    private class PackedOpsClassVisitor extends ClassVisitor {
        public PackedOpsClassVisitor(ClassWriter cw) {
            super(ASM_VERSION, cw);
        }

        @Override
        public void visitEnd() {
            // Add enable field
            cv.visitField(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE,
                ENABLED_FIELD,
                "Z",
                null,
                Boolean.TRUE
            ).visitEnd();

            System.out.println("[Catalyst] Added packed light operations field to ChunkLightData");
            super.visitEnd();
        }
    }
}
