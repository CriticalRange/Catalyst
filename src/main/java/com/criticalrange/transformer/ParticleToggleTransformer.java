package com.criticalrange.transformer;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Injects particle toggle into ParticleUtil.
 * 
 * <p>Uses VisualEffectsToggle for centralized toggle state.</p>
 */
public class ParticleToggleTransformer extends BaseTransformer {

    private static final String TARGET_CLASS = "com/hypixel/hytale/server/core/universe/world/ParticleUtil";
    private static final String TOGGLE_CLASS = "com/criticalrange/util/VisualEffectsToggle";

    @Override
    protected boolean shouldTransform(String className) {
        return TARGET_CLASS.equals(className);
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new ParticleClassVisitor(classWriter);
    }

    @Override
    public String getName() {
        return "ParticleToggleTransformer";
    }

    private static class ParticleClassVisitor extends ClassVisitor {
        public ParticleClassVisitor(ClassVisitor cv) {
            super(BaseTransformer.ASM_VERSION, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            
            // Inject early return into spawnParticleEffect methods
            if ("spawnParticleEffect".equals(name)) {
                return new ParticleMethodVisitor(mv);
            }
            
            return mv;
        }
    }

    /**
     * Injects early return at the start of particle methods.
     * 
     * if (!VisualEffectsToggle.areParticlesEnabled()) return;
     */
    private static class ParticleMethodVisitor extends MethodVisitor {
        public ParticleMethodVisitor(MethodVisitor mv) {
            super(BaseTransformer.ASM_VERSION, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();
            
            // if (!VisualEffectsToggle.areParticlesEnabled()) return;
            Label continueLabel = new Label();
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, TOGGLE_CLASS, "areParticlesEnabled", "()Z", false);
            mv.visitJumpInsn(Opcodes.IFNE, continueLabel);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitLabel(continueLabel);
        }
    }
}
