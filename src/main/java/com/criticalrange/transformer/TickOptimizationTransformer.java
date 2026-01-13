package com.criticalrange.transformer;

import org.objectweb.asm.*;

/**
 * Tick Rate Optimization Transformer
 * 
 * Optimizes server tick performance by:
 * 1. Skipping entity ticks for entities far from all players
 * 2. Reducing tick frequency for non-critical systems when under load
 * 
 * Target Classes:
 * - com.hypixel.hytale.component.system.tick.*TickingSystem
 * - com.hypixel.hytale.server.core.modules.entity.system.*TickingSystem
 * 
 * Expected Impact: 30-50% reduction in tick iterations for large worlds
 */
public class TickOptimizationTransformer extends BaseTransformer {
    
    /** Distance in blocks beyond which entities are considered "distant" */
    private static final int TICK_DISTANCE = 128; // 8 chunks
    
    /** Minimum TPS before adaptive throttling kicks in */
    private static final double MIN_TPS_THRESHOLD = 18.0;
    
    @Override
    public String getName() {
        return "TickOptimization";
    }
    
    @Override
    public int priority() {
        // Run early to optimize tick systems before other transformers
        return -100;
    }
    
    @Override
    protected boolean shouldTransform(String className) {
        // Target entity ticking systems
        return className.contains("TickingSystem") ||
               className.contains("EntityTicking") ||
               className.contains("system/tick/");
    }
    
    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new TickOptimizationClassVisitor(classWriter, className);
    }
    
    /**
     * ClassVisitor that injects distance-based tick skipping.
     */
    private static class TickOptimizationClassVisitor extends ClassVisitor {
        
        private final String className;
        
        public TickOptimizationClassVisitor(ClassWriter classWriter, String className) {
            super(ASM_VERSION, classWriter);
            this.className = className;
        }
        
        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            
            // Look for tick-related methods
            if (isTickMethod(name, descriptor)) {
                System.out.println("[Catalyst:TickOptimization] Injecting into " + 
                    className.replace('/', '.') + "." + name);
                return new TickMethodVisitor(mv, className, name);
            }
            
            return mv;
        }
        
        /**
         * Check if this method is a tick method we should optimize.
         */
        private boolean isTickMethod(String name, String descriptor) {
            // Common tick method names
            return name.equals("tick") ||
                   name.equals("tickEntity") ||
                   name.equals("tickEntities") ||
                   name.equals("update") ||
                   name.equals("onTick") ||
                   name.startsWith("tick");
        }
    }
    
    /**
     * MethodVisitor that injects distance checking at the start of tick methods.
     */
    private static class TickMethodVisitor extends MethodVisitor {
        
        private final String className;
        private final String methodName;
        
        public TickMethodVisitor(MethodVisitor mv, String className, String methodName) {
            super(ASM_VERSION, mv);
            this.className = className;
            this.methodName = methodName;
        }
        
        @Override
        public void visitCode() {
            super.visitCode();
            
            // Inject: if (!CatalystTickHelper.shouldTick(this)) return;
            // This will be called at the start of the method
            
            // Load 'this' reference
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            
            // Call our helper method
            mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "com/criticalrange/util/CatalystTickHelper",
                "shouldTick",
                "(Ljava/lang/Object;)Z",
                false
            );
            
            // If false, return early
            Label continueLabel = new Label();
            mv.visitJumpInsn(Opcodes.IFNE, continueLabel);
            
            // Return void (skip the tick)
            mv.visitInsn(Opcodes.RETURN);
            
            // Continue with normal tick logic
            mv.visitLabel(continueLabel);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        }
    }
}
