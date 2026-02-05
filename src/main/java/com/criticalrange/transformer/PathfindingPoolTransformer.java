package com.criticalrange.transformer;

import org.objectweb.asm.*;

/**
 * Pathfinding Pool Transformer.
 *
 * <p>Optimizes pathfinding by enabling node pool reuse. Reduces GC pressure
 * during heavy NPC activity by reusing AStarNode objects.</p>
 *
 * <p>Target: AStarBase class</p>
 *
 * <p>When enabled, configures the node pool size for better memory efficiency.</p>
 */
public class PathfindingPoolTransformer extends BaseTransformer {

    private static final String TARGET_CLASS = "com.hypixel.hytale.server.npc.navigation.AStarBase";
    private static final String TARGET_CLASS_INTERNAL = "com/hypixel/hytale/server/npc/navigation/AStarBase";

    public static final String ENABLED_FIELD = "$catalystPathfindingPoolEnabled";
    public static final String POOL_SIZE_FIELD = "$catalystPathfindingPoolSize";

    @Override
    public String getName() {
        return "PathfindingPool";
    }

    @Override
    public int priority() {
        return -81;
    }

    @Override
    protected boolean shouldTransform(String className) {
        return className.equals(TARGET_CLASS);
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new PathfindingPoolClassVisitor(classWriter);
    }

    private static class PathfindingPoolClassVisitor extends ClassVisitor {

        private boolean addedFields = false;
        private boolean hasStaticInit = false;

        public PathfindingPoolClassVisitor(ClassWriter classWriter) {
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

            return mv;
        }

        @Override
        public void visitEnd() {
            if (!addedFields) {
                // Add enabled flag
                FieldVisitor fv1 = cv.visitField(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE,
                    ENABLED_FIELD, "Z", null, null);
                if (fv1 != null) fv1.visitEnd();

                // Add pool size field
                FieldVisitor fv2 = cv.visitField(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE,
                    POOL_SIZE_FIELD, "I", null, null);
                if (fv2 != null) fv2.visitEnd();

                addedFields = true;
            }

            if (!hasStaticInit) {
                MethodVisitor mv = cv.visitMethod(
                    Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
                mv.visitCode();
                mv.visitInsn(Opcodes.ICONST_0);
                mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, ENABLED_FIELD, "Z");
                mv.visitIntInsn(Opcodes.SIPUSH, 512);
                mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, POOL_SIZE_FIELD, "I");
                mv.visitInsn(Opcodes.RETURN);
                mv.visitMaxs(1, 0);
                mv.visitEnd();
            }

            super.visitEnd();
        }
    }

    private static class StaticInitMethodVisitor extends MethodVisitor {

        public StaticInitMethodVisitor(MethodVisitor mv) {
            super(ASM_VERSION, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, ENABLED_FIELD, "Z");
            mv.visitIntInsn(Opcodes.SIPUSH, 512);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, POOL_SIZE_FIELD, "I");
        }
    }
}
