# Changelog

All notable changes to Catalyst will be documented in this file.

## [0.2.1] - 2026-01-22

### Removed
- **Lazy Loading Optimizations**: Completely removed lazy loading features (Lazy Block Entities, Lazy Block Tick, Lazy Fluid Processing) as they only performed throttling without actual optimization benefits
  - Removed `LAZY_BLOCK_ENTITIES_ENABLED`, `LAZY_BLOCK_TICK_ENABLED`, `LAZY_FLUID_ENABLED` config fields
  - Removed `BlockModuleMixin`, `BlockTickPluginMixin`, `FluidPluginMixin` mixin classes
  - Removed lazy loading UI toggles and event handlers
  - Removed lazy loading from metrics and menu displays

### Changed
- **Settings UI Improvements**:
  - Moved Reset to Defaults button outside of tab panels to stay fixed at bottom
  - Simplified slider options by removing redundant checkboxes - sliders are now standalone
  - Fixed tab button highlighting - selected tab now properly changes background when switching tabs
  - Tab buttons now use direct `Background` property for dynamic styling
- **Menu & Metrics**: Updated to show Runtime Optimizations instead of removed Lazy Loading options

### Added
- **New Transformers**: Added multiple bytecode transformers for various optimizations:
  - Animation and Particle toggle transformers
  - Biome/Tint interpolation transformers
  - Chunk cache, pool size, and parallel generation transformers
  - Lighting optimizations (batch, distance, propagation, queue, flat cache, packed ops)
  - Height search and opacity lookup cache transformers
- **Visual Effects Toggle**: New utility class for particle and animation control
- **Config Persistence**: Added `CatalystEarlyInit` for config file loading/saving

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
