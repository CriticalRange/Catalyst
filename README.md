# Catalyst ⚡

**Catalyst** - Hytale Performance Optimization Mod

A high-performance Early Plugin that optimizes Hytale server performance through bytecode transformation and class transformers.

## ⚡ Features

- 🚀 **Tick Rate Optimization** - Skip ticking for distant entities, adaptive tick rates
- 🎯 **Entity Tracking Optimization** - Spatial partitioning (O(n) → O(log n))
- 📦 **Chunk Loading & Caching** - Async loading, predictive caching, memory compression
- 🌐 **Network Packet Batching** - Reduce TCP overhead, batch small packets
- 💾 **Memory Management** - Object pooling, off-heap memory
- ⏱️ **Thread Pool Optimization** - Custom pools for CPU-bound vs IO-bound tasks
- 📊 **Profiling & Metrics** - Inject timing into critical paths

## 📍 Hytale Server JAR Location

The `HytaleServer.jar` is the main API reference for mod development. For Flatpak installations:

```
~/.var/app/com.hypixel.HytaleLauncher/data/Hytale/install/release/package/game/latest/Server/HytaleServer.jar
```

### Key Paths (Flatpak)

| Purpose | Path |
|---------|------|
| **Server JAR** | `~/.var/app/com.hypixel.HytaleLauncher/data/Hytale/install/release/package/game/latest/Server/HytaleServer.jar` |
| **Mods folder** | `~/.var/app/com.hypixel.HytaleLauncher/data/Hytale/install/mods/` |
| **Early plugins** | `~/.var/app/com.hypixel.HytaleLauncher/data/Hytale/install/early-plugins/` |
| **JRE** | `~/.var/app/com.hypixel.HytaleLauncher/data/Hytale/install/release/package/jre/` |
| **Assets** | `~/.var/app/com.hypixel.HytaleLauncher/data/Hytale/install/release/package/game/latest/Assets.zip` |

## 📚 Documentation

- **[Performance Optimizations](docs/PERFORMANCE_OPTIMIZATIONS.md)** - Core optimization techniques (PROJECT ROADMAP)
- **[API Reference](docs/HYTALE_API_REFERENCE.md)** - Hytale Server Plugin API documentation
- **[Early Plugins Guide](docs/ADVANCED_EARLY_PLUGINS.md)** - Bootstrap plugins and bytecode transformation

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

# Deploy to early-plugins (for class transformers)
./gradlew deployEarlyPlugin
```

## 🏗️ Architecture

Catalyst operates as an **Early Plugin** (Bootstrap Plugin) that uses bytecode transformation to optimize server performance **before** classes are loaded.

```
┌─────────────────────────────────────────────────┐
│                 Hytale Server                    │
├─────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────┐    │
│  │     TransformingClassLoader             │    │
│  │  ┌───────────────────────────────────┐  │    │
│  │  │  Catalyst ClassTransformers       │  │    │
│  │  │  ├── TickOptimizationTransformer  │  │    │
│  │  │  ├── EntityTrackerTransformer     │  │    │
│  │  │  ├── ChunkCacheTransformer        │  │    │
│  │  │  ├── PacketBatchingTransformer    │  │    │
│  │  │  └── MemoryPoolingTransformer     │  │    │
│  │  └───────────────────────────────────┘  │    │
│  └─────────────────────────────────────────┘    │
│                      ↓                           │
│           Optimized Server Code                  │
└─────────────────────────────────────────────────┘
```

## 🎯 Golden Rules

### ❌ Don't:
- Optimize without profiling first
- Optimize everything (premature optimization)
- Assume O(1) is better than O(n) without context
- Use multiple threads without understanding synchronization
- Cache everything (memory pressure)
- Optimize code that's not a bottleneck

### ✅ Do:
- Profile first
- Optimize the critical path
- Measure before and after
- Test under realistic conditions
- Consider maintainability
- Document optimizations

## 📋 Implementation Checklist

- [ ] Profile server to identify bottlenecks
- [ ] Benchmark before optimization
- [ ] Implement single optimization
- [ ] Benchmark after optimization
- [ ] If no improvement, revert and try different approach
- [ ] Test under realistic load
- [ ] Monitor for regressions
- [ ] Document what works and what doesn't

## 🛠️ Gradle Tasks

### Build Tasks
```bash
./gradlew build              # Build the mod JAR
./gradlew jar                # Build JAR only
./gradlew clean              # Clean build outputs
```

### Deployment Tasks
```bash
./gradlew deployEarlyPlugin  # Deploy to early-plugins directory
./gradlew runServer          # Start Hytale server with mod
```

## 📁 Project Structure

```
Catalyst/
├── docs/                              # Documentation
│   ├── PERFORMANCE_OPTIMIZATIONS.md   # Optimization roadmap
│   ├── HYTALE_API_REFERENCE.md        # API documentation
│   └── ADVANCED_EARLY_PLUGINS.md      # Early plugins guide
├── src/main/java/com/criticalrange/
│   ├── Catalyst.java                  # Main entry point
│   └── transformer/                   # Class transformers
│       ├── TickOptimizationTransformer.java
│       ├── EntityTrackerTransformer.java
│       ├── ChunkCacheTransformer.java
│       └── ...
├── src/main/resources/
│   ├── manifest.json                  # Mod manifest
│   └── META-INF/services/             # Service loader configs
├── build.gradle                       # Build configuration
├── gradle.properties                  # Mod properties
└── README.md                          # This file
```

## 🔒 Security & Best Practices

- Early plugins run with full system access
- Always test on a separate server
- Keep backups of world data
- Profile before optimizing - measure twice, cut once
- Return original bytecode on transformation errors

## 📝 License

This project is available under the **CC0 License** - feel free to use it however you like!

---

**Built with ❤️ for Hytale server performance**

*"Making servers fly at light speed"*
