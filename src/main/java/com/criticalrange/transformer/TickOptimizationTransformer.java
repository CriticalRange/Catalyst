package com.criticalrange.transformer;

import org.objectweb.asm.*;

/**
 * Tick Rate Optimization Transformer
 *
 * Injects simple tick counting into entity tick systems.
 * Uses inline bytecode to avoid external class dependencies.
 *
 * Target Classes:
 * - EntityTickingSystem (doTick static method)
 * - UpdateLocationSystems$TickingSystem (tick method)
 * - MovementStatesSystems$TickingSystem (tick method)
 * - RepulsionSystems$RepulsionTicker (tick method)
 *
 * The optimization logic is simplified to avoid classloader issues.
 * Full tick skipping can be added later once the infrastructure is stable.
 */
public class TickOptimizationTransformer extends BaseTransformer {

    private static final boolean DEBUG = true;

    @Override
    public String getName() {
        return "TickOptimization";
    }

    @Override
    public int priority() {
        return -100;
    }

    @Override
    protected boolean shouldTransform(String className) {
        // className comes in dot format (e.g., com.example.Class)
        return className.equals("com.hypixel.hytale.component.system.tick.EntityTickingSystem") ||
               className.equals("com.hypixel.hytale.server.core.modules.entity.system.UpdateLocationSystems$TickingSystem") ||
               className.equals("com.hypixel.hytale.server.core.entity.movement.MovementStatesSystems$TickingSystem") ||
               className.equals("com.hypixel.hytale.server.core.modules.entity.repulsion.RepulsionSystems$RepulsionTicker");
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new TickOptimizationClassVisitor(classWriter, className);
    }

    private static class TickOptimizationClassVisitor extends ClassVisitor {

        private final String className;
        private final String fieldName;

        public TickOptimizationClassVisitor(ClassWriter classWriter, String className) {
            super(ASM_VERSION, classWriter);
            this.className = className;
            // Create a unique field name based on class name
            this.fieldName = "catalyst$" + className.replace('.', '_').replace('$', '_') + "$tickCount";
        }

        @Override
        public void visitEnd() {
            // Add a static field to track tick count
            FieldVisitor fv = cv.visitField(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                fieldName,
                "J",
                null,
                null
            );
            if (fv != null) {
                fv.visitEnd();
            }
            super.visitEnd();
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

            // For EntityTickingSystem.doTick (static method)
            if (className.equals("com.hypixel.hytale.component.system.tick.EntityTickingSystem")) {
                if (name.equals("doTick") && (access & Opcodes.ACC_STATIC) != 0) {
                    if (DEBUG) {
                        System.out.println("[Catalyst:TickOptimization] Injecting counter into EntityTickingSystem.doTick");
                    }
                    return new DoTickMethodVisitor(mv, className);
                }
            }

            // For TickingSystem subclasses with tick(float, int, ...) method
            if (name.equals("tick") && descriptor.startsWith("(FI")) {
                if (DEBUG) {
                    System.out.println("[Catalyst:TickOptimization] Injecting counter into " + className + ".tick");
                }
                return new TickMethodVisitor(mv, className, fieldName);
            }

            return mv;
        }
    }

    /**
     * Injects a simple tick counter increment at the start of doTick().
     * No external class calls - just pure bytecode.
     */
    private static class DoTickMethodVisitor extends MethodVisitor {

        private final String className;

        public DoTickMethodVisitor(MethodVisitor mv, String className) {
            super(ASM_VERSION, mv);
            this.className = className;
        }

        @Override
        public void visitCode() {
            super.visitCode();

            // Inject: EntityTickingSystem.catalyst$com_hypixel_hytale_component_system_tick_EntityTickingSystem$tickCount++;
            String fieldName = "catalyst$" + className.replace('.', '_').replace('$', '_') + "$tickCount";

            // Get the static field
            mv.visitFieldInsn(Opcodes.GETSTATIC, "com/hypixel/hytale/component/system/tick/EntityTickingSystem", fieldName, "J");

            // Load 1
            mv.visitInsn(Opcodes.LCONST_1);

            // Add
            mv.visitInsn(Opcodes.LADD);

            // Put back to static field
            mv.visitFieldInsn(Opcodes.PUTSTATIC, "com/hypixel/hytale/component/system/tick/EntityTickingSystem", fieldName, "J");
        }
    }

    /**
     * Injects a simple tick counter increment at the start of each entity tick().
     * No skipping logic - just counting for now.
     */
    private static class TickMethodVisitor extends MethodVisitor {

        private final String className;
        private final String fieldName;

        public TickMethodVisitor(MethodVisitor mv, String className, String fieldName) {
            super(ASM_VERSION, mv);
            this.className = className;
            this.fieldName = fieldName;
        }

        @Override
        public void visitCode() {
            super.visitCode();

            // Convert className to internal form (dots to slashes)
            String internalName = className.replace('.', '/');

            // Inject: <ClassName>.fieldName++;
            // Get the static field
            mv.visitFieldInsn(Opcodes.GETSTATIC, internalName, fieldName, "J");

            // Load 1
            mv.visitInsn(Opcodes.LCONST_1);

            // Add
            mv.visitInsn(Opcodes.LADD);

            // Put back to static field
            mv.visitFieldInsn(Opcodes.PUTSTATIC, internalName, fieldName, "J");
        }
    }
}
