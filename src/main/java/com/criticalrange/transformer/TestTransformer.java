package com.criticalrange.transformer;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ClassVisitor;

/**
 * Simple test transformer to verify early plugin loading works.
 * This prints a message immediately when the transformer class is loaded.
 */
public class TestTransformer extends BaseTransformer {

    // Static initializer runs as soon as the class is loaded by the classloader
    static {
        System.out.println("========================================");
        System.out.println("[Catalyst:TestTransformer] CLASS LOADED!");
        System.out.println("========================================");
        System.err.println("========================================");
        System.err.println("[Catalyst:TestTransformer] CLASS LOADED!");
        System.err.println("========================================");
    }

    @Override
    public String getName() {
        return "TestTransformer";
    }

    @Override
    protected boolean shouldTransform(String className) {
        // Only log for classes we actually care about (not spamming for every class)
        if (className.contains("TickingThread") || className.contains("EntityTickingSystem")) {
            System.out.println("[Catalyst:TestTransformer] Checking: " + className);
        }
        return false; // Don't actually transform anything
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return null;
    }
}
