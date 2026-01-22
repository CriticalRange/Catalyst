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

        // Set slider values for lighting
        uiCommandBuilder.set("#LightingBatchSlider.Value", com.criticalrange.CatalystConfig.LIGHTING_BATCH_SIZE);
        uiCommandBuilder.set("#LightingDistanceSlider.Value", com.criticalrange.CatalystConfig.LIGHTING_MAX_DISTANCE);

        // Set checkbox values for advanced lighting optimizations
        uiCommandBuilder.set("#LightPropOptToggle #CheckBox.Value", com.criticalrange.CatalystConfig.LIGHT_PROP_OPT_ENABLED);
        uiCommandBuilder.set("#OpacityCacheToggle #CheckBox.Value", com.criticalrange.CatalystConfig.OPACITY_CACHE_ENABLED);
        uiCommandBuilder.set("#FlatCacheToggle #CheckBox.Value", com.criticalrange.CatalystConfig.LIGHT_FLAT_CACHE_ENABLED);
        uiCommandBuilder.set("#QueueOptToggle #CheckBox.Value", com.criticalrange.CatalystConfig.LIGHT_QUEUE_OPT_ENABLED);
        uiCommandBuilder.set("#PackedOpsToggle #CheckBox.Value", com.criticalrange.CatalystConfig.PACKED_LIGHT_OPS_ENABLED);
        uiCommandBuilder.set("#SkipEmptyToggle #CheckBox.Value", com.criticalrange.CatalystConfig.SKIP_EMPTY_SECTIONS);

        // Set slider values for chunk generation
        uiCommandBuilder.set("#ChunkThreadPrioritySlider.Value", com.criticalrange.CatalystConfig.CHUNK_THREAD_PRIORITY);

        // Set checkbox values for visual effects
        uiCommandBuilder.set("#ParticlesToggle #CheckBox.Value", com.criticalrange.CatalystConfig.PARTICLES_ENABLED);
        uiCommandBuilder.set("#AnimationsToggle #CheckBox.Value", com.criticalrange.CatalystConfig.ANIMATIONS_ENABLED);

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
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#LightingBatchSlider",
            EventData.of("@SetLightingBatch", "#LightingBatchSlider.Value"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#LightingDistanceSlider",
            EventData.of("@SetLightingDistance", "#LightingDistanceSlider.Value"), false);

        // Add event bindings for advanced lighting
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#LightPropOptToggle #CheckBox",
            EventData.of("ToggleLightPropOpt", "CAT"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#OpacityCacheToggle #CheckBox",
            EventData.of("ToggleOpacityCache", "CAT"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#FlatCacheToggle #CheckBox",
            EventData.of("ToggleFlatCache", "CAT"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#QueueOptToggle #CheckBox",
            EventData.of("ToggleQueueOpt", "CAT"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#PackedOpsToggle #CheckBox",
            EventData.of("TogglePackedOps", "CAT"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#SkipEmptyToggle #CheckBox",
            EventData.of("ToggleSkipEmpty", "CAT"), false);

        // Add event bindings for chunk generation
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#ChunkThreadPrioritySlider",
            EventData.of("@SetChunkThreadPriority", "#ChunkThreadPrioritySlider.Value"), false);

        // Add event bindings for visual effects
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#ParticlesToggle #CheckBox",
            EventData.of("ToggleParticles", "CAT"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#AnimationsToggle #CheckBox",
            EventData.of("ToggleAnimations", "CAT"), false);

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
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TabAdvanced",
            EventData.of("SwitchTabAdvanced", "CAT"), false);
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

        // Lighting slider values
        if (data.lightingBatchValue != null) {
            int newValue = Math.max(1, Math.min(32, data.lightingBatchValue));
            setLightingBatchSize(newValue);
            commandBuilder.set("#LightingBatchSlider.Value", newValue);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Lighting Batch Size: " + newValue));
            needUpdate = true;
        }

        if (data.lightingDistanceValue != null) {
            int newValue = Math.max(2, Math.min(16, data.lightingDistanceValue));
            setLightingMaxDistance(newValue);
            commandBuilder.set("#LightingDistanceSlider.Value", newValue);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Lighting Max Distance: " + newValue));
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

        // Advanced lighting toggles
        if (data.toggleLightPropOpt != null) {
            boolean newState = toggleLightPropOpt();
            commandBuilder.set("#LightPropOptToggle #CheckBox.Value", newState);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Light Propagation Opt: " + (newState ? "ENABLED" : "DISABLED")));
            needUpdate = true;
        }

        if (data.toggleOpacityCache != null) {
            boolean newState = toggleOpacityCache();
            commandBuilder.set("#OpacityCacheToggle #CheckBox.Value", newState);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Opacity Cache: " + (newState ? "ENABLED" : "DISABLED")));
            needUpdate = true;
        }

        if (data.toggleFlatCache != null) {
            boolean newState = toggleFlatCache();
            commandBuilder.set("#FlatCacheToggle #CheckBox.Value", newState);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Flat Light Cache: " + (newState ? "ENABLED" : "DISABLED")));
            needUpdate = true;
        }

        if (data.toggleQueueOpt != null) {
            boolean newState = toggleQueueOpt();
            commandBuilder.set("#QueueOptToggle #CheckBox.Value", newState);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Queue Optimization: " + (newState ? "ENABLED" : "DISABLED")));
            needUpdate = true;
        }

        if (data.togglePackedOps != null) {
            boolean newState = togglePackedOps();
            commandBuilder.set("#PackedOpsToggle #CheckBox.Value", newState);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Packed Light Ops: " + (newState ? "ENABLED" : "DISABLED")));
            needUpdate = true;
        }

        if (data.toggleSkipEmpty != null) {
            boolean newState = toggleSkipEmpty();
            commandBuilder.set("#SkipEmptyToggle #CheckBox.Value", newState);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Skip Empty Sections: " + (newState ? "ENABLED" : "DISABLED")));
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
        if (data.switchTabAdvanced != null) {
            switchToTab(commandBuilder, "Advanced");
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
        commandBuilder.set("#PanelAdvanced.Visible", false);

        // Reset all indicators
        commandBuilder.set("#IndicatorGeneral.Background", "#12151c");
        commandBuilder.set("#IndicatorLighting.Background", "#12151c");
        commandBuilder.set("#IndicatorPathfinding.Background", "#12151c");
        commandBuilder.set("#IndicatorVisual.Background", "#12151c");
        commandBuilder.set("#IndicatorAdvanced.Background", "#12151c");

        // Reset all tab buttons to unselected style
        commandBuilder.set("#TabGeneral.Background", "#1a1d26");
        commandBuilder.set("#TabLighting.Background", "#1a1d26");
        commandBuilder.set("#TabPathfinding.Background", "#1a1d26");
        commandBuilder.set("#TabVisual.Background", "#1a1d26");
        commandBuilder.set("#TabAdvanced.Background", "#1a1d26");

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
            case "Advanced":
                commandBuilder.set("#PanelAdvanced.Visible", true);
                commandBuilder.set("#IndicatorAdvanced.Background", "#55aaff");
                commandBuilder.set("#TabAdvanced.Background", "#2d3444");
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

    // ===== Lighting Toggles and Setters =====

    private boolean toggleLightingBatch() {
        com.criticalrange.CatalystConfig.LIGHTING_BATCH_ENABLED = !com.criticalrange.CatalystConfig.LIGHTING_BATCH_ENABLED;
        return com.criticalrange.CatalystConfig.LIGHTING_BATCH_ENABLED;
    }

    private void setLightingBatchSize(int value) {
        com.criticalrange.CatalystConfig.LIGHTING_BATCH_SIZE = value;
    }

    private boolean toggleLightingDistance() {
        com.criticalrange.CatalystConfig.LIGHTING_DISTANCE_ENABLED = !com.criticalrange.CatalystConfig.LIGHTING_DISTANCE_ENABLED;
        return com.criticalrange.CatalystConfig.LIGHTING_DISTANCE_ENABLED;
    }

    private void setLightingMaxDistance(int value) {
        com.criticalrange.CatalystConfig.LIGHTING_MAX_DISTANCE = value;
    }

    // ===== Advanced Lighting Toggles =====

    private boolean toggleLightPropOpt() {
        boolean newState = !com.criticalrange.CatalystConfig.LIGHT_PROP_OPT_ENABLED;
        com.criticalrange.CatalystConfig.LIGHT_PROP_OPT_ENABLED = newState;
        syncAdvancedLightingField("com.hypixel.hytale.server.core.universe.world.lighting.FloodLightCalculation",
            "$catalystLightPropOptEnabled", newState);
        return newState;
    }

    private boolean toggleOpacityCache() {
        boolean newState = !com.criticalrange.CatalystConfig.OPACITY_CACHE_ENABLED;
        com.criticalrange.CatalystConfig.OPACITY_CACHE_ENABLED = newState;
        syncAdvancedLightingField("com.hypixel.hytale.server.core.universe.world.lighting.FloodLightCalculation",
            "$catalystOpacityCacheEnabled", newState);
        return newState;
    }

    private boolean toggleFlatCache() {
        boolean newState = !com.criticalrange.CatalystConfig.LIGHT_FLAT_CACHE_ENABLED;
        com.criticalrange.CatalystConfig.LIGHT_FLAT_CACHE_ENABLED = newState;
        syncAdvancedLightingField("com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkLightDataBuilder",
            "$catalystFlatCacheEnabled", newState);
        return newState;
    }

    private boolean toggleQueueOpt() {
        boolean newState = !com.criticalrange.CatalystConfig.LIGHT_QUEUE_OPT_ENABLED;
        com.criticalrange.CatalystConfig.LIGHT_QUEUE_OPT_ENABLED = newState;
        syncAdvancedLightingField("com.hypixel.hytale.server.core.universe.world.lighting.FloodLightCalculation",
            "$catalystQueueOptEnabled", newState);
        return newState;
    }

    private boolean togglePackedOps() {
        boolean newState = !com.criticalrange.CatalystConfig.PACKED_LIGHT_OPS_ENABLED;
        com.criticalrange.CatalystConfig.PACKED_LIGHT_OPS_ENABLED = newState;
        syncAdvancedLightingField("com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkLightData",
            "$catalystPackedOpsEnabled", newState);
        return newState;
    }

    private boolean toggleSkipEmpty() {
        boolean newState = !com.criticalrange.CatalystConfig.SKIP_EMPTY_SECTIONS;
        com.criticalrange.CatalystConfig.SKIP_EMPTY_SECTIONS = newState;
        syncAdvancedLightingField("com.hypixel.hytale.server.core.universe.world.lighting.FloodLightCalculation",
            "$catalystSkipEmptySections", newState);
        return newState;
    }

    private void syncAdvancedLightingField(String className, String fieldName, boolean value) {
        try {
            Class<?> clazz = Class.forName(className);
            java.lang.reflect.Field field = clazz.getField(fieldName);
            field.setBoolean(null, value);
        } catch (Exception e) {
            System.err.println("[Catalyst] Failed to sync " + fieldName + ": " + e.getMessage());
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

    // ===== Visual Effects Toggles =====

    private boolean toggleParticles() {
        boolean newState = !VisualEffectsToggle.particlesEnabled;
        VisualEffectsToggle.particlesEnabled = newState;
        com.criticalrange.CatalystConfig.PARTICLES_ENABLED = newState;
        return newState;
    }

    private boolean toggleAnimations() {
        boolean newState = !VisualEffectsToggle.animationsEnabled;
        VisualEffectsToggle.animationsEnabled = newState;
        com.criticalrange.CatalystConfig.ANIMATIONS_ENABLED = newState;
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
        if (com.criticalrange.CatalystConfig.ENTITY_VIEW_MULTIPLIER != 32) {
            setEntityViewMultiplier(32);
        }

        if (com.criticalrange.CatalystConfig.CHUNK_RATE_ENABLED) {
            toggleChunkRate();  // Toggle to false
        }
        if (com.criticalrange.CatalystConfig.CHUNKS_PER_TICK != 4) {
            setChunksPerTick(4);
        }

        // Pathfinding defaults (disabled, vanilla limits)
        if (com.criticalrange.CatalystConfig.PATHFINDING_ENABLED) {
            togglePathfinding();  // Toggle to false
        }
        if (com.criticalrange.CatalystConfig.MAX_PATH_LENGTH != 200) {
            setMaxPathLength(200);
        }
        if (com.criticalrange.CatalystConfig.OPEN_NODES_LIMIT != 80) {
            setOpenNodesLimit(80);
        }
        if (com.criticalrange.CatalystConfig.TOTAL_NODES_LIMIT != 400) {
            setTotalNodesLimit(400);
        }

        // Lighting defaults
        com.criticalrange.CatalystConfig.LIGHTING_BATCH_ENABLED = false;
        com.criticalrange.CatalystConfig.LIGHTING_BATCH_SIZE = 8;
        com.criticalrange.CatalystConfig.LIGHTING_DISTANCE_ENABLED = false;
        com.criticalrange.CatalystConfig.LIGHTING_MAX_DISTANCE = 8;

        // Advanced lighting defaults (all enabled by default)
        // Note: These only update config values - no transformers implemented yet
        com.criticalrange.CatalystConfig.LIGHT_PROP_OPT_ENABLED = true;
        com.criticalrange.CatalystConfig.OPACITY_CACHE_ENABLED = true;
        com.criticalrange.CatalystConfig.LIGHT_FLAT_CACHE_ENABLED = true;
        com.criticalrange.CatalystConfig.LIGHT_QUEUE_OPT_ENABLED = true;
        com.criticalrange.CatalystConfig.PACKED_LIGHT_OPS_ENABLED = true;
        com.criticalrange.CatalystConfig.SKIP_EMPTY_SECTIONS = true;

        // Chunk generation defaults
        com.criticalrange.CatalystConfig.CHUNK_THREAD_PRIORITY_ENABLED = false;
        com.criticalrange.CatalystConfig.CHUNK_THREAD_PRIORITY = 5;

        // Visual effects defaults (enabled by default)
        VisualEffectsToggle.particlesEnabled = true;
        VisualEffectsToggle.animationsEnabled = true;
        com.criticalrange.CatalystConfig.PARTICLES_ENABLED = true;
        com.criticalrange.CatalystConfig.ANIMATIONS_ENABLED = true;

        // Update all UI elements to reflect default values
        commandBuilder.set("#EntityDistanceSlider.Value", 32);
        commandBuilder.set("#ChunkRateSlider.Value", 4);
        commandBuilder.set("#MaxPathLengthSlider.Value", 200);
        commandBuilder.set("#OpenNodesSlider.Value", 80);
        commandBuilder.set("#TotalNodesSlider.Value", 400);
        commandBuilder.set("#LightingBatchSlider.Value", 8);
        commandBuilder.set("#LightingDistanceSlider.Value", 8);
        commandBuilder.set("#ChunkThreadPrioritySlider.Value", 5);
        commandBuilder.set("#ParticlesToggle #CheckBox.Value", true);
        commandBuilder.set("#AnimationsToggle #CheckBox.Value", true);
        commandBuilder.set("#LightPropOptToggle #CheckBox.Value", true);
        commandBuilder.set("#OpacityCacheToggle #CheckBox.Value", true);
        commandBuilder.set("#FlatCacheToggle #CheckBox.Value", true);
        commandBuilder.set("#QueueOptToggle #CheckBox.Value", true);
        commandBuilder.set("#PackedOpsToggle #CheckBox.Value", true);
        commandBuilder.set("#SkipEmptyToggle #CheckBox.Value", true);
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
        static final String KEY_SET_LIGHTING_BATCH = "@SetLightingBatch";
        static final String KEY_SET_LIGHTING_DISTANCE = "@SetLightingDistance";
        static final String KEY_SET_CHUNK_THREAD_PRIORITY = "@SetChunkThreadPriority";
        static final String KEY_TOGGLE_PARTICLES = "ToggleParticles";
        static final String KEY_TOGGLE_ANIMATIONS = "ToggleAnimations";
        static final String KEY_TOGGLE_LIGHT_PROP_OPT = "ToggleLightPropOpt";
        static final String KEY_TOGGLE_OPACITY_CACHE = "ToggleOpacityCache";
        static final String KEY_TOGGLE_FLAT_CACHE = "ToggleFlatCache";
        static final String KEY_TOGGLE_QUEUE_OPT = "ToggleQueueOpt";
        static final String KEY_TOGGLE_PACKED_OPS = "TogglePackedOps";
        static final String KEY_TOGGLE_SKIP_EMPTY = "ToggleSkipEmpty";
        static final String KEY_RESET_TO_DEFAULTS = "ResetToDefaults";
        static final String KEY_SWITCH_TAB_GENERAL = "SwitchTabGeneral";
        static final String KEY_SWITCH_TAB_LIGHTING = "SwitchTabLighting";
        static final String KEY_SWITCH_TAB_PATHFINDING = "SwitchTabPathfinding";
        static final String KEY_SWITCH_TAB_VISUAL = "SwitchTabVisual";
        static final String KEY_SWITCH_TAB_ADVANCED = "SwitchTabAdvanced";

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
                .addField(new KeyedCodec<>(KEY_SET_LIGHTING_BATCH, Codec.INTEGER),
                    (guiData, i) -> guiData.lightingBatchValue = i, guiData -> guiData.lightingBatchValue)
                .addField(new KeyedCodec<>(KEY_SET_LIGHTING_DISTANCE, Codec.INTEGER),
                    (guiData, i) -> guiData.lightingDistanceValue = i, guiData -> guiData.lightingDistanceValue)
                .addField(new KeyedCodec<>(KEY_SET_CHUNK_THREAD_PRIORITY, Codec.INTEGER),
                    (guiData, i) -> guiData.chunkThreadPriorityValue = i, guiData -> guiData.chunkThreadPriorityValue)
                .addField(new KeyedCodec<>(KEY_TOGGLE_PARTICLES, Codec.STRING),
                    (guiData, s) -> guiData.toggleParticles = s, guiData -> guiData.toggleParticles)
                .addField(new KeyedCodec<>(KEY_TOGGLE_ANIMATIONS, Codec.STRING),
                    (guiData, s) -> guiData.toggleAnimations = s, guiData -> guiData.toggleAnimations)
                .addField(new KeyedCodec<>(KEY_TOGGLE_LIGHT_PROP_OPT, Codec.STRING),
                    (guiData, s) -> guiData.toggleLightPropOpt = s, guiData -> guiData.toggleLightPropOpt)
                .addField(new KeyedCodec<>(KEY_TOGGLE_OPACITY_CACHE, Codec.STRING),
                    (guiData, s) -> guiData.toggleOpacityCache = s, guiData -> guiData.toggleOpacityCache)
                .addField(new KeyedCodec<>(KEY_TOGGLE_FLAT_CACHE, Codec.STRING),
                    (guiData, s) -> guiData.toggleFlatCache = s, guiData -> guiData.toggleFlatCache)
                .addField(new KeyedCodec<>(KEY_TOGGLE_QUEUE_OPT, Codec.STRING),
                    (guiData, s) -> guiData.toggleQueueOpt = s, guiData -> guiData.toggleQueueOpt)
                .addField(new KeyedCodec<>(KEY_TOGGLE_PACKED_OPS, Codec.STRING),
                    (guiData, s) -> guiData.togglePackedOps = s, guiData -> guiData.togglePackedOps)
                .addField(new KeyedCodec<>(KEY_TOGGLE_SKIP_EMPTY, Codec.STRING),
                    (guiData, s) -> guiData.toggleSkipEmpty = s, guiData -> guiData.toggleSkipEmpty)
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
                .addField(new KeyedCodec<>(KEY_SWITCH_TAB_ADVANCED, Codec.STRING),
                    (guiData, s) -> guiData.switchTabAdvanced = s, guiData -> guiData.switchTabAdvanced)
                .build();

        private Integer entityDistanceValue;
        private Integer chunkRateValue;
        private Integer maxPathLengthValue;
        private Integer openNodesValue;
        private Integer totalNodesValue;
        private Integer lightingBatchValue;
        private Integer lightingDistanceValue;
        private Integer chunkThreadPriorityValue;
        private String toggleParticles;
        private String toggleAnimations;
        private String toggleLightPropOpt;
        private String toggleOpacityCache;
        private String toggleFlatCache;
        private String toggleQueueOpt;
        private String togglePackedOps;
        private String toggleSkipEmpty;
        private String resetToDefaults;
        private String switchTabGeneral;
        private String switchTabLighting;
        private String switchTabPathfinding;
        private String switchTabVisual;
        private String switchTabAdvanced;
    }
}
