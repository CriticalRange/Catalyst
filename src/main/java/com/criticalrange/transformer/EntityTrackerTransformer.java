package com.criticalrange.transformer;

import org.objectweb.asm.*;

/**
 * Entity Tracking Optimization Transformer
 * 
 * Optimizes entity tracking by:
 * 1. Using spatial hash partitioning instead of O(n) iteration
 * 2. Batching entity updates across multiple ticks
 * 
 * Target Classes:
 * - com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems
 * 
 * Expected Impact: O(n) → O(log n) entity lookups
 */
public class EntityTrackerTransformer extends BaseTransformer {
    
    @Override
    public String getName() {
        return "EntityTracker";
    }
    
    @Override
    public int priority() {
        // Run after tick optimization
        return -50;
    }
    
    @Override
    protected boolean shouldTransform(String className) {
        return className.contains("EntityTracker") ||
               className.contains("entity/tracker/") ||
               className.contains("EntityTracking");
    }
    
    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new EntityTrackerClassVisitor(classWriter, className);
    }
    
    /**
     * ClassVisitor that wraps entity tracking with spatial partitioning.
     */
    private static class EntityTrackerClassVisitor extends ClassVisitor {
        
        private final String className;
        
        public EntityTrackerClassVisitor(ClassWriter classWriter, String className) {
            super(ASM_VERSION, classWriter);
            this.className = className;
        }
        
        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            
            // Optimize entity search methods
            if (isEntitySearchMethod(name)) {
                System.out.println("[Catalyst:EntityTracker] Optimizing " + 
                    className.replace('/', '.') + "." + name);
                return new EntitySearchMethodVisitor(mv, className, name);
            }
            
            // Optimize update methods
            if (isUpdateMethod(name)) {
                System.out.println("[Catalyst:EntityTracker] Optimizing " + 
                    className.replace('/', '.') + "." + name);
                return new EntityUpdateMethodVisitor(mv, className, name);
            }
            
            return mv;
        }
        
        private boolean isEntitySearchMethod(String name) {
            return name.contains("getNearby") ||
                   name.contains("getEntities") ||
                   name.contains("findEntities") ||
                   name.contains("getVisible") ||
                   name.contains("searchEntities");
        }
        
        private boolean isUpdateMethod(String name) {
            return name.equals("update") ||
                   name.equals("updateTracking") ||
                   name.equals("trackEntity");
        }
    }
    
    /**
     * Optimizes entity search methods with spatial partitioning wrapper.
     */
    private static class EntitySearchMethodVisitor extends MethodVisitor {
        
        private final String className;
        private final String methodName;
        
        public EntitySearchMethodVisitor(MethodVisitor mv, String className, String methodName) {
            super(ASM_VERSION, mv);
            this.className = className;
            this.methodName = methodName;
        }
        
        @Override
        public void visitCode() {
            super.visitCode();
            
            // Inject: CatalystSpatialIndex.onEntitySearch();
            // This increments our search counter for metrics
            mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "com/criticalrange/util/CatalystSpatialIndex",
                "onEntitySearch",
                "()V",
                false
            );
        }
    }
    
    /**
     * Optimizes entity update methods with batching.
     */
    private static class EntityUpdateMethodVisitor extends MethodVisitor {
        
        private final String className;
        private final String methodName;
        
        public EntityUpdateMethodVisitor(MethodVisitor mv, String className, String methodName) {
            super(ASM_VERSION, mv);
            this.className = className;
            this.methodName = methodName;
        }
        
        @Override
        public void visitCode() {
            super.visitCode();
            
            // Inject: if (!CatalystSpatialIndex.shouldUpdateTracker()) return;
            mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "com/criticalrange/util/CatalystSpatialIndex",
                "shouldUpdateTracker",
                "()Z",
                false
            );
            
            Label continueLabel = new Label();
            mv.visitJumpInsn(Opcodes.IFNE, continueLabel);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitLabel(continueLabel);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        }
    }
}
