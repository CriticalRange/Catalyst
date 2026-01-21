package com.criticalrange.transformer;

import org.objectweb.asm.*;

/**
 * Pathfinding Configuration Transformer.
 *
 * <p>Makes A* pathfinding parameters configurable. These control how much computational
 * effort NPCs spend on pathfinding calculations.</p>
 *
 * <p>Target: AStarBase constructor where limits are initialized</p>
 *
 * <p>Default values:</p>
 * <ul>
 *   <li>maxPathLength = 200 (maximum path nodes)</li>
 *   <li>openNodesLimit = 80 (max nodes in open set)</li>
 *   <li>totalNodesLimit = 400 (max total visited nodes)</li>
 * </ul>
 *
 * <p>Tuning guide:</p>
 * <ul>
 *   <li>Lower values = faster pathfinding, shorter/simpler paths, less CPU</li>
 *   <li>Higher values = longer paths possible, more CPU per NPC</li>
 *   <li>For servers with many NPCs, consider lowering these values</li>
 *   <li>For complex terrain with few NPCs, consider raising them</li>
 * </ul>
 */
public class PathfindingConfigTransformer extends BaseTransformer {

    private static final String TARGET_CLASS = "com.hypixel.hytale.server.npc.navigation.AStarBase";
    private static final String TARGET_CLASS_INTERNAL = "com/hypixel/hytale/server/npc/navigation/AStarBase";

    // Field names for injected configuration
    public static final String ENABLED_FIELD = "$catalystPathfindingEnabled";
    public static final String MAX_PATH_LENGTH_FIELD = "$catalystMaxPathLength";
    public static final String OPEN_NODES_LIMIT_FIELD = "$catalystOpenNodesLimit";
    public static final String TOTAL_NODES_LIMIT_FIELD = "$catalystTotalNodesLimit";

    // Target fields in AStarBase
    private static final String MAX_PATH_LENGTH = "maxPathLength";
    private static final String OPEN_NODES_LIMIT = "openNodesLimit";
    private static final String TOTAL_NODES_LIMIT = "totalNodesLimit";

    @Override
    public String getName() {
        return "PathfindingConfig";
    }

    @Override
    public int priority() {
        return -80;
    }

    @Override
    protected boolean shouldTransform(String className) {
        return className.equals(TARGET_CLASS);
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new AStarBaseClassVisitor(classWriter);
    }

    private static class AStarBaseClassVisitor extends ClassVisitor {

        private boolean addedFields = false;
        private boolean hasStaticInit = false;

        public AStarBaseClassVisitor(ClassWriter classWriter) {
            super(ASM_VERSION, classWriter);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

            if (name.equals("<clinit>")) {
                hasStaticInit = true;
                return new StaticInitMethodVisitor(mv);
            }

            if (name.equals("<init>")) {
                return new ConstructorMethodVisitor(mv);
            }

            return mv;
        }

        @Override
        public void visitEnd() {
            if (!addedFields) {
                // Add enabled flag
                FieldVisitor fv1 = cv.visitField(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    ENABLED_FIELD,
                    "Z",
                    null,
                    null
                );
                if (fv1 != null) fv1.visitEnd();

                // Add maxPathLength config
                FieldVisitor fv2 = cv.visitField(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    MAX_PATH_LENGTH_FIELD,
                    "I",
                    null,
                    null
                );
                if (fv2 != null) fv2.visitEnd();

                // Add openNodesLimit config
                FieldVisitor fv3 = cv.visitField(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    OPEN_NODES_LIMIT_FIELD,
                    "I",
                    null,
                    null
                );
                if (fv3 != null) fv3.visitEnd();

                // Add totalNodesLimit config
                FieldVisitor fv4 = cv.visitField(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    TOTAL_NODES_LIMIT_FIELD,
                    "I",
                    null,
                    null
                );
                if (fv4 != null) fv4.visitEnd();

                addedFields = true;
            }

            // If class has no static initializer, create one
            if (!hasStaticInit) {
                MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_STATIC,
                    "<clinit>",
                    "()V",
                    null,
                    null
                );
                mv.visitCode();
                injectStaticFieldInit(mv);
                mv.visitInsn(Opcodes.RETURN);
                mv.visitMaxs(1, 0);
                mv.visitEnd();
            }

