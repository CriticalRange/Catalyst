package com.criticalrange.transformer;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Consolidated transformer for FloodLightCalculation class.
 * 
 * <p>This transformer modifies the FloodLightCalculation to optimize lighting calculations:</p>
 * <ul>
 *   <li>Skip empty sections - avoid processing sections that are solid air with no light</li>
 * </ul>
 * 
 * <p>Actual bytecode modifications:</p>
 * <ul>
 *   <li>Adds early exit check in propagateSide/Edge/Corner for empty sections</li>
 * </ul>
 * 
 * <p>The skip empty sections optimization works by checking if the source section (fromSection)
 * is solid air AND has no light data. This is safe because:</p>
 * <ul>
 *   <li>isSolidAir() returns true only if all 32768 blocks are air (block ID 0)</li>
 *   <li>getLocalLight() returns ChunkLightData.EMPTY if no light has been calculated</li>
 *   <li>If both conditions are true, there's no light to propagate from this section</li>
 * </ul>
 */
public class FloodLightCalculationTransformer extends BaseTransformer {

    private static final String TARGET_CLASS = "com.hypixel.hytale.server.core.universe.world.lighting.FloodLightCalculation";
    private static final String TARGET_CLASS_INTERNAL = "com/hypixel/hytale/server/core/universe/world/lighting/FloodLightCalculation";
    
    // Skip empty sections optimization
    public static final String SKIP_EMPTY_ENABLED_FIELD = "$catalystSkipEmptyEnabled";
    
    // BlockSection class for isSolidAir() check
    private static final String BLOCK_SECTION_CLASS = "com/hypixel/hytale/server/core/universe/world/chunk/section/BlockSection";
    
    // ChunkLightData class for empty check
    private static final String CHUNK_LIGHT_DATA_CLASS = "com/hypixel/hytale/server/core/universe/world/chunk/section/ChunkLightData";

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
            
            // Skip empty sections field
            addVolatileField(SKIP_EMPTY_ENABLED_FIELD, "Z");
        }
        
        private void addVolatileField(String name, String descriptor) {
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
            
            // Add early exit for empty sections in propagateSide method
            // Signature: propagateSide(BitSet, BlockSection, BlockSection, ChunkLightDataBuilder, IntBinaryOperator, IntBinaryOperator)
            if ("propagateSide".equals(name) && descriptor.contains("BlockSection")) {
                return new SkipEmptySectionVisitor(mv, 1); // fromSection is parameter 1 (after BitSet)
            }
            
            // Add early exit for empty sections in propagateEdge method
            // Signature: propagateEdge(BitSet, BlockSection, BlockSection, ChunkLightDataBuilder, Int2IntFunction, Int2IntFunction)
            if ("propagateEdge".equals(name) && descriptor.contains("BlockSection")) {
                return new SkipEmptySectionVisitor(mv, 1); // fromSection is parameter 1
            }
            
            // Add early exit for empty sections in propagateCorner method
            // Signature: propagateCorner(BitSet, BlockSection, BlockSection, ChunkLightDataBuilder, int, int)
            if ("propagateCorner".equals(name) && descriptor.contains("BlockSection")) {
                return new SkipEmptySectionVisitor(mv, 1); // fromSection is parameter 1
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
            // Skip empty sections - disabled by default
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, SKIP_EMPTY_ENABLED_FIELD, "Z");
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
    
    /**
     * Adds early exit check for empty sections at the start of propagate methods.
     * 
     * <p>Injects the following logic at method start:</p>
     * <pre>
     * if ($catalystSkipEmptyEnabled && fromSection != null && fromSection.isSolidAir()) {
     *     ChunkLightData localLight = fromSection.getLocalLight();
     *     if (localLight == ChunkLightData.EMPTY) {
     *         return; // Skip this section - no light to propagate
     *     }
     * }
     * </pre>
     * 
     * <p>This is safe because:</p>
     * <ul>
     *   <li>isSolidAir() means all blocks are air (transparent, no light emission)</li>
     *   <li>ChunkLightData.EMPTY means no light has been calculated yet</li>
     *   <li>Combined, this means there's no light in this section to propagate</li>
     * </ul>
     */
    private static class SkipEmptySectionVisitor extends MethodVisitor {
        private final int fromSectionParamIndex;
        
        public SkipEmptySectionVisitor(MethodVisitor mv, int fromSectionParamIndex) {
            super(BaseTransformer.ASM_VERSION, mv);
            // Local variable indices for instance methods:
            //   0 = this
            //   1 = first parameter (BitSet)
            //   2 = second parameter (fromSection - BlockSection)
            //   3 = third parameter (toSection - BlockSection)
            //   ...
            // The caller passes fromSectionParamIndex=1 (meaning "2nd parameter"),
            // so we add 1 to account for 'this' at index 0, resulting in local var index 2.
            this.fromSectionParamIndex = fromSectionParamIndex + 1;
        }
        
        @Override
        public void visitCode() {
            super.visitCode();
            
            // Inject early exit check at method start
            Label continueLabel = new Label();
            
            // Check if optimization is enabled: if (!$catalystSkipEmptyEnabled) goto continue
            mv.visitFieldInsn(Opcodes.GETSTATIC, TARGET_CLASS_INTERNAL, SKIP_EMPTY_ENABLED_FIELD, "Z");
            mv.visitJumpInsn(Opcodes.IFEQ, continueLabel);
            
            // Check if fromSection is not null: if (fromSection == null) goto continue
            mv.visitVarInsn(Opcodes.ALOAD, fromSectionParamIndex);
            mv.visitJumpInsn(Opcodes.IFNULL, continueLabel);
            
            // Check if fromSection.isSolidAir(): if (!fromSection.isSolidAir()) goto continue
            mv.visitVarInsn(Opcodes.ALOAD, fromSectionParamIndex);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, BLOCK_SECTION_CLASS, "isSolidAir", "()Z", false);
            mv.visitJumpInsn(Opcodes.IFEQ, continueLabel);
            
            // Check if localLight is EMPTY: ChunkLightData localLight = fromSection.getLocalLight()
            mv.visitVarInsn(Opcodes.ALOAD, fromSectionParamIndex);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, BLOCK_SECTION_CLASS, "getLocalLight", 
                "()L" + CHUNK_LIGHT_DATA_CLASS + ";", false);
            
            // Compare with ChunkLightData.EMPTY
            mv.visitFieldInsn(Opcodes.GETSTATIC, CHUNK_LIGHT_DATA_CLASS, "EMPTY", 
                "L" + CHUNK_LIGHT_DATA_CLASS + ";");
            
            // if (localLight != EMPTY) goto continue
            mv.visitJumpInsn(Opcodes.IF_ACMPNE, continueLabel);
            
            // All conditions met - return early (skip this empty section)
            mv.visitInsn(Opcodes.RETURN);
            
            // continueLabel: proceed with original method
            mv.visitLabel(continueLabel);
        }
    }
}
