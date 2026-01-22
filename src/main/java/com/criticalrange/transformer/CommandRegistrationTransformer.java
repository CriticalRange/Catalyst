package com.criticalrange.transformer;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Injects Catalyst command registration into CommandManager.registerCommands().
 * 
 * <p>This allows the Catalyst command to be registered from the earlyplugins directory
 * without needing a separate copy in the mods directory.</p>
 */
public class CommandRegistrationTransformer extends BaseTransformer {

    private static final String TARGET_CLASS = "com/hypixel/hytale/server/core/command/system/CommandManager";
    private static final String CATALYST_COMMAND_CLASS = "com/criticalrange/command/CatalystCommand";
    private static final String CATALYST_EARLY_INIT_CLASS = "com/criticalrange/CatalystEarlyInit";

    @Override
    protected boolean shouldTransform(String className) {
        return TARGET_CLASS.equals(className);
    }

    @Override
    protected ClassVisitor createClassVisitor(ClassWriter classWriter, String className) {
        return new CommandManagerClassVisitor(classWriter);
    }

    @Override
    public String getName() {
        return "CommandRegistrationTransformer";
    }

    private static class CommandManagerClassVisitor extends ClassVisitor {
        public CommandManagerClassVisitor(ClassVisitor cv) {
            super(BaseTransformer.ASM_VERSION, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            
            // Inject at the end of registerCommands()
            if ("registerCommands".equals(name) && "()V".equals(descriptor)) {
                System.out.println("[Catalyst] CommandRegistrationTransformer: Instrumenting registerCommands");
                return new RegisterCommandsMethodVisitor(mv);
            }
            
            return mv;
        }
    }

    /**
     * Injects Catalyst command registration at the end of registerCommands().
     * 
     * Adds:
     *   CatalystEarlyInit.init();
     *   this.registerSystemCommand(new CatalystCommand());
     */
    private static class RegisterCommandsMethodVisitor extends MethodVisitor {
        public RegisterCommandsMethodVisitor(MethodVisitor mv) {
            super(BaseTransformer.ASM_VERSION, mv);
        }

        @Override
        public void visitInsn(int opcode) {
            // Before RETURN, inject our initialization and command registration
            if (opcode == Opcodes.RETURN) {
                // CatalystEarlyInit.init();
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, CATALYST_EARLY_INIT_CLASS, "init", "()V", false);
                
                // this.registerSystemCommand(new CatalystCommand());
                mv.visitVarInsn(Opcodes.ALOAD, 0); // this
                mv.visitTypeInsn(Opcodes.NEW, CATALYST_COMMAND_CLASS);
                mv.visitInsn(Opcodes.DUP);
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, CATALYST_COMMAND_CLASS, "<init>", "()V", false);
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TARGET_CLASS, "registerSystemCommand",
                    "(Lcom/hypixel/hytale/server/core/command/system/AbstractCommand;)V", false);
                
                System.out.println("[Catalyst] Injected CatalystCommand registration into CommandManager");
            }
            super.visitInsn(opcode);
        }
    }
}
