package com.criticalrange.util;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

/**
 * Settings GUI for Catalyst optimizations.
 */
public class CatalystSettingsGui extends InteractiveCustomUIPage<CatalystSettingsGui.GuiData> {

    public CatalystSettingsGui(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime) {
        super(playerRef, lifetime, GuiData.CODEC);
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder,
                      @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        // Load the base UI file
        uiCommandBuilder.append("Pages/CriticalRange_Catalyst_Settings.ui");

        // Set slider values for runtime optimizations
        uiCommandBuilder.set("#EntityDistanceSlider.Value", com.criticalrange.CatalystConfig.ENTITY_VIEW_MULTIPLIER);
        uiCommandBuilder.set("#ChunkRateSlider.Value", com.criticalrange.CatalystConfig.CHUNKS_PER_TICK);

        // Set slider values for pathfinding
        uiCommandBuilder.set("#MaxPathLengthSlider.Value", com.criticalrange.CatalystConfig.MAX_PATH_LENGTH);
        uiCommandBuilder.set("#OpenNodesSlider.Value", com.criticalrange.CatalystConfig.OPEN_NODES_LIMIT);
        uiCommandBuilder.set("#TotalNodesSlider.Value", com.criticalrange.CatalystConfig.TOTAL_NODES_LIMIT);

        // Set checkbox values for lighting
        uiCommandBuilder.set("#SkipEmptyCheckBox.Value", com.criticalrange.CatalystConfig.LIGHT_SKIP_EMPTY_ENABLED);

        // Set slider values for chunk generation
        uiCommandBuilder.set("#ChunkThreadPrioritySlider.Value", com.criticalrange.CatalystConfig.CHUNK_THREAD_PRIORITY);

        // Set checkbox values for visual effects
        uiCommandBuilder.set("#ParticlesToggle #CheckBox.Value", com.criticalrange.CatalystConfig.PARTICLES_ENABLED);
        uiCommandBuilder.set("#AnimationsToggle #CheckBox.Value", com.criticalrange.CatalystConfig.ANIMATIONS_ENABLED);

        // Set checkbox values for caching optimizations
        uiCommandBuilder.set("#BlockSectionCacheCheckBox.Value", com.criticalrange.CatalystConfig.BLOCK_SECTION_CACHE_ENABLED);
        uiCommandBuilder.set("#BlockTypeCacheCheckBox.Value", com.criticalrange.CatalystConfig.BLOCK_TYPE_CACHE_ENABLED);
        uiCommandBuilder.set("#LocalChunkCacheCheckBox.Value", com.criticalrange.CatalystConfig.LOCAL_CHUNK_CACHE_ENABLED);

        // Add event bindings for sliders
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#EntityDistanceSlider",
            EventData.of("@SetEntityDistance", "#EntityDistanceSlider.Value"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#ChunkRateSlider",
            EventData.of("@SetChunkRate", "#ChunkRateSlider.Value"), false);

        // Add event bindings for pathfinding
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#MaxPathLengthSlider",
            EventData.of("@SetMaxPathLength", "#MaxPathLengthSlider.Value"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#OpenNodesSlider",
            EventData.of("@SetOpenNodes", "#OpenNodesSlider.Value"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#TotalNodesSlider",
            EventData.of("@SetTotalNodes", "#TotalNodesSlider.Value"), false);

        // Add event bindings for lighting
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#SkipEmptyCheckBox",
            EventData.of("ToggleSkipEmptySections", "CAT"), false);

        // Add event bindings for chunk generation
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#ChunkThreadPrioritySlider",
            EventData.of("@SetChunkThreadPriority", "#ChunkThreadPrioritySlider.Value"), false);

        // Add event bindings for visual effects
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#ParticlesToggle #CheckBox",
            EventData.of("ToggleParticles", "CAT"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#AnimationsToggle #CheckBox",
            EventData.of("ToggleAnimations", "CAT"), false);

        // Add event bindings for caching optimizations
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#BlockSectionCacheCheckBox",
            EventData.of("ToggleBlockSectionCache", "CAT"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#BlockTypeCacheCheckBox",
            EventData.of("ToggleBlockTypeCache", "CAT"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#LocalChunkCacheCheckBox",
            EventData.of("ToggleLocalChunkCache", "CAT"), false);

        // Add event binding for reset button
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ResetButton",
            EventData.of("ResetToDefaults", "CAT"), false);

