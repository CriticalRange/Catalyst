# Catalyst

> Bytecode-level performance optimization for Hytale servers

Catalyst is an early-loading plugin that hooks into Hytale's class loading pipeline and injects optimizations directly into the bytecode. Instead of wrapping APIs or patching the JAR, it modifies classes as they're loaded—so the optimizations apply everywhere, automatically.

## Why this exists

Hytale servers do a lot of work that isn't always necessary. Chests get instantiated during world gen before anyone's near them. Fluids simulate during chunk load even when no player can see the result. Block tick discovery runs eagerly on every chunk preload.

Catalyst defers this work until it's actually needed, and adds some batching/caching on top. The changes happen at the bytecode level, so there's no plugin API overhead and no game patches to reapply when Hytale updates.

## What's actually working

Right now the project is in early stages. Here's what's implemented:

| Transformer | What it does |
|-------------|--------------|
| `LazyBlockEntityTransformer` | Defers chest/furnace/sign creation until first access |
| `LazyBlockTickTransformer` | Postpones tick discovery during chunk preload |
| `LazyFluidTransformer` | Skips fluid sim during chunk load |

Everything's toggleable at runtime via `/catalyst menu`—no restart needed.

## Installation

**For most users:**

1. Download the latest JAR from: [RELEASES PAGE - ADD URL HERE]
2. Drop it in your `early-plugins/` folder (The plugin goes in `early-plugins/`, not `mods/`. It runs before the server starts, so transformers can intercept class loading.):
   - **Windows**: `C:\Program Files\Hytale\install\early-plugins\`
   - **Linux (Flatpak)**: `~/.var/app/com.hypixel.HytaleLauncher/data/Hytale/install/early-plugins/`
   - **macOS**: `~/Library/Application Support/Hytale/install/early-plugins/` (You may want to double-check the macOS path. I used the standard macOS app support location because I don't have a mac)
3. Restart your server

**For developers:**

```bash
./gradlew build
./gradlew deployEarlyPlugin
```

The plugin goes in `early-plugins/`, not `mods/`. It runs before the server starts, so transformers can intercept class loading.

## Finding the Hytale JAR

If you're on Flatpak, the paths are a bit buried:

```
~/.var/app/com.hypixel.HytaleLauncher/data/Hytale/
└── install/
    ├── early-plugins/        # Put Catalyst here
    ├── mods/                 # Regular plugins go here
    └── release/package/game/latest/
        ├── Server/HytaleServer.jar
        └── Assets.zip
```

## Development

The transformer pattern is straightforward—extend `BaseTransformer`, implement `transform()`, and register via `META-INF/services`. If something goes wrong, return the original bytecode and the server continues normally.

All config fields are `volatile` and readable at runtime, so you can flip optimizations on/off without restarting.

```bash
./gradlew build              # Build JAR
./gradlew deployEarlyPlugin  # Deploy to early-plugins/
./gradlew runServer          # Launch server with mod loaded
```

## Documentation

- [Performance Optimizations](docs/PERFORMANCE_OPTIMIZATIONS.md) — Roadmap and planned work
- [API Reference](docs/HYTALE_API_REFERENCE.md) — Hytale Server Plugin API docs
- [Early Plugins Guide](docs/ADVANCED_EARLY_PLUGINS.md) — How bytecode transformation works

## Hyxin Compatibility

Catalyst includes optional support for [Hyxin](https://github.com/Darkhax/Hyxin), the Mixin framework for Hytale. When Hyxin is installed alongside Catalyst, the same optimizations are available through Mixin instead of raw ASM transformation.

**Current approach:** Catalyst ships with both ASM transformers and Mixin equivalents. The ASM transformers are the primary method and work standalone. The Mixin versions are there for compatibility and will activate automatically when Hyxin is present.

**Why both?** Hyxin is still in early access and has some known issues being worked on. Once Hyxin reaches a stable release, we plan to transition fully to Mixin-based optimizations. Until then, the ASM transformers ensure Catalyst works reliably regardless of whether Hyxin is installed.

If you're a Hyxin user and run into issues, you can remove Hyxin and Catalyst will fall back to its standalone ASM transformers automatically.

## Status

This is experimental. Early plugins run with full system access and can crash your server if something goes wrong. Test on a non-production world first, keep backups, and report issues.

## License

MIT — do whatever you want with this code, just keep the copyright notice.
