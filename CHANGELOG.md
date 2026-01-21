# Changelog

All notable changes to Catalyst will be documented in this file.

## [0.2.0] - 2026-01-21

### Added
- **Entity Distance Control**: Configurable entity view distance multiplier (default: 1.0)
  - Adjusts how far entities are visible/processed
  - Lower values improve server performance
- **Chunk Rate Control**: Configurable chunks processed per tick (default: 1)
  - Limits chunk loading speed to prevent server lag spikes
- **Pathfinding Optimization**: Configurable pathfinding timeout reduction (default: 50%)
  - Reduces CPU usage from entity pathfinding calculations
- **Reset to Defaults Button**: One-click reset in settings GUI to restore all defaults
- **Optimization Metrics**: Track performance impact of enabled optimizations

### Changed
- **UI Redesign**: Complete settings UI overhaul following Hytale design patterns
  - Organized into logical sections with color-coded headers
  - Fixed position elements (reset button at bottom)
  - Removed missing texture references that caused red backgrounds
  - Improved layout with proper scrolling sections
- **Slider Values Fixed**: Corrected property type from `.Text` to `.Value` for numeric sliders
- **Improved transformer base class**: Better error handling and logging

### Fixed
- Fixed slider value setting error (now uses correct `.Value` property)
- Fixed red background in UI sections (removed missing texture reference)
- Fixed reset button position (now at fixed bottom position, not scrolling)
- Fixed checkbox value binding in settings UI

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