        // Add event bindings for tab buttons
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TabGeneral",
            EventData.of("SwitchTabGeneral", "CAT"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TabLighting",
            EventData.of("SwitchTabLighting", "CAT"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TabPathfinding",
            EventData.of("SwitchTabPathfinding", "CAT"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TabVisual",
            EventData.of("SwitchTabVisual", "CAT"), false);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull GuiData data) {
        super.handleDataEvent(ref, store, data);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        boolean needUpdate = false;
        UICommandBuilder commandBuilder = new UICommandBuilder();
        UIEventBuilder eventBuilder = new UIEventBuilder();

        // Slider value changes
        if (data.entityDistanceValue != null) {
            int newValue = Math.max(8, Math.min(64, data.entityDistanceValue));  // Clamp to valid range
            setEntityViewMultiplier(newValue);
            commandBuilder.set("#EntityDistanceSlider.Value", newValue);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Entity View Multiplier: " + newValue));
            needUpdate = true;
        }

        if (data.chunkRateValue != null) {
            int newValue = Math.max(1, Math.min(16, data.chunkRateValue));  // Clamp to valid range
            setChunksPerTick(newValue);
            commandBuilder.set("#ChunkRateSlider.Value", newValue);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Chunks Per Tick: " + newValue));
            needUpdate = true;
        }

        // Pathfinding slider values
        if (data.maxPathLengthValue != null) {
            int newValue = Math.max(50, Math.min(500, data.maxPathLengthValue));  // Clamp to valid range
            setMaxPathLength(newValue);
            commandBuilder.set("#MaxPathLengthSlider.Value", newValue);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Max Path Length: " + newValue));
            needUpdate = true;
        }

        if (data.openNodesValue != null) {
            int newValue = Math.max(20, Math.min(200, data.openNodesValue));  // Clamp to valid range
            setOpenNodesLimit(newValue);
            commandBuilder.set("#OpenNodesSlider.Value", newValue);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Open Nodes Limit: " + newValue));
            needUpdate = true;
        }

        if (data.totalNodesValue != null) {
            int newValue = Math.max(100, Math.min(1000, data.totalNodesValue));  // Clamp to valid range
            setTotalNodesLimit(newValue);
            commandBuilder.set("#TotalNodesSlider.Value", newValue);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Total Nodes Limit: " + newValue));
            needUpdate = true;
        }

        // Lighting toggle
        if (data.toggleSkipEmptySections != null) {
            boolean newState = toggleSkipEmptySections();
            commandBuilder.set("#SkipEmptyCheckBox.Value", newState);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Skip Empty Sections: " + (newState ? "ENABLED" : "DISABLED")));
            needUpdate = true;
        }

        // Chunk thread priority slider
        if (data.chunkThreadPriorityValue != null) {
            int newValue = Math.max(1, Math.min(10, data.chunkThreadPriorityValue));
            setChunkThreadPriority(newValue);
            commandBuilder.set("#ChunkThreadPrioritySlider.Value", newValue);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Chunk Thread Priority: " + newValue));
            needUpdate = true;
        }

        // Visual effects toggles
        if (data.toggleParticles != null) {
            boolean newState = toggleParticles();
            commandBuilder.set("#ParticlesToggle #CheckBox.Value", newState);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Particles: " + (newState ? "ENABLED" : "DISABLED")));
            needUpdate = true;
        }

        if (data.toggleAnimations != null) {
            boolean newState = toggleAnimations();
            commandBuilder.set("#AnimationsToggle #CheckBox.Value", newState);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "NPC Animations: " + (newState ? "ENABLED" : "DISABLED")));
            needUpdate = true;
        }

        // Caching optimization toggles
        if (data.toggleBlockSectionCache != null) {
            boolean newState = toggleBlockSectionCache();
            commandBuilder.set("#BlockSectionCacheCheckBox.Value", newState);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Block Section Cache: " + (newState ? "ENABLED" : "DISABLED")));
            needUpdate = true;
        }

        if (data.toggleBlockTypeCache != null) {
            boolean newState = toggleBlockTypeCache();
            commandBuilder.set("#BlockTypeCacheCheckBox.Value", newState);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Block Type Cache: " + (newState ? "ENABLED" : "DISABLED")));
            needUpdate = true;
        }

        if (data.toggleLocalChunkCache != null) {
            boolean newState = toggleLocalChunkCache();
            commandBuilder.set("#LocalChunkCacheCheckBox.Value", newState);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Local Chunk Cache: " + (newState ? "ENABLED" : "DISABLED")));
            needUpdate = true;
        }

