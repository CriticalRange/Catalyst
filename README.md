# Catalyst ⚡

**Catalyst** - The Hytale Modding Framework

A comprehensive toolkit for developing high-performance Hytale server mods with advanced features like bytecode transformation, performance optimization, and complete API documentation.

## ⚡ Features

- 🚀 **Performance Optimization** - Advanced techniques for optimizing server performance
- 🔧 **Early Plugins** - Class transformation for low-level modifications
- 📚 **Complete API Reference** - Comprehensive documentation of Hytale's server API
- 🛠️ **Modern Build System** - Gradle 9.2.1 with cross-platform support
- 🎯 **IDE Integration** - Ready-to-go configurations for IntelliJ, VSCode, Eclipse
- 🌍 **Multiplayer Focus** - Server-side modding (Hytale architecture)

## 📚 Documentation

### Core Documentation

- **[API Reference](docs/HYTALE_API_REFERENCE.md)** - Complete Hytale Server Plugin API documentation
- **[Early Plugins Guide](docs/ADVANCED_EARLY_PLUGINS.md)** - Bootstrap plugins and bytecode transformation
- **[Performance Optimization](docs/PERFORMANCE_OPTIMIZATIONS.md)** - Advanced performance tuning techniques

### Quick Links

- [Hytale Modding Discord](https://discord.gg/hytalemodding)
- [Hytale Official Documentation](https://hytale.com/documentation)
- [Template Generator](https://hytale-template.vercel.app) - Generate custom mod templates

## 🚀 Quick Start

### Prerequisites

- Java 25+
- Hytale (installed via official launcher)
- Gradle (included via wrapper)

### Installation

```bash
# Clone or download Catalyst
cd Catalyst

# Build the mod
./gradlew build

# Run server
./gradlew runServer
```

## 📖 Getting Started

### 1. Basic Plugin Structure

```java
package com.criticalrange;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import javax.annotation.Nonnull;

public class Catalyst extends JavaPlugin {

    public Catalyst(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        // Register commands and events here
        getCommandRegistry().registerCommand(
            new MyCommand("hello", "Says hello")
        );
    }

    @Override
    protected void start() {
        super.start();
        getLogger().info("Catalyst started!");
    }

    @Override
    protected void shutdown() {
        getLogger().info("Catalyst shutting down...");
    }
}
```

### 2. Creating Commands

```java
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.Message;
import javax.annotation.Nonnull;

public class MyCommand extends CommandBase {
    public MyCommand(String name, String description) {
        super(name, description);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        context.sendMessage(Message.raw("Hello from Catalyst!"));
    }
}
```

### 3. Handling Events

```java
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;

public class MyEventHandler {
    public static void onPlayerReady(PlayerReadyEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(Message.raw("Welcome to the server!"));
    }
}

// Register in setup():
getEventRegistry().registerGlobal(
    PlayerReadyEvent.class,
    MyEventHandler::onPlayerReady
);
```

## 🛠️ Gradle Tasks

### Build Tasks
```bash
./gradlew build          # Build the mod JAR
./gradlew jar            # Build JAR only
./gradlew clean          # Clean build outputs and remove mod JARs
```

### Hytale Tasks
```bash
./gradlew runServer      # Start Hytale server with mod
./gradlew runClient      # Launch official Hytale launcher
./gradlew copyJar        # Copy mod to official mods directory
./gradlew copyMod        # Copy mod to Hytale mods directory
./gradlew downloadAssets # Copy Assets.zip locally
```

### IDE Tasks
```bash
./gradlew ide            # Generate all IDE configurations
./gradlew vscode         # Generate VSCode configurations
./gradlew eclipse        # Generate Eclipse configurations
```

## 📁 Project Structure

```
Catalyst/
├── docs/                           # Documentation
│   ├── HYTALE_API_REFERENCE.md     # Complete API documentation
│   ├── ADVANCED_EARLY_PLUGINS.md   # Bootstrap/bytecode transformation
│   └── PERFORMANCE_OPTIMIZATIONS.md # Performance tuning guide
├── src/main/java/com/criticalrange/
│   └── Catalyst.java               # Main plugin class
├── src/main/resources/
│   └── manifest.json               # Mod manifest
├── build.gradle                    # Build configuration
├── gradle.properties               # Mod properties
├── settings.gradle                 # Project settings
├── gradlew                         # Unix Gradle wrapper
└── gradlew.bat                     # Windows Gradle wrapper
```

## 🔧 Configuration

### gradle.properties

```properties
group=com.criticalrange
name=Catalyst
version=1.0.0
java_version=25
mod_description=The Hytale Modding Framework
website=https://github.com/CriticalRange/catalyst
server_version=*
entry_point=com.criticalrange.Catalyst
```

## 💡 Advanced Features

### Early Plugins (Bootstrap)

Catalyst supports **Early Plugins** - special plugins that can transform bytecode as classes load:

```java
import com.hypixel.hytale.plugin.early.ClassTransformer;

public class MyTransformer implements ClassTransformer {
    @Override
    public byte[] transform(String className, String classLoaderName, byte[] classBytes) {
        // Transform classes here
        return classBytes;
    }

    @Override
    public int priority() {
        return 0; // Execution order
    }
}
```

**⚠️ Warning:** Early plugins are extremely powerful and should only be used when absolutely necessary. See [Early Plugins Guide](docs/ADVANCED_EARLY_PLUGINS.md) for details.

### Performance Optimization

Catalyst includes advanced performance optimization techniques:

- **Spatial Partitioning** - O(n²) → O(log n) entity lookups
- **Distance-Based Ticking** - Skip ticks for distant entities
- **Async Chunk Loading** - Non-blocking I/O
- **Memory Compression** - Compress inactive chunks
- **Packet Batching** - Reduce network overhead

See [Performance Optimization Guide](docs/PERFORMANCE_OPTIMIZATIONS.md) for details.

## 🎯 What Makes Catalyst Special

### 1. Complete API Documentation

Unlike other frameworks, Catalyst provides comprehensive documentation of:
- Every plugin class and method
- Command system internals
- Event system architecture
- Entity, chunk, and world management
- Registry systems
- Package structure and organization

### 2. Performance-First Design

Built from the ground up with performance in mind:
- Optimized build configuration
- Efficient class loading
- Smart caching strategies
- Memory management best practices

### 3. Advanced Capabilities

Not just a basic template - Catalyst includes:
- Early plugin support (bytecode transformation)
- Performance optimization techniques
- Advanced documentation for power users
- Real-world examples and patterns

## 🌐 Hytale Architecture Notes

**Important:** Hytale uses a unified server-side mod architecture:

- ✅ All mods are **server-side Java plugins**
- ✅ Client does NOT load mods directly
- ✅ Singleplayer uses an embedded server
- ✅ No client-side modding API exists

This means:
- **Server testing:** Use `./gradlew runServer`
- **Singleplayer testing:** Use `./gradlew runClient` (launches official launcher)
- Mods load from `mods/` directory (relative to server working directory)

## 🔒 Security & Best Practices

- Never load untrusted early plugins - they have full system access
- Always test early plugins on a separate server
- Keep backups of world data
- Profile before optimizing - measure twice, cut once
- Use standard plugins unless you absolutely need bytecode transformation

## 📝 License

This project is available under the **CC0 License** - feel free to use it however you like!

## 🙏 Acknowledgments

- Hypixel Studios for creating Hytale
- The Hytale modding community
- All contributors and testers

## 🔗 Links

- **GitHub:** https://github.com/CriticalRange/catalyst
- **Website:** https://hytale-template.vercel.app
- **Discord:** [Hytale Modding Discord](https://discord.gg/hytalemodding)
- **Hytale:** [https://hytale.com](https://hytale.com)

---

**Built with ❤️ for the Hytale modding community**

*"From forest to fortress, Catalyst sparks creativity"*
