package com.criticalrange.transformer;

import org.objectweb.asm.*;

/**
 * Chunk Loading Rate Transformer.
 *
 * <p>Makes the maxChunksPerTick value configurable. This controls how many
 * chunks are sent to players per server tick.</p>
 *
 * <p>Target: ChunkTracker constructor where maxChunksPerTick is initialized</p>
 *
 * <p>The original code sets: this.maxChunksPerTick = 4</p>
 * <p>We change it to use our configurable value.</p>
 *
 * <p>Characteristics:</p>
 * <ul>
 *   <li>Higher values = faster chunk loading for players, more bandwidth/CPU</li>
 *   <li>Lower values = slower chunk loading, smoother server TPS</li>
 *   <li>Hytale default is 4 chunks per tick</li>
 *   <li>Allows tuning for specific server hardware and player count</li>
 * </ul>
 */
public class ChunkRateTransformer extends BaseTransformer {

    private static final String TARGET_CLASS = "com.hypixel.hytale.server.core.modules.entity.player.ChunkTracker";
    private static final String TARGET_CLASS_INTERNAL = "com/hypixel/hytale/server/core/modules/entity/player/ChunkTracker";
    private static final String TARGET_FIELD = "maxChunksPerTick";

    /** Field name for the injected configurable value */
    public static final String ENABLED_FIELD = "$catalystChunkRateEnabled";
    public static final String CHUNKS_FIELD = "$catalystChunksPerTick";

    @Override
    public String getName() {
        return "ChunkRate";
    }

    @Override
    public int priority() {
        return -70;
    }

    @Override
    protected boolean shouldTransform(String className) {
        return className.equals(TARGET_CLASS);
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new ChunkTrackerClassVisitor(classWriter);
    }

    private static class ChunkTrackerClassVisitor extends ClassVisitor {

        private boolean addedFields = false;
        private boolean hasStaticInit = false;

        public ChunkTrackerClassVisitor(ClassWriter classWriter) {
            super(ASM_VERSION, classWriter);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

            // Transform static initializer to set our field values
            if (name.equals("<clinit>")) {
                hasStaticInit = true;
                return new StaticInitMethodVisitor(mv);
            }

            // Only transform constructors where maxChunksPerTick is initialized
            if (name.equals("<init>")) {
                return new ConstructorMethodVisitor(mv);
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

                // Add configurable chunks per tick (no default value - will be set via <clinit>)
                FieldVisitor fv2 = cv.visitField(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    CHUNKS_FIELD,
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
                // Default: 4 (same as vanilla)
                mv.visitInsn(Opcodes.ICONST_4);
                mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, CHUNKS_FIELD, "I");
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

            // Default: 4 (same as vanilla)
            mv.visitInsn(Opcodes.ICONST_4);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, CHUNKS_FIELD, "I");
        }
    }

    /**
     * Transforms the constructor to use our configurable value for maxChunksPerTick.
     * 
     * <p>Original bytecode pattern:</p>
     * <pre>
     *   aload_0
     *   iconst_4
     *   putfield maxChunksPerTick:I
     * </pre>
     * 
     * <p>We replace iconst_4 with our field read when the next instruction
     * is putfield to maxChunksPerTick.</p>
     */
    private static class ConstructorMethodVisitor extends MethodVisitor {

        private boolean pendingIconst4 = false;

        public ConstructorMethodVisitor(MethodVisitor mv) {
            super(ASM_VERSION, mv);
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode == Opcodes.ICONST_4) {
                // Don't emit yet, wait to see if next is putfield maxChunksPerTick
                pendingIconst4 = true;
                return;
            }
            
            flushPending();
            super.visitInsn(opcode);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
            if (pendingIconst4 && opcode == Opcodes.PUTFIELD && 
                name.equals(TARGET_FIELD) && owner.equals(TARGET_CLASS_INTERNAL)) {
                // This is the pattern: this.maxChunksPerTick = 4
                // Stack state: [this] (aload_0 was done before iconst_4)
                // We need to push an int value, then do putfield
                
                Label useDefault = new Label();
                Label done = new Label();
                
                // Check if enabled (stack: [this])
                mv.visitFieldInsn(Opcodes.GETSTATIC, TARGET_CLASS_INTERNAL, ENABLED_FIELD, "Z");
                mv.visitJumpInsn(Opcodes.IFEQ, useDefault);
                
                // Enabled: use configurable value (stack: [this])
                mv.visitFieldInsn(Opcodes.GETSTATIC, TARGET_CLASS_INTERNAL, CHUNKS_FIELD, "I");
                // Stack now: [this, int]
                mv.visitJumpInsn(Opcodes.GOTO, done);
                
                // Disabled: use default 4 (stack: [this])
                mv.visitLabel(useDefault);
                // Frame computed automatically by COMPUTE_FRAMES
                mv.visitInsn(Opcodes.ICONST_4);
                // Stack now: [this, int]
                
                mv.visitLabel(done);
                // Frame computed automatically by COMPUTE_FRAMES
                
                // Now do the putfield (consumes [this, int])
                super.visitFieldInsn(opcode, owner, name, descriptor);
                
                pendingIconst4 = false;
                return;
            }
            
            flushPending();
            super.visitFieldInsn(opcode, owner, name, descriptor);
        }

        @Override
        public void visitVarInsn(int opcode, int var) {
            flushPending();
            super.visitVarInsn(opcode, var);
        }

        @Override
        public void visitIntInsn(int opcode, int operand) {
            flushPending();
            super.visitIntInsn(opcode, operand);
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

        @Override
        public void visitLabel(Label label) {
            flushPending();
            super.visitLabel(label);
        }

        private void flushPending() {
            if (pendingIconst4) {
                super.visitInsn(Opcodes.ICONST_4);
                pendingIconst4 = false;
            }
        }

        @Override
        public void visitEnd() {
            flushPending();
            super.visitEnd();
        }
    }
}
