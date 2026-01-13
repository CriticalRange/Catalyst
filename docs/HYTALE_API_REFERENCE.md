# Hytale Server Plugin API Reference

This document provides a comprehensive reference for the Hytale Server Plugin API based on decompilation of the HytaleServer.jar.

## Table of Contents

- [Plugin Structure](#plugin-structure)
- [Core Classes](#core-classes)
- [Lifecycle Methods](#lifecycle-methods)
- [Command System](#command-system)
- [Event System](#event-system)
- [Available Registries](#available-registries)
- [Common Events](#common-events)

---

## Plugin Structure

### JavaPlugin

All plugins must extend `JavaPlugin`:

```java
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import javax.annotation.Nonnull;

public class MyPlugin extends JavaPlugin {

    public MyPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        // Register commands and events here
    }

    @Override
    protected void start() {
        // Plugin startup logic
    }

    @Override
    protected void shutdown() {
        // Plugin cleanup
    }
}
```

---

## Core Classes

### PluginBase

The base class for all plugins provides access to:

**Key Methods:**
- `getLogger()` - Get the plugin's logger
- `getName()` - Get the plugin name
- `getIdentifier()` - Get the plugin identifier
- `getManifest()` - Get the plugin manifest
- `getDataDirectory()` - Get the plugin's data directory (Path)
- `getState()` - Get the current plugin state
- `isDisabled()` - Check if plugin is disabled
- `isEnabled()` - Check if plugin is enabled

**Registries:**
- `getCommandRegistry()` - Access to command registration
- `getEventRegistry()` - Access to event registration
- `getEntityRegistry()` - Access to entity registration
- `getBlockStateRegistry()` - Access to block state registration
- `getTaskRegistry()` - Access to task registration
- `getClientFeatureRegistry()` - Access to client feature registration
- `getEntityStoreRegistry()` - Access to entity store components
- `getChunkStoreRegistry()` - Access to chunk store components
- `getAssetRegistry()` - Access to asset registration

**Config Management:**
- `withConfig(BuilderCodec<T>)` - Register a config file
- `withConfig(String, BuilderCodec<T>)` - Register a named config file

---

## Lifecycle Methods

### setup()

Called when the plugin is being initialized. Use this to:
- Register commands
- Register event listeners
- Set up configurations
- Initialize registries

```java
@Override
protected void setup() {
    // Register a command
    getCommandRegistry().registerCommand(new MyCommand("mycommand", "Description"));

    // Register an event
    getEventRegistry().registerGlobal(PlayerReadyEvent.class, MyEvent::onPlayerReady);
}
```

### start()

Called when the plugin is fully loaded and ready to:
- Start tasks/schedulers
- Interact with the world
- Initialize systems

```java
@Override
protected void start() {
    super.start();
    getLogger().info("Plugin started!");

    // Start a repeating task
    getTaskRegistry().scheduleDelayed(this::myTask, Duration.ofSeconds(5));
}
```

### shutdown()

Called when the plugin is being unloaded. Use this to:
- Save data
- Cancel tasks
- Clean up resources

```java
@Override
protected void shutdown() {
    getLogger().info("Plugin shutting down...");
    // Cleanup code here
}
```

---

## Command System

### CommandBase

Base class for creating commands:

```java
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import javax.annotation.Nonnull;

public class MyCommand extends CommandBase {

    public MyCommand(String name, String description) {
        super(name, description);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        // Command logic here
        context.sendMessage(Message.raw("Hello from my command!"));
    }
}
```

**Constructors:**
- `CommandBase(String name)` - Command with no description
- `CommandBase(String name, String description)` - Command with description
- `CommandBase(String name, String description, boolean hidden)` - Command with visibility control

### CommandContext

Provides context about the command execution:

**Key Methods:**
- `sendMessage(Message)` - Send a message to the command sender
- `sender()` - Get the command sender
- `isPlayer()` - Check if sender is a player
- `senderAs(Class<T>)` - Cast sender to specific type
- `senderAsPlayerRef()` - Get sender as a player entity reference
- `getInputString()` - Get the full input string
- `get(Argument<?, DataType>)` - Get parsed argument value
- `getInput(Argument<?, ?>)` - Get raw input for argument
- `provided(Argument<?, ?>)` - Check if argument was provided

### Message

Used to send messages to players:

```java
import com.hypixel.hytale.server.core.Message;

// Plain text message
Message.raw("Hello, world!")

// Formatted message (formatting codes vary)
Message.text("Hello ").color(Color.YELLOW)
    .append(Message.text("World!").color(Color.AQUA))
```

---

## Event System

### Registering Events

```java
@Override
protected void setup() {
    // Register using method reference
    getEventRegistry().registerGlobal(PlayerReadyEvent.class, MyEventClass::onPlayerReady);

    // Register using lambda
    getEventRegistry().registerGlobal(PlayerChatEvent.class, event -> {
        Player player = event.getPlayer();
        getLogger().info("Player said: " + event.getMessage());
    });
}
```

### Creating Event Handlers

```java
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.Message;

public class MyEventHandler {

    public static void onPlayerReady(PlayerReadyEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(Message.raw("Welcome to the server!"));
    }
}
```

---

## Available Registries

### CommandRegistry

Register and manage commands:
```java
getCommandRegistry().registerCommand(new MyCommand("cmd", "Description"));
```

### EventRegistry

Register event listeners:
```java
getEventRegistry().registerGlobal(PlayerReadyEvent.class, this::onPlayerReady);
```

### EntityRegistry

Register custom entity types:
```java
getEntityRegistry().register(MyCustomEntity.class);
```

### BlockStateRegistry

Register custom block states:
```java
getBlockStateRegistry().register(customBlockState);
```

### TaskRegistry

Schedule tasks:
```java
getTaskRegistry().scheduleDelayed(() -> {
    // Run once after delay
}, Duration.ofSeconds(10));

getTaskRegistry().scheduleRepeating(() -> {
    // Run repeatedly
}, Duration.ofSeconds(0), Duration.ofSeconds(5));
```

### AssetRegistry

Register custom assets:
```java
getAssetRegistry().registerAsset("myasset.json", assetData);
```

---

## Common Events

### Player Events

**PlayerReadyEvent**
- Fired when a player is fully ready to play
- Usage: Welcome messages, initial setup

```java
public static void onPlayerReady(PlayerReadyEvent event) {
    Player player = event.getPlayer();
    // Player is ready
}
```

**PlayerConnectEvent**
- Fired when a player connects
- Early in connection process

**PlayerChatEvent**
- Fired when a player sends a chat message
- Can be cancelled

```java
public static void onChat(PlayerChatEvent event) {
    String message = event.getMessage();
    Player player = event.getPlayer();

    // Cancel inappropriate messages
    if (message.contains("badword")) {
        event.setCancelled(true);
        player.sendMessage(Message.raw("Watch your language!"));
    }
}
```

**PlayerDisconnectEvent**
- Fired when a player disconnects
- Usage: Save player data, cleanup

### World Events

**AddWorldEvent**
- Fired when a world is added

**RemoveWorldEvent**
- Fired when a world is removed

**ChunkLoadEvent** / **ChunkUnloadEvent**
- Fired when chunks are loaded/unloaded

### Entity Events

Various entity-related events exist for:
- Damage events
- Death events
- Spawn events
- Interaction events

---

## Utility Classes

### Logger

```java
getLogger().info("Info message");
getLogger().warn("Warning message");
getLogger().error("Error message");
getLogger().debug("Debug message");
```

### Data Directory

Each plugin has its own data directory:

```java
Path dataDir = getDataDirectory();
Path configFile = dataDir.resolve("config.json");

// Read/write files
Files.writeString(configFile, jsonContent);
String content = Files.readString(configFile);
```

---

## Full Example Plugin

```java
package com.example.myplugin;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import javax.annotation.Nonnull;

public class MyPlugin extends JavaPlugin {

    public MyPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        // Register command
        getCommandRegistry().registerCommand(
            new MyCommand("hello", "Says hello to the player")
        );

        // Register event
        getEventRegistry().registerGlobal(
            PlayerReadyEvent.class,
            MyPlugin::onPlayerReady
        );
    }

    @Override
    protected void start() {
        super.start();
        getLogger().info("MyPlugin has started!");
    }

    @Override
    protected void shutdown() {
        getLogger().info("MyPlugin is shutting down...");
    }

    private static void onPlayerReady(PlayerReadyEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(Message.raw("Welcome! Type /hello for a greeting."));
    }

    public static class MyCommand extends CommandBase {
        public MyCommand(String name, String description) {
            super(name, description);
        }

        @Override
        protected void executeSync(@Nonnull CommandContext context) {
            context.sendMessage(Message.raw("Hello there! Thanks for using my plugin."));
        }
    }
}
```

---

## Package Structure

**Core Plugin:**
- `com.hypixel.hytale.server.core.plugin.JavaPlugin` - Base plugin class
- `com.hypixel.hytale.server.core.plugin.PluginBase` - Base with all core methods
- `com.hypixel.hytale.server.core.plugin.JavaPluginInit` - Plugin initialization

**Commands:**
- `com.hypixel.hytale.server.core.command.system.basecommands.CommandBase` - Base command class
- `com.hypixel.hytale.server.core.command.system.CommandContext` - Command execution context
- `com.hypixel.hytale.server.core.command.system.CommandSender` - Command sender interface
- `com.hypixel.hytale.server.core.command.system.arguments` - Command argument system

**Events:**
- `com.hypixel.hytale.server.core.event.events.player.*` - Player events
- `com.hypixel.hytale.server.core.event.events.entity.*` - Entity events
- `com.hypixel.hytale.server.core.event.events.world.*` - World events
- `com.hypixel.hytale.event.EventRegistry` - Event registration

**Entities:**
- `com.hypixel.hytale.server.core.entity.entities.Player` - Player entity
- `com.hypixel.hytale.server.core.modules.entity.EntityRegistry` - Entity registration

**Utilities:**
- `com.hypixel.hytale.server.core.Message` - Message formatting
- `com.hypixel.hytale.logger.HytaleLogger` - Plugin logging
- `com.hypixel.hytale.server.core.task.TaskRegistry` - Task scheduling

---

## Early Plugins (Bootstrap/Class Transformation)

### What Are Early Plugins?

**Early Plugins** (also called Bootstrap Plugins) are special plugins that operate **outside of the standard plugin environment**. They have the ability to perform low-level bytecode transformation on classes as they are loaded.

⚠️ **WARNING:** These should only be used when absolutely necessary! They can:
- Break the server if used incorrectly
- Create compatibility issues with other plugins
- Make debugging extremely difficult
- Void server stability guarantees

### When to Use Early Plugins

Early plugins are typically used for:
- **Core API modifications** - When you need to modify how the server itself works
- **Mixin/interception** - When you need to intercept method calls that aren't event-based
- **Access restriction bypass** - When you need access to non-public APIs
- **Performance optimization** - When events add too much overhead

**You should NOT use early plugins for:**
- Normal gameplay features
- Adding commands or events (use the standard plugin system)
- Most modding tasks

### Early Plugin System

The early plugin system consists of three main components:

**Location:**
```java
// Early plugins are loaded from a special directory
Path EARLY_PLUGINS_PATH = Path.of("early-plugins"); // Relative to server root
```

**Core Interfaces:**

```java
package com.hypixel.hytale.plugin.early;

/**
 * Interface for class transformers.
 * Implement this to modify bytecode as classes are loaded.
 */
public interface ClassTransformer {
    /**
     * Transform a class's bytecode before it's defined.
     *
     * @param className The binary name of the class (e.g., "com/example/MyClass")
     * @param classLoaderName The name of the class loader loading the class
     * @param classBytes The original bytecode of the class
     * @return The transformed bytecode (or original if no changes)
     */
    byte[] transform(String className, String classLoaderName, byte[] classBytes);

    /**
     * Priority determines the order transformers are applied.
     * Lower values = earlier execution.
     * @return The priority (default is 0)
     */
    default int priority() {
        return 0;
    }
}
```

### Creating an Early Plugin

**Step 1: Create your transformer class**

```java
package com.example.earlyplugin;

import com.hypixel.hytale.plugin.early.ClassTransformer;

public class MyTransformer implements ClassTransformer {

    @Override
    public int priority() {
        // Run early (lower numbers = earlier)
        return -100;
    }

    @Override
    public byte[] transform(String className, String classLoaderName, byte[] classBytes) {
        // Only transform specific classes
        if (!className.startsWith("com/hypixel/hytale/server/core/")) {
            return classBytes; // Return original
        }

        // Transform the bytecode
        // You'll need a bytecode manipulation library like:
        // - ASM (org.ow2.asm:asm)
        // - Javassist
        // - Byte Buddy

        try {
            // Example: Use ASM to modify the class
            ClassReader reader = new ClassReader(classBytes);
            ClassWriter writer = new ClassWriter(reader, 0);
            ClassVisitor visitor = new MyClassVisitor(writer);

            reader.accept(visitor, 0);
            return writer.toByteArray();
        } catch (Exception e) {
            // On error, return original bytes to prevent crashes
            return classBytes;
        }
    }
}
```

**Step 2: Register your transformer**

Early plugins use a special entry point. Create a file:
```
META-INF/services/com.hypixel.hytale.plugin.early.ClassTransformer
```

With the contents:
```
com.example.earlyplugin.MyTransformer
```

**Step 3: Deploy to early-plugins directory**

```bash
# Build your JAR
./gradlew build

# Copy to the early-plugins directory (NOT mods!)
cp build/libs/MyEarlyPlugin-1.0.0.jar \
   ~/.var/app/com.hypixel.HytaleLauncher/data/Hytale/install/early-plugins/
```

### Class Loading Security

The `TransformingClassLoader` has security restrictions:

```java
// Secure packages that cannot be transformed
private static final Set<String> SECURE_PACKAGE_PREFIXES = Set.of(
    "java/lang",
    "javax/",
    "sun/",
    "com/hypixel/hytale/plugin/early/"  // Can't transform the transformer system itself
);
```

Attempts to transform classes in secure packages will be ignored.

### Bytecode Manipulation Libraries

**Using ASM (Recommended):**

```gradle
dependencies {
    implementation 'org.ow2.asm:asm:9.6'
    implementation 'org.ow2.asm:asm-commons:9.6'
}
```

```java
import org.objectweb.asm.*;

public class MyClassVisitor extends ClassVisitor {

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor,
                                     String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        return new MyMethodVisitor(mv);
    }
}

class MyMethodVisitor extends MethodVisitor {

    @Override
    public void visitCode() {
        // Inject code at the beginning of methods
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn("Method called!");
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        super.visitCode();
    }
}
```

### Example: Method Interception

```java
public class InterceptionTransformer implements ClassTransformer {

    @Override
    public byte[] transform(String className, String classLoaderName, byte[] classBytes) {
        // Intercept all PlayerChatEvent calls
        if (className.equals("com/hypixel/hytale/server/core/event/events/player/PlayerChatEvent")) {
            ClassReader reader = new ClassReader(classBytes);
            ClassWriter writer = new ClassWriter(reader, 0);
            ClassVisitor visitor = new ChatEventInterceptor(writer);
            reader.accept(visitor, 0);
            return writer.toByteArray();
        }
        return classBytes;
    }

    static class ChatEventInterceptor extends ClassVisitor {
        // Intercept and modify methods
    }
}
```

### Loading Order

1. **Early plugins** are loaded first (before standard plugins)
2. Transformers are applied in priority order (lower numbers first)
3. Standard plugins load after all transformations complete

### Debugging

```java
@Override
public byte[] transform(String className, String classLoaderName, byte[] classBytes) {
    // Log to help debug
    System.err.println("[EarlyPlugin] Transforming: " + className);

    byte[] transformed = doTransform(className, classBytes);

    System.err.println("[EarlyPlugin] Transformed: " + className +
                       " (size: " + classBytes.length + " -> " + transformed.length + ")");

    return transformed;
}
```

### Security Considerations

- Early plugins run with full system access
- They can bypass normal permission checks
- They can access private fields and methods
- They should NEVER be loaded from untrusted sources

### Best Practices

1. **Always return the original bytes on error**
   ```java
   try {
       return transformBytes(classBytes);
   } catch (Throwable t) {
       t.printStackTrace();
       return classBytes;  // Don't crash the server!
   }
   ```

2. **Check class names before transforming**
   ```java
   if (!shouldTransform(className)) {
       return classBytes;
   }
   ```

3. **Use minimal priority adjustments**
   ```java
   @Override
   public int priority() {
       return 0;  // Use 0 unless you have a specific reason
   }
   ```

4. **Document what you're modifying**
   ```java
   /**
    * Transforms PlayerChatEvent to add profanity filtering.
    * Priority: 100 (run after other transformers)
    */
   public class ProfanityFilterTransformer implements ClassTransformer { ... }
   ```

---

## Notes

- All plugin JARs must include a valid `manifest.json` in `src/main/resources/`
- The manifest is automatically expanded with properties from `gradle.properties`
- Standard plugins run in a separate ClassLoader to avoid conflicts
- Early plugins use a shared ClassLoader and can affect ALL classes
- The server uses Java 25+ (as of latest version)
- All mods are server-side - there is no client-side modding API
- Early plugins are advanced tools that should only be used when standard plugins aren't sufficient

---

For the latest updates and community support, visit:
- [Hytale Modding Discord](https://discord.gg/hytalemodding)
- [Hytale Documentation](https://hytale.com/documentation)
