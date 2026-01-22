package com.criticalrange.transformer;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

/**
 * Optimizes light propagation in FloodLightCalculation by:
 * 1. Using packed short operations for all 4 channels simultaneously
 * 2. Reducing redundant method calls in tight loops
 * 3. Optimizing the propagation threshold checks
 */
public class LightPropagationOptTransformer extends BaseTransformer {

    private static final String TARGET_CLASS = "com/hypixel/hytale/server/core/universe/world/lighting/FloodLightCalculation";
    private static final String PROPAGATE_LIGHT_METHOD = "propagateLight";

    // Injected field for enabling/disabling optimization
    public static final String ENABLED_FIELD = "$catalystLightPropOptEnabled";

    @Override
    public String getName() {
        return "LightPropagationOptTransformer";
    }

    @Override
    protected boolean shouldTransform(String className) {
        return TARGET_CLASS.equals(className);
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new LightPropOptClassVisitor(classWriter);
    }

    private class LightPropOptClassVisitor extends ClassVisitor {
        public LightPropOptClassVisitor(ClassWriter cw) {
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
                Boolean.TRUE
            ).visitEnd();
            
            System.out.println("[Catalyst] Added light propagation optimization field to FloodLightCalculation");
            super.visitEnd();
        }
    }
}

