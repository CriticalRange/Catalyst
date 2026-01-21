package com.criticalrange.transformer;

import org.objectweb.asm.*;

/**
 * Entity Tracker Distance Transformer.
 *
 * <p>Allows configuring the entity view distance multiplier. The game calculates
 * entity view distance as: Player.getViewRadius() * 32 (blocks per chunk).</p>
 *
 * <p>This transformer modifies the Universe class to use a configurable multiplier
 * instead of the hardcoded 32, allowing finer control over entity sync distance
 * independent of chunk view distance.</p>
 *
 * <p>Target: Universe.addPlayer (where EntityViewer is constructed)</p>
 *
 * <p>Benefits:</p>
 * <ul>
 *   <li>Reduces network bandwidth for entity sync when lowered</li>
 *   <li>Reduces client-side entity rendering load</li>
 *   <li>Can increase entity visibility for PvP servers</li>
 *   <li>Safe: only modifies the specific multiplication constant</li>
 * </ul>
 */
public class EntityDistanceTransformer extends BaseTransformer {

    private static final String TARGET_CLASS = "com.hypixel.hytale.server.core.universe.Universe";
    private static final String TARGET_CLASS_INTERNAL = "com/hypixel/hytale/server/core/universe/Universe";

    /** Field names for injected state */
    public static final String ENABLED_FIELD = "$catalystEntityDistEnabled";
    public static final String MULTIPLIER_FIELD = "$catalystEntityViewMultiplier";

    @Override
    public String getName() {
        return "EntityDistance";
    }

    @Override
    public int priority() {
        return -90;
    }

    @Override
    protected boolean shouldTransform(String className) {
        return className.equals(TARGET_CLASS);
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new UniverseClassVisitor(classWriter);
    }

    private static class UniverseClassVisitor extends ClassVisitor {

        private boolean addedFields = false;
        private boolean hasStaticInit = false;

        public UniverseClassVisitor(ClassWriter classWriter) {
            super(ASM_VERSION, classWriter);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

            // Transform static initializer to set our field values from CatalystConfig
            if (name.equals("<clinit>")) {
                hasStaticInit = true;
                return new StaticInitMethodVisitor(mv);
            }

            // Target only methods that actually contain the BIPUSH 32; IMUL pattern
            // Based on bytecode analysis: addPlayer and resetPlayer
            if (name.equals("addPlayer") || name.equals("resetPlayer")) {
                return new ViewDistanceMethodVisitor(mv);
            }

            return mv;
        }