        // Reset to defaults button
        if (data.resetToDefaults != null) {
            resetToDefaults(commandBuilder);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "All settings reset to defaults!"));
            needUpdate = true;
        }

        // Tab switching
        if (data.switchTabGeneral != null) {
            switchToTab(commandBuilder, "General");
            needUpdate = true;
        }
        if (data.switchTabLighting != null) {
            switchToTab(commandBuilder, "Lighting");
            needUpdate = true;
        }
        if (data.switchTabPathfinding != null) {
            switchToTab(commandBuilder, "Pathfinding");
            needUpdate = true;
        }
        if (data.switchTabVisual != null) {
            switchToTab(commandBuilder, "Visual");
            needUpdate = true;
        }

        if (needUpdate) {
            this.sendUpdate(commandBuilder, eventBuilder, false);
            // Auto-save config to file
            com.criticalrange.CatalystEarlyInit.saveConfig();
        }
    }

    // ===== Tab Switching =====

    private void switchToTab(UICommandBuilder commandBuilder, String tabName) {
        // Hide all panels
        commandBuilder.set("#PanelGeneral.Visible", false);
        commandBuilder.set("#PanelLighting.Visible", false);
        commandBuilder.set("#PanelPathfinding.Visible", false);
        commandBuilder.set("#PanelVisual.Visible", false);

        // Reset all indicators
        commandBuilder.set("#IndicatorGeneral.Background", "#12151c");
        commandBuilder.set("#IndicatorLighting.Background", "#12151c");
        commandBuilder.set("#IndicatorPathfinding.Background", "#12151c");
        commandBuilder.set("#IndicatorVisual.Background", "#12151c");

        // Reset all tab buttons to unselected style
        commandBuilder.set("#TabGeneral.Background", "#1a1d26");
        commandBuilder.set("#TabLighting.Background", "#1a1d26");
        commandBuilder.set("#TabPathfinding.Background", "#1a1d26");
        commandBuilder.set("#TabVisual.Background", "#1a1d26");

        // Show selected panel, indicator, and tab button
        switch (tabName) {
            case "General":
                commandBuilder.set("#PanelGeneral.Visible", true);
                commandBuilder.set("#IndicatorGeneral.Background", "#55aaff");
                commandBuilder.set("#TabGeneral.Background", "#2d3444");
                break;
            case "Lighting":
                commandBuilder.set("#PanelLighting.Visible", true);
                commandBuilder.set("#IndicatorLighting.Background", "#55aaff");
                commandBuilder.set("#TabLighting.Background", "#2d3444");
                break;
            case "Pathfinding":
                commandBuilder.set("#PanelPathfinding.Visible", true);
                commandBuilder.set("#IndicatorPathfinding.Background", "#55aaff");
                commandBuilder.set("#TabPathfinding.Background", "#2d3444");
                break;
            case "Visual":
                commandBuilder.set("#PanelVisual.Visible", true);
                commandBuilder.set("#IndicatorVisual.Background", "#55aaff");
                commandBuilder.set("#TabVisual.Background", "#2d3444");
                break;
        }
    }

    // ===== Runtime Optimization Toggles =====

    private void setEntityViewMultiplier(int value) {
        try {
            Class<?> universeClass = Class.forName("com.hypixel.hytale.server.core.universe.Universe");
            java.lang.reflect.Field field = universeClass.getField("$catalystEntityViewMultiplier");
            field.setInt(null, value);
            com.criticalrange.CatalystConfig.ENTITY_VIEW_MULTIPLIER = value;
        } catch (Exception e) {
            System.err.println("[Catalyst] Failed to set entity view multiplier: " + e.getMessage());
            com.criticalrange.CatalystConfig.ENTITY_VIEW_MULTIPLIER = value;
        }
    }

    private void setChunksPerTick(int value) {
        try {
            Class<?> chunkTrackerClass = Class.forName("com.hypixel.hytale.server.core.modules.entity.player.ChunkTracker");
            java.lang.reflect.Field field = chunkTrackerClass.getField("$catalystChunksPerTick");
            field.setInt(null, value);
            com.criticalrange.CatalystConfig.CHUNKS_PER_TICK = value;
        } catch (Exception e) {
            System.err.println("[Catalyst] Failed to set chunks per tick: " + e.getMessage());
            com.criticalrange.CatalystConfig.CHUNKS_PER_TICK = value;
        }
    }

    private boolean toggleEntityDistance() {
        try {
            Class<?> universeClass = Class.forName("com.hypixel.hytale.server.core.universe.Universe");
            java.lang.reflect.Field field = universeClass.getField("$catalystEntityDistEnabled");
            boolean currentState = field.getBoolean(null);
            boolean newState = !currentState;
            field.setBoolean(null, newState);
            com.criticalrange.CatalystConfig.ENTITY_DISTANCE_ENABLED = newState;
            return newState;
        } catch (Exception e) {
            System.err.println("[Catalyst] Failed to toggle entity distance: " + e.getMessage());
            com.criticalrange.CatalystConfig.ENTITY_DISTANCE_ENABLED = !com.criticalrange.CatalystConfig.ENTITY_DISTANCE_ENABLED;
            return com.criticalrange.CatalystConfig.ENTITY_DISTANCE_ENABLED;
        }
    }

    private boolean toggleChunkRate() {
        try {
            Class<?> chunkTrackerClass = Class.forName("com.hypixel.hytale.server.core.modules.entity.player.ChunkTracker");
            java.lang.reflect.Field field = chunkTrackerClass.getField("$catalystChunkRateEnabled");
            boolean currentState = field.getBoolean(null);
            boolean newState = !currentState;
            field.setBoolean(null, newState);
            com.criticalrange.CatalystConfig.CHUNK_RATE_ENABLED = newState;
            return newState;
        } catch (Exception e) {
            System.err.println("[Catalyst] Failed to toggle chunk rate: " + e.getMessage());
            com.criticalrange.CatalystConfig.CHUNK_RATE_ENABLED = !com.criticalrange.CatalystConfig.CHUNK_RATE_ENABLED;
            return com.criticalrange.CatalystConfig.CHUNK_RATE_ENABLED;
        }
    }

    // ===== Pathfinding Toggles and Setters =====

    private boolean togglePathfinding() {
        try {
            Class<?> astarClass = Class.forName("com.hypixel.hytale.server.npc.navigation.AStarBase");
            java.lang.reflect.Field field = astarClass.getField("$catalystPathfindingEnabled");
            boolean currentState = field.getBoolean(null);
            boolean newState = !currentState;
            field.setBoolean(null, newState);
            com.criticalrange.CatalystConfig.PATHFINDING_ENABLED = newState;
            return newState;
        } catch (Exception e) {
            System.err.println("[Catalyst] Failed to toggle pathfinding: " + e.getMessage());
            com.criticalrange.CatalystConfig.PATHFINDING_ENABLED = !com.criticalrange.CatalystConfig.PATHFINDING_ENABLED;
            return com.criticalrange.CatalystConfig.PATHFINDING_ENABLED;
        }
    }

    private void setMaxPathLength(int value) {
        try {
            Class<?> astarClass = Class.forName("com.hypixel.hytale.server.npc.navigation.AStarBase");
            java.lang.reflect.Field field = astarClass.getField("$catalystMaxPathLength");
            field.setInt(null, value);
            com.criticalrange.CatalystConfig.MAX_PATH_LENGTH = value;
        } catch (Exception e) {
            System.err.println("[Catalyst] Failed to set max path length: " + e.getMessage());
            com.criticalrange.CatalystConfig.MAX_PATH_LENGTH = value;
        }
    }

    private void setOpenNodesLimit(int value) {
        try {
            Class<?> astarClass = Class.forName("com.hypixel.hytale.server.npc.navigation.AStarBase");
            java.lang.reflect.Field field = astarClass.getField("$catalystOpenNodesLimit");
            field.setInt(null, value);
            com.criticalrange.CatalystConfig.OPEN_NODES_LIMIT = value;
        } catch (Exception e) {
            System.err.println("[Catalyst] Failed to set open nodes limit: " + e.getMessage());
            com.criticalrange.CatalystConfig.OPEN_NODES_LIMIT = value;
        }
    }

    private void setTotalNodesLimit(int value) {
        try {
            Class<?> astarClass = Class.forName("com.hypixel.hytale.server.npc.navigation.AStarBase");
            java.lang.reflect.Field field = astarClass.getField("$catalystTotalNodesLimit");
            field.setInt(null, value);
            com.criticalrange.CatalystConfig.TOTAL_NODES_LIMIT = value;
        } catch (Exception e) {
            System.err.println("[Catalyst] Failed to set total nodes limit: " + e.getMessage());
            com.criticalrange.CatalystConfig.TOTAL_NODES_LIMIT = value;
        }
    }



    // ===== Chunk Generation Toggles and Setters =====

    private boolean toggleChunkThreadPriority() {
        com.criticalrange.CatalystConfig.CHUNK_THREAD_PRIORITY_ENABLED = !com.criticalrange.CatalystConfig.CHUNK_THREAD_PRIORITY_ENABLED;
        return com.criticalrange.CatalystConfig.CHUNK_THREAD_PRIORITY_ENABLED;
    }

    private void setChunkThreadPriority(int value) {
        com.criticalrange.CatalystConfig.CHUNK_THREAD_PRIORITY = value;
    }

    // ===== Lighting Toggles =====

    private boolean toggleSkipEmptySections() {
        boolean newState = !com.criticalrange.CatalystConfig.LIGHT_SKIP_EMPTY_ENABLED;
        com.criticalrange.CatalystConfig.LIGHT_SKIP_EMPTY_ENABLED = newState;
        // Sync to injected field
        try {
            Class<?> floodLightClass = Class.forName("com.hypixel.hytale.server.core.universe.world.lighting.FloodLightCalculation");
            java.lang.reflect.Field field = floodLightClass.getField("$catalystSkipEmptyEnabled");
            field.setBoolean(null, newState);
        } catch (Exception e) {
            // Field may not exist if transformer didn't run
        }
        return newState;
    }

    // ===== Visual Effects Toggles =====

    private boolean toggleParticles() {
        boolean newState = !VisualEffectsToggle.particlesEnabled;
        VisualEffectsToggle.particlesEnabled = newState;
        com.criticalrange.CatalystConfig.PARTICLES_ENABLED = newState;
        // Sync to injected field in Hytale class
        try {
            Class<?> particleUtilClass = Class.forName("com.hypixel.hytale.server.core.universe.world.ParticleUtil");
            java.lang.reflect.Field field = particleUtilClass.getField("$catalystParticlesEnabled");
            field.setBoolean(null, newState);
        } catch (Exception e) {
            // Field may not exist if transformer didn't run
        }
        return newState;
    }

    private boolean toggleAnimations() {
        boolean newState = !VisualEffectsToggle.animationsEnabled;
        VisualEffectsToggle.animationsEnabled = newState;
        com.criticalrange.CatalystConfig.ANIMATIONS_ENABLED = newState;
        // Sync to injected field in Hytale class
        try {
            Class<?> animationUtilsClass = Class.forName("com.hypixel.hytale.server.core.entity.AnimationUtils");
            java.lang.reflect.Field field = animationUtilsClass.getField("$catalystAnimationsEnabled");
            field.setBoolean(null, newState);
        } catch (Exception e) {
            // Field may not exist if transformer didn't run
        }
        return newState;
    }

    // ===== Caching Optimization Toggles =====

    private boolean toggleBlockSectionCache() {
        boolean newState = !com.criticalrange.CatalystConfig.BLOCK_SECTION_CACHE_ENABLED;
        com.criticalrange.CatalystConfig.BLOCK_SECTION_CACHE_ENABLED = newState;
        // Sync to injected field
        try {
            Class<?> blockChunkClass = Class.forName("com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk");
            java.lang.reflect.Field field = blockChunkClass.getField("$catalystSectionCacheEnabled");
            field.setBoolean(null, newState);
        } catch (Exception e) {
            // Field may not exist if transformer didn't run
        }
        return newState;
    }

    private boolean toggleBlockTypeCache() {
        boolean newState = !com.criticalrange.CatalystConfig.BLOCK_TYPE_CACHE_ENABLED;
        com.criticalrange.CatalystConfig.BLOCK_TYPE_CACHE_ENABLED = newState;
        // Sync to injected field
        try {
            Class<?> assetMapClass = Class.forName("com.hypixel.hytale.assetstore.map.BlockTypeAssetMap");
            java.lang.reflect.Field field = assetMapClass.getField("$catalystBlockTypeCacheEnabled");
            field.setBoolean(null, newState);
        } catch (Exception e) {
            // Field may not exist if transformer didn't run
        }
        return newState;
    }

    private boolean toggleLocalChunkCache() {
        boolean newState = !com.criticalrange.CatalystConfig.LOCAL_CHUNK_CACHE_ENABLED;
        com.criticalrange.CatalystConfig.LOCAL_CHUNK_CACHE_ENABLED = newState;
        // Sync to injected field
        try {
            Class<?> accessorClass = Class.forName("com.hypixel.hytale.server.core.universe.world.accessor.LocalCachedChunkAccessor");
            java.lang.reflect.Field field = accessorClass.getField("$catalystLocalChunkCacheEnabled");
            field.setBoolean(null, newState);
        } catch (Exception e) {
            // Field may not exist if transformer didn't run
        }
        return newState;
    }

    // ===== Reset to Defaults =====

    /**
     * Resets all Catalyst settings to their default values.
     */
    private void resetToDefaults(UICommandBuilder commandBuilder) {
        // Runtime optimization defaults (all disabled, values at vanilla)
        if (com.criticalrange.CatalystConfig.ENTITY_DISTANCE_ENABLED) {
            toggleEntityDistance();  // Toggle to false
        }
        if (com.criticalrange.CatalystConfig.ENTITY_VIEW_MULTIPLIER != com.criticalrange.CatalystConfig.DEFAULT_BLOCKS_PER_CHUNK) {
            setEntityViewMultiplier(com.criticalrange.CatalystConfig.DEFAULT_BLOCKS_PER_CHUNK);
        }

        if (com.criticalrange.CatalystConfig.CHUNK_RATE_ENABLED) {
            toggleChunkRate();  // Toggle to false
        }
        if (com.criticalrange.CatalystConfig.CHUNKS_PER_TICK != com.criticalrange.CatalystConfig.DEFAULT_CHUNKS_PER_TICK) {
            setChunksPerTick(com.criticalrange.CatalystConfig.DEFAULT_CHUNKS_PER_TICK);
        }

        // Pathfinding defaults (disabled, vanilla limits)
        if (com.criticalrange.CatalystConfig.PATHFINDING_ENABLED) {
            togglePathfinding();  // Toggle to false
        }
        if (com.criticalrange.CatalystConfig.MAX_PATH_LENGTH != com.criticalrange.CatalystConfig.DEFAULT_MAX_PATH_LENGTH) {
            setMaxPathLength(com.criticalrange.CatalystConfig.DEFAULT_MAX_PATH_LENGTH);
        }
        if (com.criticalrange.CatalystConfig.OPEN_NODES_LIMIT != com.criticalrange.CatalystConfig.DEFAULT_OPEN_NODES_LIMIT) {
            setOpenNodesLimit(com.criticalrange.CatalystConfig.DEFAULT_OPEN_NODES_LIMIT);
        }
        if (com.criticalrange.CatalystConfig.TOTAL_NODES_LIMIT != com.criticalrange.CatalystConfig.DEFAULT_TOTAL_NODES_LIMIT) {
            setTotalNodesLimit(com.criticalrange.CatalystConfig.DEFAULT_TOTAL_NODES_LIMIT);
        }

        // Lighting defaults
        com.criticalrange.CatalystConfig.LIGHT_SKIP_EMPTY_ENABLED = false;
        // Sync to injected field
        try {
            Class<?> floodLightClass = Class.forName("com.hypixel.hytale.server.core.universe.world.lighting.FloodLightCalculation");
            java.lang.reflect.Field field = floodLightClass.getField("$catalystSkipEmptyEnabled");
            field.setBoolean(null, false);
        } catch (Exception e) {
            // Field may not exist if transformer didn't run
        }

        // Chunk generation defaults
        com.criticalrange.CatalystConfig.CHUNK_POOL_SIZE_ENABLED = false;
        com.criticalrange.CatalystConfig.CHUNK_CACHE_SIZE_ENABLED = false;
        com.criticalrange.CatalystConfig.CHUNK_THREAD_PRIORITY_ENABLED = false;
        com.criticalrange.CatalystConfig.CHUNK_THREAD_PRIORITY = 5;

        // Visual effects defaults (enabled by default)
        VisualEffectsToggle.particlesEnabled = true;
        VisualEffectsToggle.animationsEnabled = true;
        com.criticalrange.CatalystConfig.PARTICLES_ENABLED = true;
        com.criticalrange.CatalystConfig.ANIMATIONS_ENABLED = true;

        // Caching defaults (disabled by default)
        com.criticalrange.CatalystConfig.BLOCK_SECTION_CACHE_ENABLED = false;
        com.criticalrange.CatalystConfig.BLOCK_TYPE_CACHE_ENABLED = false;
        com.criticalrange.CatalystConfig.LOCAL_CHUNK_CACHE_ENABLED = false;
        // Sync to injected fields
        try {
            Class<?> blockChunkClass = Class.forName("com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk");
            blockChunkClass.getField("$catalystSectionCacheEnabled").setBoolean(null, false);
        } catch (Exception e) { /* ignore */ }
        try {
            Class<?> assetMapClass = Class.forName("com.hypixel.hytale.assetstore.map.BlockTypeAssetMap");
            assetMapClass.getField("$catalystBlockTypeCacheEnabled").setBoolean(null, false);
        } catch (Exception e) { /* ignore */ }
        try {
            Class<?> accessorClass = Class.forName("com.hypixel.hytale.server.core.universe.world.accessor.LocalCachedChunkAccessor");
            accessorClass.getField("$catalystLocalChunkCacheEnabled").setBoolean(null, false);
        } catch (Exception e) { /* ignore */ }

        // Update all UI elements to reflect default values
        commandBuilder.set("#EntityDistanceSlider.Value", com.criticalrange.CatalystConfig.DEFAULT_BLOCKS_PER_CHUNK);
        commandBuilder.set("#ChunkRateSlider.Value", com.criticalrange.CatalystConfig.DEFAULT_CHUNKS_PER_TICK);
        commandBuilder.set("#MaxPathLengthSlider.Value", com.criticalrange.CatalystConfig.DEFAULT_MAX_PATH_LENGTH);
        commandBuilder.set("#OpenNodesSlider.Value", com.criticalrange.CatalystConfig.DEFAULT_OPEN_NODES_LIMIT);
        commandBuilder.set("#TotalNodesSlider.Value", com.criticalrange.CatalystConfig.DEFAULT_TOTAL_NODES_LIMIT);
        commandBuilder.set("#SkipEmptyCheckBox.Value", false);
        commandBuilder.set("#ChunkThreadPrioritySlider.Value", Thread.NORM_PRIORITY);
        commandBuilder.set("#ParticlesToggle #CheckBox.Value", true);
        commandBuilder.set("#AnimationsToggle #CheckBox.Value", true);
        commandBuilder.set("#BlockSectionCacheCheckBox.Value", false);
        commandBuilder.set("#BlockTypeCacheCheckBox.Value", false);
        commandBuilder.set("#LocalChunkCacheCheckBox.Value", false);
    }

    /**
     * GUI data for event handling.
     */
    public static class GuiData {
        static final String KEY_SET_ENTITY_DISTANCE = "@SetEntityDistance";
        static final String KEY_SET_CHUNK_RATE = "@SetChunkRate";
        static final String KEY_SET_MAX_PATH_LENGTH = "@SetMaxPathLength";
        static final String KEY_SET_OPEN_NODES = "@SetOpenNodes";
        static final String KEY_SET_TOTAL_NODES = "@SetTotalNodes";
        static final String KEY_TOGGLE_SKIP_EMPTY_SECTIONS = "ToggleSkipEmptySections";
        static final String KEY_SET_CHUNK_THREAD_PRIORITY = "@SetChunkThreadPriority";
        static final String KEY_TOGGLE_PARTICLES = "ToggleParticles";
        static final String KEY_TOGGLE_ANIMATIONS = "ToggleAnimations";
        static final String KEY_RESET_TO_DEFAULTS = "ResetToDefaults";
        static final String KEY_SWITCH_TAB_GENERAL = "SwitchTabGeneral";
        static final String KEY_SWITCH_TAB_LIGHTING = "SwitchTabLighting";
        static final String KEY_SWITCH_TAB_PATHFINDING = "SwitchTabPathfinding";
        static final String KEY_SWITCH_TAB_VISUAL = "SwitchTabVisual";
        static final String KEY_TOGGLE_BLOCK_SECTION_CACHE = "ToggleBlockSectionCache";
        static final String KEY_TOGGLE_BLOCK_TYPE_CACHE = "ToggleBlockTypeCache";
        static final String KEY_TOGGLE_LOCAL_CHUNK_CACHE = "ToggleLocalChunkCache";

        public static final BuilderCodec<GuiData> CODEC = BuilderCodec.builder(GuiData.class, GuiData::new)
                .addField(new KeyedCodec<>(KEY_SET_ENTITY_DISTANCE, Codec.INTEGER),
                    (guiData, i) -> guiData.entityDistanceValue = i, guiData -> guiData.entityDistanceValue)
                .addField(new KeyedCodec<>(KEY_SET_CHUNK_RATE, Codec.INTEGER),
                    (guiData, i) -> guiData.chunkRateValue = i, guiData -> guiData.chunkRateValue)
                .addField(new KeyedCodec<>(KEY_SET_MAX_PATH_LENGTH, Codec.INTEGER),
                    (guiData, i) -> guiData.maxPathLengthValue = i, guiData -> guiData.maxPathLengthValue)
                .addField(new KeyedCodec<>(KEY_SET_OPEN_NODES, Codec.INTEGER),
                    (guiData, i) -> guiData.openNodesValue = i, guiData -> guiData.openNodesValue)
                .addField(new KeyedCodec<>(KEY_SET_TOTAL_NODES, Codec.INTEGER),
                    (guiData, i) -> guiData.totalNodesValue = i, guiData -> guiData.totalNodesValue)
                .addField(new KeyedCodec<>(KEY_TOGGLE_SKIP_EMPTY_SECTIONS, Codec.STRING),
                    (guiData, s) -> guiData.toggleSkipEmptySections = s, guiData -> guiData.toggleSkipEmptySections)
                .addField(new KeyedCodec<>(KEY_SET_CHUNK_THREAD_PRIORITY, Codec.INTEGER),
                    (guiData, i) -> guiData.chunkThreadPriorityValue = i, guiData -> guiData.chunkThreadPriorityValue)
                .addField(new KeyedCodec<>(KEY_TOGGLE_PARTICLES, Codec.STRING),
                    (guiData, s) -> guiData.toggleParticles = s, guiData -> guiData.toggleParticles)
                .addField(new KeyedCodec<>(KEY_TOGGLE_ANIMATIONS, Codec.STRING),
                    (guiData, s) -> guiData.toggleAnimations = s, guiData -> guiData.toggleAnimations)
                .addField(new KeyedCodec<>(KEY_RESET_TO_DEFAULTS, Codec.STRING),
                    (guiData, s) -> guiData.resetToDefaults = s, guiData -> guiData.resetToDefaults)
                .addField(new KeyedCodec<>(KEY_SWITCH_TAB_GENERAL, Codec.STRING),
                    (guiData, s) -> guiData.switchTabGeneral = s, guiData -> guiData.switchTabGeneral)
                .addField(new KeyedCodec<>(KEY_SWITCH_TAB_LIGHTING, Codec.STRING),
                    (guiData, s) -> guiData.switchTabLighting = s, guiData -> guiData.switchTabLighting)
                .addField(new KeyedCodec<>(KEY_SWITCH_TAB_PATHFINDING, Codec.STRING),
                    (guiData, s) -> guiData.switchTabPathfinding = s, guiData -> guiData.switchTabPathfinding)
                .addField(new KeyedCodec<>(KEY_SWITCH_TAB_VISUAL, Codec.STRING),
                    (guiData, s) -> guiData.switchTabVisual = s, guiData -> guiData.switchTabVisual)
                .addField(new KeyedCodec<>(KEY_TOGGLE_BLOCK_SECTION_CACHE, Codec.STRING),
                    (guiData, s) -> guiData.toggleBlockSectionCache = s, guiData -> guiData.toggleBlockSectionCache)
                .addField(new KeyedCodec<>(KEY_TOGGLE_BLOCK_TYPE_CACHE, Codec.STRING),
                    (guiData, s) -> guiData.toggleBlockTypeCache = s, guiData -> guiData.toggleBlockTypeCache)
                .addField(new KeyedCodec<>(KEY_TOGGLE_LOCAL_CHUNK_CACHE, Codec.STRING),
                    (guiData, s) -> guiData.toggleLocalChunkCache = s, guiData -> guiData.toggleLocalChunkCache)
                .build();

        private Integer entityDistanceValue;
        private Integer chunkRateValue;
        private Integer maxPathLengthValue;
        private Integer openNodesValue;
        private Integer totalNodesValue;
        private String toggleSkipEmptySections;
        private Integer chunkThreadPriorityValue;
        private String toggleParticles;
        private String toggleAnimations;
        private String resetToDefaults;
        private String switchTabGeneral;
        private String switchTabLighting;
        private String switchTabPathfinding;
        private String switchTabVisual;
        private String toggleBlockSectionCache;
        private String toggleBlockTypeCache;
        private String toggleLocalChunkCache;
    }
}
