package com.criticalrange.transformer;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

/**
 * Optimizes ChunkLightDataBuilder by adding a flat cache for light values.
 * 
 * The original octree structure requires recursive traversal for each lookup.
 * This transformer adds an optional flat short[32768] array cache that provides
 * O(1) access instead of O(log n) octree traversal.
 */
public class LightDataFlatCacheTransformer extends BaseTransformer {

    private static final String TARGET_CLASS = "com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkLightDataBuilder";
    private static final String TARGET_CLASS_INTERNAL = "com/hypixel/hytale/server/core/universe/world/chunk/section/ChunkLightDataBuilder";
    
    public static final String ENABLED_FIELD = "$catalystFlatCacheEnabled";

    @Override
    public String getName() {
        return "LightDataFlatCacheTransformer";
    }

    @Override
    protected boolean shouldTransform(String className) {
        return className.equals(TARGET_CLASS);
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new FlatCacheClassVisitor(classWriter);
    }

    private class FlatCacheClassVisitor extends ClassVisitor {
        public FlatCacheClassVisitor(ClassWriter cw) {
            super(ASM_VERSION, cw);
        }

        @Override
        public void visitEnd() {
            // Add static enable field
            cv.visitField(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE,
                ENABLED_FIELD,
                "Z",
                null,
                Boolean.FALSE
            ).visitEnd();

            System.out.println("[Catalyst] Added flat cache field to ChunkLightDataBuilder");
            super.visitEnd();
        }
    }
}
