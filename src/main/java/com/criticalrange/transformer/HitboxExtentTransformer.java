package com.criticalrange.transformer;

import org.objectweb.asm.*;

/**
 * Optimized Hitbox Extent Calculation Transformer
 *
 * Tracks usage of cached vs recalculated hitbox extents in BlockSection.
 *
 * Target: com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection
 */
public class HitboxExtentTransformer extends BaseTransformer {

    @Override
    public String getName() {
        return "HitboxExtent";
    }

    @Override
    public int priority() {
        return -100;
    }

    @Override
    protected boolean shouldTransform(String className) {
        return className.equals("com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection");
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new HitboxExtentClassVisitor(classWriter);
    }

    private static class HitboxExtentClassVisitor extends ClassVisitor {
        public HitboxExtentClassVisitor(ClassWriter classWriter) {
            super(ASM_VERSION, classWriter);
        }

        @Override
        public void visitEnd() {
            // Incremental updates counter
            FieldVisitor fv = cv.visitField(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "$catalystIncrementalUpdates", "J", null, null
            );
            if (fv != null) fv.visitEnd();

            // Full recalculations counter
            FieldVisitor fv2 = cv.visitField(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "$catalystFullRecalcs", "J", null, null
            );
            if (fv2 != null) fv2.visitEnd();

            // Saved (cached) calculations counter
            FieldVisitor fv3 = cv.visitField(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "$catalystExtentSaved", "J", null, null
            );
            if (fv3 != null) fv3.visitEnd();

            super.visitEnd();
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            if (name.equals("getMaximumHitboxExtent") && descriptor.equals("()D")) {
                return new HitboxRecalcMethodVisitor(mv);
            }
            return mv;
        }
    }

    private static class HitboxRecalcMethodVisitor extends MethodVisitor {
        public HitboxRecalcMethodVisitor(MethodVisitor mv) {
            super(ASM_VERSION, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();

            String ownerClass = "com/hypixel/hytale/server/core/universe/world/chunk/section/BlockSection";

            // Load this.maximumHitboxExtent
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, ownerClass, "maximumHitboxExtent", "D");
            
            // Compare with -1.0
            mv.visitLdcInsn(-1.0);
            mv.visitInsn(Opcodes.DCMPL);
            
            Label cachedLabel = new Label();
            mv.visitJumpInsn(Opcodes.IFNE, cachedLabel); 
            
            // Not Cached (Full Recalc): Increment counter
            mv.visitFieldInsn(Opcodes.GETSTATIC, ownerClass, "$catalystFullRecalcs", "J");
            mv.visitInsn(Opcodes.LCONST_1);
            mv.visitInsn(Opcodes.LADD);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, ownerClass, "$catalystFullRecalcs", "J");
            
            Label continueLabel = new Label();
            mv.visitJumpInsn(Opcodes.GOTO, continueLabel);
            
            // Cached: Increment saved counter
            mv.visitLabel(cachedLabel);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitFieldInsn(Opcodes.GETSTATIC, ownerClass, "$catalystExtentSaved", "J");
            mv.visitInsn(Opcodes.LCONST_1);
            mv.visitInsn(Opcodes.LADD);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, ownerClass, "$catalystExtentSaved", "J");
            
            mv.visitLabel(continueLabel);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        }
    }
}
