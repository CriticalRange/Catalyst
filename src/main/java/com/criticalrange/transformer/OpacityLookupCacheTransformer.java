package com.criticalrange.transformer;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

/**
 * Optimizes BlockType opacity lookups by injecting a cached opacity array.
 * 
 * The original code does:
 *   BlockType.getAssetMap().getAsset(blockId).getOpacity()
 * 
 * Optimization: Pre-compute opacity values into a byte[] array indexed by block ID.
 * This reduces 3 method calls to a single array access: opacityCache[blockId]
 */
public class OpacityLookupCacheTransformer extends BaseTransformer {

    private static final String TARGET_CLASS = "com/hypixel/hytale/server/core/universe/world/lighting/FloodLightCalculation";
    
    public static final String ENABLED_FIELD = "$catalystOpacityCacheEnabled";
    public static final String CACHE_FIELD = "$catalystOpacityCache";
    public static final String CACHE_INITIALIZED_FIELD = "$catalystOpacityCacheInit";

    @Override
    public String getName() {
        return "OpacityLookupCacheTransformer";
    }

    @Override
    protected boolean shouldTransform(String className) {
        return TARGET_CLASS.equals(className);
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new OpacityCacheClassVisitor(classWriter);
    }

    private class OpacityCacheClassVisitor extends ClassVisitor {
        public OpacityCacheClassVisitor(ClassWriter cw) {
            super(ASM_VERSION, cw);
        }

        @Override
        public void visitEnd() {
            // Add the enable field
            cv.visitField(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE,
                ENABLED_FIELD,
                "Z",
                null,
                Boolean.FALSE
            ).visitEnd();

            // Add the cache array field
            cv.visitField(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE,
                CACHE_FIELD,
                "[B",
                null,
                null
            ).visitEnd();

            // Add initialized flag
            cv.visitField(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE,
                CACHE_INITIALIZED_FIELD,
                "Z",
                null,
                Boolean.FALSE
            ).visitEnd();

            System.out.println("[Catalyst] Added opacity cache fields to FloodLightCalculation");
            super.visitEnd();
        }
    }
}
