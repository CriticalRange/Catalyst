package com.criticalrange.transformer;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/**
 * LocalCachedChunkAccessor Optimization Transformer.
 *
 * <p>Optimizes the LocalCachedChunkAccessor which is used extensively during
 * lighting calculations and chunk operations. Uses Tree API for reliable
 * method replacement.</p>
 *
 * <p>Target: LocalCachedChunkAccessor - optimizes chunk lookup with 2-slot LRU cache</p>
 *
 * <p>Expected impact: 5-10% faster chunk access during lighting calculations</p>
 */
public class LocalCachedChunkAccessorTransformer extends BaseTransformer {

    private static final String TARGET_CLASS = "com.hypixel.hytale.server.core.universe.world.accessor.LocalCachedChunkAccessor";
    private static final String TARGET_CLASS_INTERNAL = "com/hypixel/hytale/server/core/universe/world/accessor/LocalCachedChunkAccessor";
    
    private static final String WORLD_CHUNK_CLASS = "com/hypixel/hytale/server/core/universe/world/chunk/WorldChunk";
    private static final String CHUNK_ACCESSOR_CLASS = "com/hypixel/hytale/server/core/universe/world/accessor/ChunkAccessor";
    private static final String ICHUNK_ACCESSOR_SYNC_CLASS = "com/hypixel/hytale/server/core/universe/world/accessor/IChunkAccessorSync";

    public static final String ENABLED_FIELD = "$catalystLocalChunkCacheEnabled";
    private static final String CACHED_INDEX_1 = "$catalystCachedChunkIndex1";
    private static final String CACHED_CHUNK_1 = "$catalystCachedChunk1";
    private static final String CACHED_INDEX_2 = "$catalystCachedChunkIndex2";
    private static final String CACHED_CHUNK_2 = "$catalystCachedChunk2";

    @Override
    public String getName() {
        return "LocalCachedChunkAccessor";
    }

    @Override
    public int priority() {
        return -58;
    }

    @Override
    protected boolean shouldTransform(String className) {
        return className.equals(TARGET_CLASS);
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return classWriter;
    }

    @Override
    public byte[] transform(String className, String classLoaderName, byte[] classBytes) {
        if (!enabled || !shouldTransform(className)) {
            return classBytes;
        }

        long startTime = System.nanoTime();

        try {
            ClassReader classReader = new ClassReader(classBytes);
            ClassNode classNode = new ClassNode();
            classReader.accept(classNode, 0);

            addFields(classNode);
            modifyStaticInit(classNode);
            replaceGetChunkIfInMemoryMethod(classNode);

            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            classNode.accept(classWriter);

            transformedCount++;
            totalTransformTime += (System.nanoTime() - startTime);
            logTransformation(className);

            return classWriter.toByteArray();

        } catch (Exception e) {
            System.err.println("[Catalyst:" + getName() + "] Error transforming " + className + ": " + e.getMessage());
            e.printStackTrace();
            return classBytes;
        }
    }

