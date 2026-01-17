package com.criticalrange.transformer;

import org.objectweb.asm.*;

/**
 * Cached Block Type Lookup Transformer
 *
 * Optimizes BlockType lookups by adding a direct-access array cache.
 *
 * Target: com.hypixel.hytale.assetstore.map.BlockTypeAssetMap
 */
public class BlockTypeCacheTransformer extends BaseTransformer {

    @Override
    public String getName() {
        return "BlockTypeCache";
    }

    @Override
    public int priority() {
        return -150;
    }

    @Override
    protected boolean shouldTransform(String className) {
        return className.equals("com.hypixel.hytale.assetstore.map.BlockTypeAssetMap");
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new BlockTypeCacheClassVisitor(classWriter);
    }

    private static class BlockTypeCacheClassVisitor extends ClassVisitor {
        public BlockTypeCacheClassVisitor(ClassWriter classWriter) {
            super(ASM_VERSION, classWriter);
        }

        @Override
        public void visitEnd() {
            // Cache array (Object[] to avoid direct class reference issues)
            FieldVisitor fv = cv.visitField(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE,
                "$catalystTypeCache", "[Ljava/lang/Object;", null, null
            );
            if (fv != null) fv.visitEnd();

            // Cache size
            FieldVisitor fv2 = cv.visitField(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE,
                "$catalystCacheSize", "I", null, null
            );
            if (fv2 != null) fv2.visitEnd();

            // Cache hits
            FieldVisitor fv3 = cv.visitField(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "$catalystCacheHits", "J", null, null
            );
            if (fv3 != null) fv3.visitEnd();

            // Cache misses
            FieldVisitor fv4 = cv.visitField(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "$catalystCacheMisses", "J", null, null
            );
            if (fv4 != null) fv4.visitEnd();

            super.visitEnd();
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            if (name.equals("getAsset") && descriptor.startsWith("(I")) {
                return new CacheLookupMethodVisitor(mv);
            }
            return mv;
        }
    }

    private static class CacheLookupMethodVisitor extends MethodVisitor {
        public CacheLookupMethodVisitor(MethodVisitor mv) {
            super(ASM_VERSION, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();

            String ownerClass = "com/hypixel/hytale/assetstore/map/BlockTypeAssetMap";

            // Load cache array
            mv.visitFieldInsn(Opcodes.GETSTATIC, ownerClass, "$catalystTypeCache", "[Ljava/lang/Object;");
            mv.visitVarInsn(Opcodes.ASTORE, 2); 
            
            // Check cache validity (null check)
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            Label cacheNullLabel = new Label();
            mv.visitJumpInsn(Opcodes.IFNULL, cacheNullLabel);

            // Check bounds: 0 <= blockId < cache.length
            mv.visitVarInsn(Opcodes.ILOAD, 1);
            Label outOfBoundsLabel = new Label();
            mv.visitJumpInsn(Opcodes.IFLT, outOfBoundsLabel);

            mv.visitVarInsn(Opcodes.ILOAD, 1);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitInsn(Opcodes.ARRAYLENGTH);
            mv.visitJumpInsn(Opcodes.IF_ICMPGE, outOfBoundsLabel);

            // Fetch from cache
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitVarInsn(Opcodes.ILOAD, 1);
            mv.visitInsn(Opcodes.AALOAD);
            mv.visitVarInsn(Opcodes.ASTORE, 3);

            // Check if cached entry exists
            mv.visitVarInsn(Opcodes.ALOAD, 3);
            Label cacheMissLabel = new Label();
            mv.visitJumpInsn(Opcodes.IFNULL, cacheMissLabel);

            // Cache Hit: Increment hits and return
            mv.visitFieldInsn(Opcodes.GETSTATIC, ownerClass, "$catalystCacheHits", "J");
            mv.visitInsn(Opcodes.LCONST_1);
            mv.visitInsn(Opcodes.LADD);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, ownerClass, "$catalystCacheHits", "J");

            mv.visitVarInsn(Opcodes.ALOAD, 3);
            mv.visitInsn(Opcodes.ARETURN);

            // Cache Miss: Increment misses
            mv.visitLabel(cacheMissLabel);
            mv.visitFrame(Opcodes.F_APPEND, 2, new Object[]{"[Ljava/lang/Object;", "java/lang/Object"}, 0, null);
            mv.visitFieldInsn(Opcodes.GETSTATIC, ownerClass, "$catalystCacheMisses", "J");
            mv.visitInsn(Opcodes.LCONST_1);
            mv.visitInsn(Opcodes.LADD);
            mv.visitFieldInsn(Opcodes.PUTSTATIC, ownerClass, "$catalystCacheMisses", "J");
            
            Label continueLabel = new Label();
            mv.visitJumpInsn(Opcodes.GOTO, continueLabel);

            // Out of bounds handling
            mv.visitLabel(outOfBoundsLabel);
            mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"[Ljava/lang/Object;"}, 0, null);
            mv.visitJumpInsn(Opcodes.GOTO, continueLabel);

            // Null cache handling
            mv.visitLabel(cacheNullLabel);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

            // Proceed with original method
            mv.visitLabel(continueLabel);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        }
    }
}
