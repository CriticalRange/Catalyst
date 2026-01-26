package com.criticalrange.transformer;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Injects animation toggle into AnimationUtils.
 * 
 * <p>Adds a static field to AnimationUtils that can be toggled to disable animations.</p>
 */
public class AnimationToggleTransformer extends BaseTransformer {

    private static final String TARGET_CLASS = "com.hypixel.hytale.server.core.entity.AnimationUtils";
    private static final String TARGET_CLASS_INTERNAL = "com/hypixel/hytale/server/core/entity/AnimationUtils";
    
    public static final String ENABLED_FIELD = "$catalystAnimationsEnabled";

    @Override
    protected boolean shouldTransform(String className) {
        return className.equals(TARGET_CLASS);
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
        private boolean hasStaticInit = false;

        public AnimationClassVisitor(ClassVisitor cv) {
            super(BaseTransformer.ASM_VERSION, cv);
        }

        @Override
        public void visit(int version, int access, String name, String signature,
                         String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            
            // Add static field: public static volatile boolean $catalystAnimationsEnabled = true;
            FieldVisitor fv = cv.visitField(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE,
                ENABLED_FIELD, "Z", null, null);
            if (fv != null) fv.visitEnd();
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            
            if ("<clinit>".equals(name)) {
                hasStaticInit = true;
                return new StaticInitVisitor(mv);
            }
            
            // Inject early return into playAnimation and stopAnimation methods
            if ("playAnimation".equals(name) || "stopAnimation".equals(name)) {
                return new AnimationMethodVisitor(mv);
            }
            
            return mv;
        }

        @Override
        public void visitEnd() {
            if (!hasStaticInit) {
                MethodVisitor mv = cv.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
                mv.visitCode();
                initField(mv);
                mv.visitInsn(Opcodes.RETURN);
                mv.visitMaxs(1, 0);
                mv.visitEnd();
            }
            super.visitEnd();
        }

        static void initField(MethodVisitor mv) {
            // $catalystAnimationsEnabled = true (enabled by default)
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, ENABLED_FIELD, "Z");
        }
    }

    private static class StaticInitVisitor extends MethodVisitor {
        public StaticInitVisitor(MethodVisitor mv) {
            super(BaseTransformer.ASM_VERSION, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();
            AnimationClassVisitor.initField(mv);
        }
    }

    /**
     * Injects early return at the start of animation methods.
     * 
     * if (!$catalystAnimationsEnabled) return;
     */
    private static class AnimationMethodVisitor extends MethodVisitor {
        public AnimationMethodVisitor(MethodVisitor mv) {
            super(BaseTransformer.ASM_VERSION, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();
            
            // if (!$catalystAnimationsEnabled) return;
            Label continueLabel = new Label();
            mv.visitFieldInsn(Opcodes.GETSTATIC, TARGET_CLASS_INTERNAL, ENABLED_FIELD, "Z");
            mv.visitJumpInsn(Opcodes.IFNE, continueLabel);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitLabel(continueLabel);
        }
    }
}
