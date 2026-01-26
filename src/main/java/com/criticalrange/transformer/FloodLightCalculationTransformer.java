package com.criticalrange.transformer;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Consolidated transformer for FloodLightCalculation class.
 * 
 * <p>Combines all FloodLightCalculation-related field injections into a single transformer
 * to avoid bytecode corruption from multiple transformers modifying the same class.</p>
 * 
 * <p>Includes fields for:</p>
 * <ul>
 *   <li>Light propagation optimization</li>
 *   <li>Opacity lookup cache</li>
 *   <li>Queue optimization</li>
 *   <li>Lighting distance limit</li>
 * </ul>
 */
public class FloodLightCalculationTransformer extends BaseTransformer {

    private static final String TARGET_CLASS = "com.hypixel.hytale.server.core.universe.world.lighting.FloodLightCalculation";
    private static final String TARGET_CLASS_INTERNAL = "com/hypixel/hytale/server/core/universe/world/lighting/FloodLightCalculation";
    
    // Light propagation optimization field
    public static final String LIGHT_PROP_OPT_ENABLED_FIELD = "$catalystLightPropOptEnabled";
    
    // Opacity lookup cache fields
    public static final String OPACITY_CACHE_ENABLED_FIELD = "$catalystOpacityCacheEnabled";
    public static final String OPACITY_CACHE_FIELD = "$catalystOpacityCache";
    public static final String OPACITY_CACHE_INIT_FIELD = "$catalystOpacityCacheInit";
    
    // Queue optimization fields
    public static final String QUEUE_OPT_ENABLED_FIELD = "$catalystQueueOptEnabled";
    public static final String SKIP_EMPTY_SECTIONS_FIELD = "$catalystSkipEmptySections";
    
    // Lighting distance fields
    public static final String LIGHTING_DISTANCE_ENABLED_FIELD = "$catalystLightingDistanceEnabled";
    public static final String LIGHTING_MAX_DISTANCE_FIELD = "$catalystLightingMaxDistance";

    @Override
    protected boolean shouldTransform(String className) {
        return className.equals(TARGET_CLASS);
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new FloodLightClassVisitor(classWriter);
    }

    @Override
    public String getName() {
        return "FloodLightCalculationTransformer";
    }

    private static class FloodLightClassVisitor extends ClassVisitor {
        private boolean hasStaticInit = false;

        public FloodLightClassVisitor(ClassVisitor cv) {
            super(BaseTransformer.ASM_VERSION, cv);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            
            // Light propagation optimization field
            addField(LIGHT_PROP_OPT_ENABLED_FIELD, "Z");
            
            // Opacity lookup cache fields
            addField(OPACITY_CACHE_ENABLED_FIELD, "Z");
            addField(OPACITY_CACHE_FIELD, "[B");
            addField(OPACITY_CACHE_INIT_FIELD, "Z");
            
            // Queue optimization fields
            addField(QUEUE_OPT_ENABLED_FIELD, "Z");
            addField(SKIP_EMPTY_SECTIONS_FIELD, "Z");
            
            // Lighting distance fields
            addField(LIGHTING_DISTANCE_ENABLED_FIELD, "Z");
            addField(LIGHTING_MAX_DISTANCE_FIELD, "I");
        }
        
        private void addField(String name, String descriptor) {
            FieldVisitor fv = cv.visitField(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE,
                name, descriptor, null, null);
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
            
            return mv;
        }

        @Override
        public void visitEnd() {
            if (!hasStaticInit) {
                MethodVisitor mv = cv.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
                mv.visitCode();
                initAllFields(mv);
                mv.visitInsn(Opcodes.RETURN);
                mv.visitMaxs(2, 0);
                mv.visitEnd();
            }
            super.visitEnd();
        }

        static void initAllFields(MethodVisitor mv) {
            // Light propagation optimization - disabled by default
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, LIGHT_PROP_OPT_ENABLED_FIELD, "Z");
            
            // Opacity lookup cache - disabled by default
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, OPACITY_CACHE_ENABLED_FIELD, "Z");
            // Cache array starts as null
            mv.visitInsn(Opcodes.ACONST_NULL);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, OPACITY_CACHE_FIELD, "[B");
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, OPACITY_CACHE_INIT_FIELD, "Z");
            
            // Queue optimization - disabled by default
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, QUEUE_OPT_ENABLED_FIELD, "Z");
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, SKIP_EMPTY_SECTIONS_FIELD, "Z");
            
            // Lighting distance - disabled by default, default distance 8
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, LIGHTING_DISTANCE_ENABLED_FIELD, "Z");
            mv.visitIntInsn(Opcodes.BIPUSH, 8);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, LIGHTING_MAX_DISTANCE_FIELD, "I");
        }
    }

    private static class StaticInitVisitor extends MethodVisitor {
        public StaticInitVisitor(MethodVisitor mv) {
            super(BaseTransformer.ASM_VERSION, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();
            FloodLightClassVisitor.initAllFields(mv);
        }
    }
}