        @Override
        public void visitEnd() {
            if (!addedFields) {
                // Add enabled flag (no default value - will be set via <clinit>)
                FieldVisitor fv1 = cv.visitField(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    ENABLED_FIELD,
                    "Z",
                    null,
                    null
                );
                if (fv1 != null) fv1.visitEnd();

                // Add multiplier (blocks per chunk, default 32)
                // Lower values = shorter entity view distance
                // e.g., 16 = half the normal distance
                FieldVisitor fv2 = cv.visitField(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    MULTIPLIER_FIELD,
                    "I",
                    null,
                    null
                );
                if (fv2 != null) fv2.visitEnd();

                addedFields = true;
            }

            // If class has no static initializer, create one with default values
            if (!hasStaticInit) {
                MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_STATIC,
                    "<clinit>",
                    "()V",
                    null,
                    null
                );
                mv.visitCode();
                // Default: false (disabled) - will be updated via reflection when plugin loads
                mv.visitInsn(Opcodes.ICONST_0);
                mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, ENABLED_FIELD, "Z");
                // Default: 32 (same as vanilla)
                mv.visitIntInsn(Opcodes.BIPUSH, 32);
                mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, MULTIPLIER_FIELD, "I");
                mv.visitInsn(Opcodes.RETURN);
                mv.visitMaxs(1, 0);
                mv.visitEnd();
            }

            super.visitEnd();
        }
    }

    /**
     * Injects field initialization into the static initializer.
     * Uses literal default values - updated via reflection when plugin loads.
     */
    private static class StaticInitMethodVisitor extends MethodVisitor {

        public StaticInitMethodVisitor(MethodVisitor mv) {
            super(ASM_VERSION, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();

            // Default: false (disabled) - will be updated via reflection when plugin loads
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, ENABLED_FIELD, "Z");

            // Default: 32 (same as vanilla)
            mv.visitIntInsn(Opcodes.BIPUSH, 32);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, MULTIPLIER_FIELD, "I");
        }
    }

    /**
     * Replaces the hardcoded BIPUSH 32 multiplication with our configurable field.
     * 
     * <p>The original code does: Player.getViewRadius() * 32</p>
     * <p>We change it to: Player.getViewRadius() * $catalystEntityViewMultiplier</p>
     * 
     * <p>This is safe because we specifically look for BIPUSH 32 followed by IMUL,
     * which is the exact pattern used for view distance calculation.</p>
     */
    private static class ViewDistanceMethodVisitor extends MethodVisitor {

        private boolean lastWasBipush32 = false;

        public ViewDistanceMethodVisitor(MethodVisitor mv) {
            super(ASM_VERSION, mv);
        }

        @Override
        public void visitIntInsn(int opcode, int operand) {
            // Check for BIPUSH 32 (the blocks-per-chunk multiplier for view distance)
            if (opcode == Opcodes.BIPUSH && operand == 32) {
                lastWasBipush32 = true;
                // Don't emit yet - wait to see if IMUL follows
                return;
            }
            
            // If we had a BIPUSH 32 but next instruction isn't what we expect,
            // emit the original BIPUSH 32
            if (lastWasBipush32) {
                super.visitIntInsn(Opcodes.BIPUSH, 32);
                lastWasBipush32 = false;
            }
            
            super.visitIntInsn(opcode, operand);
        }

        @Override
        public void visitInsn(int opcode) {
            // Check if this is IMUL following BIPUSH 32
            if (lastWasBipush32 && opcode == Opcodes.IMUL) {
                // This is the pattern: viewRadius * 32
                // Stack state: [int] (viewRadius from getViewRadius())
                // Replace with: viewRadius * $catalystEntityViewMultiplier (if enabled)
                // or: viewRadius * 32 (if disabled)
                
                Label useDefault = new Label();
                Label done = new Label();
                
                // Check if enabled (stack: [int])
                mv.visitFieldInsn(Opcodes.GETSTATIC, TARGET_CLASS_INTERNAL, ENABLED_FIELD, "Z");
                mv.visitJumpInsn(Opcodes.IFEQ, useDefault);
                
                // Enabled: use configurable multiplier (stack: [int])
                mv.visitFieldInsn(Opcodes.GETSTATIC, TARGET_CLASS_INTERNAL, MULTIPLIER_FIELD, "I");
                mv.visitInsn(Opcodes.IMUL);
                // Stack now: [int] (result)
                mv.visitJumpInsn(Opcodes.GOTO, done);
                
                // Disabled: use default 32 (stack: [int])
                mv.visitLabel(useDefault);
                // Frame computed automatically by COMPUTE_FRAMES
                mv.visitIntInsn(Opcodes.BIPUSH, 32);
                mv.visitInsn(Opcodes.IMUL);
                // Stack now: [int] (result)
                
                mv.visitLabel(done);
                // Frame computed automatically by COMPUTE_FRAMES
                
                lastWasBipush32 = false;
                return;
            }
            
            // If we had BIPUSH 32 but this isn't IMUL, emit the original
            if (lastWasBipush32) {
                super.visitIntInsn(Opcodes.BIPUSH, 32);
                lastWasBipush32 = false;
            }
            
            super.visitInsn(opcode);
        }

        @Override
        public void visitVarInsn(int opcode, int var) {
            flushPending();
            super.visitVarInsn(opcode, var);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
            flushPending();
            super.visitFieldInsn(opcode, owner, name, descriptor);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            flushPending();
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }

        @Override
        public void visitJumpInsn(int opcode, Label label) {
            flushPending();
            super.visitJumpInsn(opcode, label);
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            flushPending();
            super.visitTypeInsn(opcode, type);
        }

        @Override
        public void visitLdcInsn(Object value) {
            flushPending();
            super.visitLdcInsn(value);
        }

        private void flushPending() {
            if (lastWasBipush32) {
                super.visitIntInsn(Opcodes.BIPUSH, 32);
                lastWasBipush32 = false;
            }
        }

        @Override
        public void visitEnd() {
            flushPending();
            super.visitEnd();
        }
    }
}