            super.visitEnd();
        }
    }

    /**
     * Injects field initialization with literal default values.
     * These will be updated via reflection when the plugin loads.
     */
    private static void injectStaticFieldInit(MethodVisitor mv) {
        // Default: false (disabled)
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, ENABLED_FIELD, "Z");

        // Default: 200 (same as vanilla maxPathLength)
        mv.visitIntInsn(Opcodes.SIPUSH, 200);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, MAX_PATH_LENGTH_FIELD, "I");

        // Default: 80 (same as vanilla openNodesLimit)
        mv.visitIntInsn(Opcodes.BIPUSH, 80);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, OPEN_NODES_LIMIT_FIELD, "I");

        // Default: 400 (same as vanilla totalNodesLimit)
        mv.visitIntInsn(Opcodes.SIPUSH, 400);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, TOTAL_NODES_LIMIT_FIELD, "I");
    }

    /**
     * Injects field initialization into existing static initializer.
     */
    private static class StaticInitMethodVisitor extends MethodVisitor {

        public StaticInitMethodVisitor(MethodVisitor mv) {
            super(ASM_VERSION, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();
            injectStaticFieldInit(mv);
        }
    }

    /**
     * Transforms the constructor to use configurable values.
     *
     * <p>Original bytecode pattern:</p>
     * <pre>
     *   aload_0
     *   sipush 200
     *   putfield maxPathLength:I
     *   aload_0
     *   bipush 80
     *   putfield openNodesLimit:I
     *   aload_0
     *   sipush 400
     *   putfield totalNodesLimit:I
     * </pre>
     */
    private static class ConstructorMethodVisitor extends MethodVisitor {

        private Integer pendingValue = null;
        private int pendingOpcode = -1;

        public ConstructorMethodVisitor(MethodVisitor mv) {
            super(ASM_VERSION, mv);
        }

        @Override
        public void visitIntInsn(int opcode, int operand) {
            // Check for SIPUSH 200 (maxPathLength) or BIPUSH 80 (openNodesLimit) or SIPUSH 400 (totalNodesLimit)
            if ((opcode == Opcodes.SIPUSH && operand == 200) ||
                (opcode == Opcodes.BIPUSH && operand == 80) ||
                (opcode == Opcodes.SIPUSH && operand == 400)) {
                pendingValue = operand;
                pendingOpcode = opcode;
                return;
            }

            flushPending();
            super.visitIntInsn(opcode, operand);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
            if (pendingValue != null && opcode == Opcodes.PUTFIELD && owner.equals(TARGET_CLASS_INTERNAL)) {
                String configField = null;
                int defaultValue = pendingValue;

                if (name.equals(MAX_PATH_LENGTH) && pendingValue == 200) {
                    configField = MAX_PATH_LENGTH_FIELD;
                } else if (name.equals(OPEN_NODES_LIMIT) && pendingValue == 80) {
                    configField = OPEN_NODES_LIMIT_FIELD;
                } else if (name.equals(TOTAL_NODES_LIMIT) && pendingValue == 400) {
                    configField = TOTAL_NODES_LIMIT_FIELD;
                }

                if (configField != null) {
                    // Inject conditional: if enabled, use config value, else use default
                    Label useDefault = new Label();
                    Label done = new Label();

                    // Check if enabled
                    mv.visitFieldInsn(Opcodes.GETSTATIC, TARGET_CLASS_INTERNAL, ENABLED_FIELD, "Z");
                    mv.visitJumpInsn(Opcodes.IFEQ, useDefault);

                    // Enabled: use configurable value
                    mv.visitFieldInsn(Opcodes.GETSTATIC, TARGET_CLASS_INTERNAL, configField, "I");
                    mv.visitJumpInsn(Opcodes.GOTO, done);

                    // Disabled: use default
                    mv.visitLabel(useDefault);
                    if (pendingOpcode == Opcodes.SIPUSH) {
                        mv.visitIntInsn(Opcodes.SIPUSH, defaultValue);
                    } else {
                        mv.visitIntInsn(Opcodes.BIPUSH, defaultValue);
                    }

                    mv.visitLabel(done);
                    // Frame computed automatically by COMPUTE_FRAMES

                    super.visitFieldInsn(opcode, owner, name, descriptor);
                    pendingValue = null;
                    pendingOpcode = -1;
                    return;
                }
            }

            flushPending();
            super.visitFieldInsn(opcode, owner, name, descriptor);
        }

        @Override
        public void visitInsn(int opcode) {
            flushPending();
            super.visitInsn(opcode);
        }

        @Override
        public void visitVarInsn(int opcode, int var) {
            flushPending();
            super.visitVarInsn(opcode, var);
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
            if (pendingValue != null) {
                if (pendingOpcode == Opcodes.SIPUSH) {
                    super.visitIntInsn(Opcodes.SIPUSH, pendingValue);
                } else {
                    super.visitIntInsn(Opcodes.BIPUSH, pendingValue);
                }
                pendingValue = null;
                pendingOpcode = -1;
            }
        }

        @Override
        public void visitEnd() {
            flushPending();
            super.visitEnd();
        }
    }
}
