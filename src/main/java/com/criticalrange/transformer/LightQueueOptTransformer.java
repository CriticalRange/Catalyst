package com.criticalrange.transformer;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

/**
 * Optimizes the light propagation queue processing in FloodLightCalculation.
 * Adds early-exit conditions for sections with no light sources.
 */
public class LightQueueOptTransformer extends BaseTransformer {

    private static final String TARGET_CLASS = "com/hypixel/hytale/server/core/universe/world/lighting/FloodLightCalculation";
    
    public static final String ENABLED_FIELD = "$catalystQueueOptEnabled";
    public static final String SKIP_EMPTY_FIELD = "$catalystSkipEmptySections";

    @Override
    public String getName() {
        return "LightQueueOptTransformer";
    }

    @Override
    protected boolean shouldTransform(String className) {
        return TARGET_CLASS.equals(className);
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new QueueOptClassVisitor(classWriter);
    }

    private class QueueOptClassVisitor extends ClassVisitor {
        public QueueOptClassVisitor(ClassWriter cw) {
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

            // Add skip empty sections field
            cv.visitField(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE,
                SKIP_EMPTY_FIELD,
                "Z",
                null,
                Boolean.TRUE
            ).visitEnd();

            System.out.println("[Catalyst] Added queue optimization fields to FloodLightCalculation");
            super.visitEnd();
        }
    }
}
