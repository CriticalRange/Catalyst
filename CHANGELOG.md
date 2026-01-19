# Changelog

All notable changes to Catalyst will be documented in this file.

## [0.1.1] - 2026-01-19

### Added
- **Hyxin Compatibility**: Full support for the Hyxin Mixin framework
  - Added Mixin classes (`BlockModuleMixin`, `BlockTickPluginMixin`, `FluidPluginMixin`)
  - Added `catalyst.mixins.json` configuration
  - Works seamlessly alongside ASM transformers
- **Dual-mode operation**: Catalyst now works both standalone (ASM) and with Hyxin (Mixin)

### Changed
- **Java 21 target**: Downgraded from Java 25 to Java 21 for broader compatibility
- **ASM relocation**: Bundled ASM classes relocated to `com.criticalrange.asm` to prevent classloader conflicts with Hyxin
- **Build system**: Migrated to Shadow plugin for proper dependency shading

### Fixed
- Fixed typo in settings UI ("CATALYT" -> "CATALYST")
- Resolved ASM version conflicts when running alongside Hyxin

## [0.1.0] - Initial Release

### Added
- Core bytecode transformation system using ASM
- **Lazy Block Entity Loading**: Defers `BlockModule.onChunkPreLoadProcessEnsureBlockEntity()` calls
- **Lazy Block Tick Discovery**: Defers `BlockTickPlugin.discoverTickingBlocks()` calls  
- **Lazy Fluid Processing**: Defers `FluidPlugin.onChunkPreProcess()` calls
- In-game settings GUI for toggling optimizations
- `/catalyst` command for runtime configuration
- Early plugin architecture for Hytale server integration
