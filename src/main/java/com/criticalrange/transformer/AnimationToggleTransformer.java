package com.criticalrange.transformer;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Injects animation toggle into AnimationUtils.
 * 
 * <p>Uses VisualEffectsToggle for centralized toggle state.</p>
 */
public class AnimationToggleTransformer extends BaseTransformer {

    private static final String TARGET_CLASS = "com/hypixel/hytale/server/core/entity/AnimationUtils";
    private static final String TOGGLE_CLASS = "com/criticalrange/util/VisualEffectsToggle";

    @Override
    protected boolean shouldTransform(String className) {
        return TARGET_CLASS.equals(className);
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new AnimationClassVisitor(classWriter);
    }

    @Override
    public String getName() {
        return "AnimationToggleTransformer";
    }

    private static class AnimationClassVisitor extends ClassVisitor {
        public AnimationClassVisitor(ClassVisitor cv) {
            super(BaseTransformer.ASM_VERSION, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            
            // Inject early return into playAnimation and stopAnimation methods
            if ("playAnimation".equals(name) || "stopAnimation".equals(name)) {
                return new AnimationMethodVisitor(mv);
            }
            
            return mv;
        }
    }

    /**
     * Injects early return at the start of animation methods.
     * 
     * if (!VisualEffectsToggle.areAnimationsEnabled()) return;
     */
    private static class AnimationMethodVisitor extends MethodVisitor {
        public AnimationMethodVisitor(MethodVisitor mv) {
            super(BaseTransformer.ASM_VERSION, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();
            
            // if (!VisualEffectsToggle.areAnimationsEnabled()) return;
            Label continueLabel = new Label();
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, TOGGLE_CLASS, "areAnimationsEnabled", "()Z", false);
            mv.visitJumpInsn(Opcodes.IFNE, continueLabel);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitLabel(continueLabel);
        }
    }
}