    private static void addFields(ClassNode classNode) {
        classNode.fields.add(new FieldNode(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_VOLATILE,
            ENABLED_FIELD, "Z", null, null
        ));
        classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, CACHED_INDEX_1, "I", null, null));
        classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, CACHED_CHUNK_1, "L" + WORLD_CHUNK_CLASS + ";", null, null));
        classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, CACHED_INDEX_2, "I", null, null));
        classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, CACHED_CHUNK_2, "L" + WORLD_CHUNK_CLASS + ";", null, null));
    }

    private static void modifyStaticInit(ClassNode classNode) {
        MethodNode clinit = null;
        for (MethodNode method : classNode.methods) {
            if (method.name.equals("<clinit>")) {
                clinit = method;
                break;
            }
        }

        InsnList initInsns = new InsnList();
        initInsns.add(new InsnNode(Opcodes.ICONST_0));
        initInsns.add(new FieldInsnNode(Opcodes.PUTSTATIC, TARGET_CLASS_INTERNAL, ENABLED_FIELD, "Z"));

        if (clinit == null) {
            clinit = new MethodNode(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
            clinit.instructions.add(initInsns);
            clinit.instructions.add(new InsnNode(Opcodes.RETURN));
            classNode.methods.add(clinit);
        } else {
            clinit.instructions.insert(initInsns);
        }
    }

    private static void replaceGetChunkIfInMemoryMethod(ClassNode classNode) {
        String methodName = "getChunkIfInMemory";
        String methodDesc = "(II)L" + WORLD_CHUNK_CLASS + ";";

        MethodNode targetMethod = null;
        for (MethodNode method : classNode.methods) {
            if (method.name.equals(methodName) && method.desc.equals(methodDesc)) {
                targetMethod = method;
                break;
            }
        }

        if (targetMethod == null) {
            System.err.println("[Catalyst:LocalCachedChunkAccessor] Method getChunkIfInMemory(II) not found");
            return;
        }

        targetMethod.instructions.clear();
        targetMethod.tryCatchBlocks.clear();
        targetMethod.localVariables = null;

        InsnList insns = new InsnList();

        LabelNode skipCache = new LabelNode();
        LabelNode checkSlot2 = new LabelNode();
        LabelNode cacheMiss = new LabelNode();
        LabelNode cacheMissWithPop = new LabelNode();
        LabelNode outsideBounds = new LabelNode();
        LabelNode updateCache = new LabelNode();
        LabelNode fetchFromDelegate = new LabelNode();

        // Check if enabled
        insns.add(new FieldInsnNode(Opcodes.GETSTATIC, TARGET_CLASS_INTERNAL, ENABLED_FIELD, "Z"));
        insns.add(new JumpInsnNode(Opcodes.IFEQ, skipCache));

        // int packedIndex = (x << 16) | (z & 0xFFFF)
        insns.add(new VarInsnNode(Opcodes.ILOAD, 1));
        insns.add(new IntInsnNode(Opcodes.BIPUSH, 16));
        insns.add(new InsnNode(Opcodes.ISHL));
        insns.add(new VarInsnNode(Opcodes.ILOAD, 2));
        insns.add(new LdcInsnNode(0xFFFF));
        insns.add(new InsnNode(Opcodes.IAND));
        insns.add(new InsnNode(Opcodes.IOR));
        insns.add(new VarInsnNode(Opcodes.ISTORE, 3));

        // Check slot 1
        insns.add(new VarInsnNode(Opcodes.ILOAD, 3));
        insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insns.add(new FieldInsnNode(Opcodes.GETFIELD, TARGET_CLASS_INTERNAL, CACHED_INDEX_1, "I"));
        insns.add(new JumpInsnNode(Opcodes.IF_ICMPNE, checkSlot2));
        insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insns.add(new FieldInsnNode(Opcodes.GETFIELD, TARGET_CLASS_INTERNAL, CACHED_CHUNK_1, "L" + WORLD_CHUNK_CLASS + ";"));
        insns.add(new InsnNode(Opcodes.DUP));
        insns.add(new JumpInsnNode(Opcodes.IFNULL, cacheMissWithPop));
        insns.add(new InsnNode(Opcodes.ARETURN));

        // Check slot 2
        insns.add(checkSlot2);
        insns.add(new VarInsnNode(Opcodes.ILOAD, 3));
        insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insns.add(new FieldInsnNode(Opcodes.GETFIELD, TARGET_CLASS_INTERNAL, CACHED_INDEX_2, "I"));
        insns.add(new JumpInsnNode(Opcodes.IF_ICMPNE, cacheMiss));
        insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insns.add(new FieldInsnNode(Opcodes.GETFIELD, TARGET_CLASS_INTERNAL, CACHED_CHUNK_2, "L" + WORLD_CHUNK_CLASS + ";"));
        insns.add(new InsnNode(Opcodes.DUP));
        insns.add(new JumpInsnNode(Opcodes.IFNULL, cacheMissWithPop));
        insns.add(new InsnNode(Opcodes.ARETURN));

        // Cache miss with null on stack - pop it first
        insns.add(cacheMissWithPop);
        insns.add(new InsnNode(Opcodes.POP));

        // Cache miss - stack is clean here
        insns.add(cacheMiss);

        // xOffset = x - minX
        insns.add(new VarInsnNode(Opcodes.ILOAD, 1));
        insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insns.add(new FieldInsnNode(Opcodes.GETFIELD, TARGET_CLASS_INTERNAL, "minX", "I"));
        insns.add(new InsnNode(Opcodes.ISUB));
        insns.add(new VarInsnNode(Opcodes.ISTORE, 4));

        // zOffset = z - minZ
        insns.add(new VarInsnNode(Opcodes.ILOAD, 2));
        insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insns.add(new FieldInsnNode(Opcodes.GETFIELD, TARGET_CLASS_INTERNAL, "minZ", "I"));
        insns.add(new InsnNode(Opcodes.ISUB));
        insns.add(new VarInsnNode(Opcodes.ISTORE, 5));

        // Bounds checks
        insns.add(new VarInsnNode(Opcodes.ILOAD, 4));
        insns.add(new JumpInsnNode(Opcodes.IFLT, outsideBounds));
        insns.add(new VarInsnNode(Opcodes.ILOAD, 4));
        insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insns.add(new FieldInsnNode(Opcodes.GETFIELD, TARGET_CLASS_INTERNAL, "length", "I"));
        insns.add(new JumpInsnNode(Opcodes.IF_ICMPGE, outsideBounds));
        insns.add(new VarInsnNode(Opcodes.ILOAD, 5));
        insns.add(new JumpInsnNode(Opcodes.IFLT, outsideBounds));
        insns.add(new VarInsnNode(Opcodes.ILOAD, 5));
        insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insns.add(new FieldInsnNode(Opcodes.GETFIELD, TARGET_CLASS_INTERNAL, "length", "I"));
        insns.add(new JumpInsnNode(Opcodes.IF_ICMPGE, outsideBounds));

        // arrayIndex = xOffset * length + zOffset
        insns.add(new VarInsnNode(Opcodes.ILOAD, 4));
        insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insns.add(new FieldInsnNode(Opcodes.GETFIELD, TARGET_CLASS_INTERNAL, "length", "I"));
        insns.add(new InsnNode(Opcodes.IMUL));
        insns.add(new VarInsnNode(Opcodes.ILOAD, 5));
        insns.add(new InsnNode(Opcodes.IADD));
        insns.add(new VarInsnNode(Opcodes.ISTORE, 6));

        // result = chunks[arrayIndex]
        insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insns.add(new FieldInsnNode(Opcodes.GETFIELD, TARGET_CLASS_INTERNAL, "chunks", "[L" + WORLD_CHUNK_CLASS + ";"));
        insns.add(new VarInsnNode(Opcodes.ILOAD, 6));
        insns.add(new InsnNode(Opcodes.AALOAD));
        insns.add(new VarInsnNode(Opcodes.ASTORE, 7));

        // if result != null, go to updateCache
        insns.add(new VarInsnNode(Opcodes.ALOAD, 7));
        insns.add(new JumpInsnNode(Opcodes.IFNONNULL, updateCache));

        // Fetch from delegate and store in array
        insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insns.add(new FieldInsnNode(Opcodes.GETFIELD, TARGET_CLASS_INTERNAL, "chunks", "[L" + WORLD_CHUNK_CLASS + ";"));
        insns.add(new VarInsnNode(Opcodes.ILOAD, 6));
        insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insns.add(new FieldInsnNode(Opcodes.GETFIELD, TARGET_CLASS_INTERNAL, "delegate", "L" + CHUNK_ACCESSOR_CLASS + ";"));
        insns.add(new VarInsnNode(Opcodes.ILOAD, 1));
        insns.add(new VarInsnNode(Opcodes.ILOAD, 2));
        insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/hypixel/hytale/math/util/ChunkUtil", "indexChunk", "(II)J", false));
        insns.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, ICHUNK_ACCESSOR_SYNC_CLASS, "getChunkIfInMemory", "(J)Lcom/hypixel/hytale/server/core/universe/world/accessor/BlockAccessor;", true));
        insns.add(new TypeInsnNode(Opcodes.CHECKCAST, WORLD_CHUNK_CLASS));
        insns.add(new InsnNode(Opcodes.DUP_X2));
        insns.add(new InsnNode(Opcodes.AASTORE));
        insns.add(new VarInsnNode(Opcodes.ASTORE, 7));
        insns.add(new JumpInsnNode(Opcodes.GOTO, updateCache));

        // Outside bounds - delegate only
        insns.add(outsideBounds);
        insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insns.add(new FieldInsnNode(Opcodes.GETFIELD, TARGET_CLASS_INTERNAL, "delegate", "L" + CHUNK_ACCESSOR_CLASS + ";"));
        insns.add(new VarInsnNode(Opcodes.ILOAD, 1));
        insns.add(new VarInsnNode(Opcodes.ILOAD, 2));
        insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/hypixel/hytale/math/util/ChunkUtil", "indexChunk", "(II)J", false));
        insns.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, ICHUNK_ACCESSOR_SYNC_CLASS, "getChunkIfInMemory", "(J)Lcom/hypixel/hytale/server/core/universe/world/accessor/BlockAccessor;", true));
        insns.add(new TypeInsnNode(Opcodes.CHECKCAST, WORLD_CHUNK_CLASS));
        insns.add(new VarInsnNode(Opcodes.ASTORE, 7));

        // Update cache
        insns.add(updateCache);
        insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insns.add(new FieldInsnNode(Opcodes.GETFIELD, TARGET_CLASS_INTERNAL, CACHED_INDEX_1, "I"));
        insns.add(new FieldInsnNode(Opcodes.PUTFIELD, TARGET_CLASS_INTERNAL, CACHED_INDEX_2, "I"));
        insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insns.add(new FieldInsnNode(Opcodes.GETFIELD, TARGET_CLASS_INTERNAL, CACHED_CHUNK_1, "L" + WORLD_CHUNK_CLASS + ";"));
        insns.add(new FieldInsnNode(Opcodes.PUTFIELD, TARGET_CLASS_INTERNAL, CACHED_CHUNK_2, "L" + WORLD_CHUNK_CLASS + ";"));
        insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insns.add(new VarInsnNode(Opcodes.ILOAD, 3));
        insns.add(new FieldInsnNode(Opcodes.PUTFIELD, TARGET_CLASS_INTERNAL, CACHED_INDEX_1, "I"));
        insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insns.add(new VarInsnNode(Opcodes.ALOAD, 7));
        insns.add(new FieldInsnNode(Opcodes.PUTFIELD, TARGET_CLASS_INTERNAL, CACHED_CHUNK_1, "L" + WORLD_CHUNK_CLASS + ";"));
        insns.add(new VarInsnNode(Opcodes.ALOAD, 7));
        insns.add(new InsnNode(Opcodes.ARETURN));

        // skipCache - original logic
        insns.add(skipCache);
        // xOffset = x - minX
        insns.add(new VarInsnNode(Opcodes.ILOAD, 1));
        insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insns.add(new FieldInsnNode(Opcodes.GETFIELD, TARGET_CLASS_INTERNAL, "minX", "I"));
        insns.add(new InsnNode(Opcodes.ISUB));
        insns.add(new VarInsnNode(Opcodes.ISTORE, 3));
        // zOffset = z - minZ
        insns.add(new VarInsnNode(Opcodes.ILOAD, 2));
        insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insns.add(new FieldInsnNode(Opcodes.GETFIELD, TARGET_CLASS_INTERNAL, "minZ", "I"));
        insns.add(new InsnNode(Opcodes.ISUB));
        insns.add(new VarInsnNode(Opcodes.ISTORE, 4));

        LabelNode origOutside = new LabelNode();
        // Bounds checks
        insns.add(new VarInsnNode(Opcodes.ILOAD, 3));
        insns.add(new JumpInsnNode(Opcodes.IFLT, origOutside));
        insns.add(new VarInsnNode(Opcodes.ILOAD, 3));
        insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insns.add(new FieldInsnNode(Opcodes.GETFIELD, TARGET_CLASS_INTERNAL, "length", "I"));
        insns.add(new JumpInsnNode(Opcodes.IF_ICMPGE, origOutside));
        insns.add(new VarInsnNode(Opcodes.ILOAD, 4));
        insns.add(new JumpInsnNode(Opcodes.IFLT, origOutside));
        insns.add(new VarInsnNode(Opcodes.ILOAD, 4));
        insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insns.add(new FieldInsnNode(Opcodes.GETFIELD, TARGET_CLASS_INTERNAL, "length", "I"));
        insns.add(new JumpInsnNode(Opcodes.IF_ICMPGE, origOutside));

        // arrayIndex = xOffset * length + zOffset
        insns.add(new VarInsnNode(Opcodes.ILOAD, 3));
        insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insns.add(new FieldInsnNode(Opcodes.GETFIELD, TARGET_CLASS_INTERNAL, "length", "I"));
        insns.add(new InsnNode(Opcodes.IMUL));
        insns.add(new VarInsnNode(Opcodes.ILOAD, 4));
        insns.add(new InsnNode(Opcodes.IADD));
        insns.add(new VarInsnNode(Opcodes.ISTORE, 5));

        // result = chunks[arrayIndex]
        insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insns.add(new FieldInsnNode(Opcodes.GETFIELD, TARGET_CLASS_INTERNAL, "chunks", "[L" + WORLD_CHUNK_CLASS + ";"));
        insns.add(new VarInsnNode(Opcodes.ILOAD, 5));
        insns.add(new InsnNode(Opcodes.AALOAD));
        insns.add(new VarInsnNode(Opcodes.ASTORE, 6));

        LabelNode origReturn = new LabelNode();
        insns.add(new VarInsnNode(Opcodes.ALOAD, 6));
        insns.add(new JumpInsnNode(Opcodes.IFNONNULL, origReturn));

        // Fetch and store
        insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insns.add(new FieldInsnNode(Opcodes.GETFIELD, TARGET_CLASS_INTERNAL, "chunks", "[L" + WORLD_CHUNK_CLASS + ";"));
        insns.add(new VarInsnNode(Opcodes.ILOAD, 5));
        insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insns.add(new FieldInsnNode(Opcodes.GETFIELD, TARGET_CLASS_INTERNAL, "delegate", "L" + CHUNK_ACCESSOR_CLASS + ";"));
        insns.add(new VarInsnNode(Opcodes.ILOAD, 1));
        insns.add(new VarInsnNode(Opcodes.ILOAD, 2));
        insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/hypixel/hytale/math/util/ChunkUtil", "indexChunk", "(II)J", false));
        insns.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, ICHUNK_ACCESSOR_SYNC_CLASS, "getChunkIfInMemory", "(J)Lcom/hypixel/hytale/server/core/universe/world/accessor/BlockAccessor;", true));
        insns.add(new TypeInsnNode(Opcodes.CHECKCAST, WORLD_CHUNK_CLASS));
        insns.add(new InsnNode(Opcodes.DUP_X2));
        insns.add(new InsnNode(Opcodes.AASTORE));
        insns.add(new InsnNode(Opcodes.ARETURN));

        insns.add(origReturn);
        insns.add(new VarInsnNode(Opcodes.ALOAD, 6));
        insns.add(new InsnNode(Opcodes.ARETURN));

        // Outside bounds
        insns.add(origOutside);
        insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insns.add(new FieldInsnNode(Opcodes.GETFIELD, TARGET_CLASS_INTERNAL, "delegate", "L" + CHUNK_ACCESSOR_CLASS + ";"));
        insns.add(new VarInsnNode(Opcodes.ILOAD, 1));
        insns.add(new VarInsnNode(Opcodes.ILOAD, 2));
        insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/hypixel/hytale/math/util/ChunkUtil", "indexChunk", "(II)J", false));
        insns.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, ICHUNK_ACCESSOR_SYNC_CLASS, "getChunkIfInMemory", "(J)Lcom/hypixel/hytale/server/core/universe/world/accessor/BlockAccessor;", true));
        insns.add(new TypeInsnNode(Opcodes.CHECKCAST, WORLD_CHUNK_CLASS));
        insns.add(new InsnNode(Opcodes.ARETURN));

        targetMethod.instructions = insns;
        targetMethod.maxStack = 8;
        targetMethod.maxLocals = 8;
    }
}
