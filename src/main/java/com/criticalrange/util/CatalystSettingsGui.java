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
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#TabAdvanced",
            EventData.of("SwitchTabAdvanced", "CAT"), false);

        // Set values for advanced optimizations
        uiCommandBuilder.set("#BlockEntitySleepCheckBox.Value", com.criticalrange.CatalystConfig.BLOCK_ENTITY_SLEEP_ENABLED);
        uiCommandBuilder.set("#BlockEntitySleepIntervalSlider.Value", com.criticalrange.CatalystConfig.BLOCK_ENTITY_SLEEP_INTERVAL);
        uiCommandBuilder.set("#StatRecalcThrottleCheckBox.Value", com.criticalrange.CatalystConfig.STAT_RECALC_THROTTLE_ENABLED);
        uiCommandBuilder.set("#StatRecalcIntervalSlider.Value", com.criticalrange.CatalystConfig.STAT_RECALC_INTERVAL);
        uiCommandBuilder.set("#BlockUpdateBatchingCheckBox.Value", com.criticalrange.CatalystConfig.BLOCK_UPDATE_BATCHING_ENABLED);
        uiCommandBuilder.set("#BlockUpdateBatchSizeSlider.Value", com.criticalrange.CatalystConfig.BLOCK_UPDATE_BATCH_SIZE);
        uiCommandBuilder.set("#FloodFillLimitCheckBox.Value", com.criticalrange.CatalystConfig.FLOOD_FILL_LIMIT_ENABLED);
        uiCommandBuilder.set("#FloodFillMaxIterationsSlider.Value", com.criticalrange.CatalystConfig.FLOOD_FILL_MAX_ITERATIONS);
        uiCommandBuilder.set("#PathfindingPoolCheckBox.Value", com.criticalrange.CatalystConfig.PATHFINDING_POOL_ENABLED);
        uiCommandBuilder.set("#PathfindingPoolSizeSlider.Value", com.criticalrange.CatalystConfig.PATHFINDING_POOL_SIZE);

        // Add event bindings for search (CompactTextField uses ValueChanged with .Value)
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#SearchInput",
            EventData.of("@SearchQuery", "#SearchInput.Value"), false);

        // Add event bindings for advanced optimizations
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#BlockEntitySleepCheckBox",
            EventData.of("ToggleBlockEntitySleep", "CAT"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#BlockEntitySleepIntervalSlider",
            EventData.of("@SetBlockEntitySleepInterval", "#BlockEntitySleepIntervalSlider.Value"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#StatRecalcThrottleCheckBox",
            EventData.of("ToggleStatRecalcThrottle", "CAT"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#StatRecalcIntervalSlider",
            EventData.of("@SetStatRecalcInterval", "#StatRecalcIntervalSlider.Value"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#BlockUpdateBatchingCheckBox",
            EventData.of("ToggleBlockUpdateBatching", "CAT"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#BlockUpdateBatchSizeSlider",
            EventData.of("@SetBlockUpdateBatchSize", "#BlockUpdateBatchSizeSlider.Value"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#FloodFillLimitCheckBox",
            EventData.of("ToggleFloodFillLimit", "CAT"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#FloodFillMaxIterationsSlider",
            EventData.of("@SetFloodFillMaxIterations", "#FloodFillMaxIterationsSlider.Value"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#PathfindingPoolCheckBox",
            EventData.of("TogglePathfindingPool", "CAT"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#PathfindingPoolSizeSlider",
            EventData.of("@SetPathfindingPoolSize", "#PathfindingPoolSizeSlider.Value"), false);
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

        // Search handling
        if (data.searchQuery != null) {
            handleSearch(commandBuilder, eventBuilder, data.searchQuery);
            needUpdate = true;
        }

        // Tab switching - also clear search
        if (data.switchTabGeneral != null) {
            clearSearch(commandBuilder);
            switchToTab(commandBuilder, "General");
            needUpdate = true;
        }
        if (data.switchTabLighting != null) {
            clearSearch(commandBuilder);
            switchToTab(commandBuilder, "Lighting");
            needUpdate = true;
        }
        if (data.switchTabPathfinding != null) {
            clearSearch(commandBuilder);
            switchToTab(commandBuilder, "Pathfinding");
            needUpdate = true;
        }
        if (data.switchTabVisual != null) {
            clearSearch(commandBuilder);
            switchToTab(commandBuilder, "Visual");
            needUpdate = true;
        }
        if (data.switchTabAdvanced != null) {
            clearSearch(commandBuilder);
            switchToTab(commandBuilder, "Advanced");
            needUpdate = true;
        }

        // Advanced optimization toggles
        if (data.toggleBlockEntitySleep != null) {
            boolean newState = toggleBlockEntitySleep();
            commandBuilder.set("#BlockEntitySleepCheckBox.Value", newState);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Skip Idle Block Entities: " + (newState ? "ENABLED" : "DISABLED")));
            needUpdate = true;
        }

        if (data.blockEntitySleepIntervalValue != null) {
            int newValue = Math.max(1, Math.min(100, data.blockEntitySleepIntervalValue));
            setBlockEntitySleepInterval(newValue);
            commandBuilder.set("#BlockEntitySleepIntervalSlider.Value", newValue);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Block Entity Sleep Interval: " + newValue + " ticks"));
            needUpdate = true;
        }

        if (data.toggleStatRecalcThrottle != null) {
            boolean newState = toggleStatRecalcThrottle();
            commandBuilder.set("#StatRecalcThrottleCheckBox.Value", newState);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Skip Stat Modifiers Recalc: " + (newState ? "ENABLED" : "DISABLED")));
            needUpdate = true;
        }

        if (data.statRecalcIntervalValue != null) {
            int newValue = Math.max(1, Math.min(40, data.statRecalcIntervalValue));
            setStatRecalcInterval(newValue);
            commandBuilder.set("#StatRecalcIntervalSlider.Value", newValue);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Stat Recalc Interval: " + newValue + " ticks"));
            needUpdate = true;
        }

        if (data.toggleBlockUpdateBatching != null) {
            boolean newState = toggleBlockUpdateBatching();
            commandBuilder.set("#BlockUpdateBatchingCheckBox.Value", newState);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Block Update Batching: " + (newState ? "ENABLED" : "DISABLED")));
            needUpdate = true;
        }

        if (data.blockUpdateBatchSizeValue != null) {
            int newValue = Math.max(16, Math.min(256, data.blockUpdateBatchSizeValue));
            setBlockUpdateBatchSize(newValue);
            commandBuilder.set("#BlockUpdateBatchSizeSlider.Value", newValue);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Block Update Batch Size: " + newValue));
            needUpdate = true;
        }

        if (data.toggleFloodFillLimit != null) {
            boolean newState = toggleFloodFillLimit();
            commandBuilder.set("#FloodFillLimitCheckBox.Value", newState);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Flood Fill Depth Limit: " + (newState ? "ENABLED" : "DISABLED")));
            needUpdate = true;
        }

        if (data.floodFillMaxIterationsValue != null) {
            int newValue = Math.max(1000, Math.min(20000, data.floodFillMaxIterationsValue));
            setFloodFillMaxIterations(newValue);
            commandBuilder.set("#FloodFillMaxIterationsSlider.Value", newValue);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Flood Fill Max Iterations: " + newValue));
            needUpdate = true;
        }

        if (data.togglePathfindingPool != null) {
            boolean newState = togglePathfindingPool();
            commandBuilder.set("#PathfindingPoolCheckBox.Value", newState);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Pathfinding Memory Pool: " + (newState ? "ENABLED" : "DISABLED")));
            needUpdate = true;
        }

        if (data.pathfindingPoolSizeValue != null) {
            int newValue = Math.max(128, Math.min(2048, data.pathfindingPoolSizeValue));
            setPathfindingPoolSize(newValue);
            commandBuilder.set("#PathfindingPoolSizeSlider.Value", newValue);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Pathfinding Pool Size: " + newValue));
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

    // ===== Search Functionality =====

    private static final SearchableSetting[] SEARCHABLE_SETTINGS = {
        // General tab - sliders use existing element IDs, checkboxes use existing IDs
        new SearchableSetting("Entity View Multiplier", "entity view distance range visibility", "General", 
            "slider", "#EntityDistanceSlider", "@SetEntityDistance", 1, 64),
        new SearchableSetting("Chunks Per Tick", "chunk loading rate speed generation", "General", 
            "slider", "#ChunkRateSlider", "@SetChunkRate", 1, 16),
        new SearchableSetting("Block Section Cache", "cache section memory optimization", "General", 
            "checkbox", "#BlockSectionCacheCheckBox", "ToggleBlockSectionCache", 0, 0),
        new SearchableSetting("Block Type Cache", "cache type lookup optimization", "General", 
            "checkbox", "#BlockTypeCacheCheckBox", "ToggleBlockTypeCache", 0, 0),
        new SearchableSetting("Local Chunk Cache", "cache chunk lighting optimization", "General", 
            "checkbox", "#LocalChunkCacheCheckBox", "ToggleLocalChunkCache", 0, 0),
        new SearchableSetting("Thread Priority", "chunk generation thread priority cpu", "General", 
            "slider", "#ChunkThreadPrioritySlider", "@SetChunkThreadPriority", 1, 10),
        // Lighting tab
        new SearchableSetting("Skip Empty Sections", "light lighting empty air optimization", "Lighting", 
            "checkbox", "#SkipEmptySectionsCheckBox", "ToggleSkipEmptySections", 0, 0),
        // Pathfinding tab
        new SearchableSetting("Max Path Length", "pathfinding npc path length limit", "Pathfinding", 
            "slider", "#MaxPathLengthSlider", "@SetMaxPathLength", 50, 500),
        new SearchableSetting("Open Nodes Limit", "pathfinding npc nodes memory", "Pathfinding", 
            "slider", "#OpenNodesSlider", "@SetOpenNodes", 20, 200),
        new SearchableSetting("Total Nodes Limit", "pathfinding npc total nodes limit", "Pathfinding", 
            "slider", "#TotalNodesSlider", "@SetTotalNodes", 100, 1000),
        // Visual tab
        new SearchableSetting("Particle Effects", "particles visual effects toggle", "Visual", 
            "checkbox", "#ParticlesCheckBox", "ToggleParticles", 0, 0),
        new SearchableSetting("NPC Animations", "animations npc visual toggle", "Visual", 
            "checkbox", "#AnimationsCheckBox", "ToggleAnimations", 0, 0),
        // Advanced tab
        new SearchableSetting("Skip Idle Block Entities", "block entity sleep idle tick optimization", "Advanced", 
            "checkbox", "#BlockEntitySleepCheckBox", "ToggleBlockEntitySleep", 0, 0),
        new SearchableSetting("Sleep Interval", "block entity sleep interval ticks", "Advanced", 
            "slider", "#BlockEntitySleepIntervalSlider", "@SetBlockEntitySleepInterval", 1, 100),
        new SearchableSetting("Throttle Stat Recalc", "stat modifiers recalculation throttle cpu", "Advanced", 
            "checkbox", "#StatRecalcThrottleCheckBox", "ToggleStatRecalcThrottle", 0, 0),
        new SearchableSetting("Recalc Interval", "stat recalculation interval ticks", "Advanced", 
            "slider", "#StatRecalcIntervalSlider", "@SetStatRecalcInterval", 1, 40),
        new SearchableSetting("Batch Notifications", "block update batching network traffic", "Advanced", 
            "checkbox", "#BlockUpdateBatchingCheckBox", "ToggleBlockUpdateBatching", 0, 0),
        new SearchableSetting("Batch Size", "block update batch size network", "Advanced", 
            "slider", "#BlockUpdateBatchSizeSlider", "@SetBlockUpdateBatchSize", 16, 256),
        new SearchableSetting("Limit Flood Fill", "flood fill spawn depth limit", "Advanced", 
            "checkbox", "#FloodFillLimitCheckBox", "ToggleFloodFillLimit", 0, 0),
        new SearchableSetting("Max Flood Fill Depth", "flood fill max iterations depth", "Advanced", 
            "slider", "#FloodFillMaxIterationsSlider", "@SetFloodFillMaxIterations", 1000, 20000),
        new SearchableSetting("Pathfinding Node Pool", "pathfinding pool memory garbage collection", "Advanced", 
            "checkbox", "#PathfindingPoolCheckBox", "TogglePathfindingPool", 0, 0),
        new SearchableSetting("Pool Size", "pathfinding pool size memory", "Advanced", 
            "slider", "#PathfindingPoolSizeSlider", "@SetPathfindingPoolSize", 128, 2048),
    };

    private static class SearchableSetting {
        final String name;
        final String keywords;
        final String tab;
        final String controlType; // "slider" or "checkbox"
        final String elementId;
        final String eventKey;
        final int min;
        final int max;

        SearchableSetting(String name, String keywords, String tab, String controlType, String elementId, String eventKey, int min, int max) {
            this.name = name;
            this.keywords = keywords.toLowerCase();
            this.tab = tab;
            this.controlType = controlType;
            this.elementId = elementId;
            this.eventKey = eventKey;
            this.min = min;
            this.max = max;
        }

        boolean matches(String query) {
            String lowerQuery = query.toLowerCase();
            return name.toLowerCase().contains(lowerQuery) || keywords.contains(lowerQuery);
        }
    }

    private boolean getCheckboxValue(String eventKey) {
        switch (eventKey) {
            case "ToggleBlockSectionCache": return com.criticalrange.CatalystConfig.BLOCK_SECTION_CACHE_ENABLED;
            case "ToggleBlockTypeCache": return com.criticalrange.CatalystConfig.BLOCK_TYPE_CACHE_ENABLED;
            case "ToggleLocalChunkCache": return com.criticalrange.CatalystConfig.LOCAL_CHUNK_CACHE_ENABLED;
            case "ToggleSkipEmptySections": return com.criticalrange.CatalystConfig.LIGHT_SKIP_EMPTY_ENABLED;
            case "ToggleParticles": return com.criticalrange.CatalystConfig.PARTICLES_ENABLED;
            case "ToggleAnimations": return com.criticalrange.CatalystConfig.ANIMATIONS_ENABLED;
            case "ToggleBlockEntitySleep": return com.criticalrange.CatalystConfig.BLOCK_ENTITY_SLEEP_ENABLED;
            case "ToggleStatRecalcThrottle": return com.criticalrange.CatalystConfig.STAT_RECALC_THROTTLE_ENABLED;
            case "ToggleBlockUpdateBatching": return com.criticalrange.CatalystConfig.BLOCK_UPDATE_BATCHING_ENABLED;
            case "ToggleFloodFillLimit": return com.criticalrange.CatalystConfig.FLOOD_FILL_LIMIT_ENABLED;
            case "TogglePathfindingPool": return com.criticalrange.CatalystConfig.PATHFINDING_POOL_ENABLED;
            default: return false;
        }
    }

    private void clearSearch(UICommandBuilder commandBuilder) {
        // Hide search panel and show tabs
        commandBuilder.set("#PanelSearch.Visible", false);
        commandBuilder.set("#PanelsContainer.Visible", true);
        // Clear search input
        commandBuilder.set("#SearchInput.Value", "");
        // Clear search results
        commandBuilder.clear("#SearchResultsList");
    }

    private void handleSearch(UICommandBuilder commandBuilder, UIEventBuilder eventBuilder, String query) {
        if (query == null || query.trim().isEmpty()) {
            // Hide search panel and show tabs when search is cleared
            commandBuilder.set("#PanelSearch.Visible", false);
            commandBuilder.set("#PanelsContainer.Visible", true);
            return;
        }

        // Hide tabs and show search panel
        commandBuilder.set("#PanelsContainer.Visible", false);
        commandBuilder.set("#PanelSearch.Visible", true);

        // Clear previous search results
        commandBuilder.clear("#SearchResultsList");

        // Find matching settings
        java.util.List<SearchableSetting> matches = new java.util.ArrayList<>();
        for (SearchableSetting setting : SEARCHABLE_SETTINGS) {
            if (setting.matches(query)) {
                matches.add(setting);
            }
        }

        if (matches.isEmpty()) {
            commandBuilder.set("#NoResultsLabel.Visible", true);
            commandBuilder.set("#SearchResultsLabel.Text", "No Results");
            commandBuilder.set("#SearchResultsList.Visible", false);
        } else {
            commandBuilder.set("#NoResultsLabel.Visible", false);
            commandBuilder.set("#SearchResultsList.Visible", true);
            commandBuilder.set("#SearchResultsLabel.Text", "Found " + matches.size() + " setting" + (matches.size() > 1 ? "s" : ""));
            
            // Append UI templates and configure each one using indexed selectors
            for (int i = 0; i < matches.size(); i++) {
                SearchableSetting match = matches.get(i);
                
                if ("checkbox".equals(match.controlType)) {
                    // Append checkbox template
                    commandBuilder.append("#SearchResultsList", "Components/SearchResultCheckbox.ui");
                    // Configure using indexed selector
                    commandBuilder.set("#SearchResultsList[" + i + "] #SearchResultLabel.Text", match.name);
                    // Set current checkbox value
                    commandBuilder.set("#SearchResultsList[" + i + "] #SearchResultCheckBox.Value", getCheckboxValue(match.eventKey));
                    // Add event binding for checkbox ValueChanged
                    eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, 
                        "#SearchResultsList[" + i + "] #SearchResultCheckBox",
                        EventData.of(match.eventKey, "CAT"), false);
                } else {
                    // Append slider template
                    commandBuilder.append("#SearchResultsList", "Components/SearchResultSlider.ui");
                    // Configure using indexed selector
                    commandBuilder.set("#SearchResultsList[" + i + "] #SearchResultLabel.Text", match.name);
                    commandBuilder.set("#SearchResultsList[" + i + "] #SearchResultSlider.Min", match.min);
                    commandBuilder.set("#SearchResultsList[" + i + "] #SearchResultSlider.Max", match.max);
                    // Add event binding for this slider
                    eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged,
                        "#SearchResultsList[" + i + "] #SearchResultSlider",
                        EventData.of(match.eventKey, "#SearchResultsList[" + i + "] #SearchResultSlider.Value"), false);
                }
            }
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

    // ===== Advanced Optimization Toggles =====

    private boolean toggleBlockEntitySleep() {
        boolean newState = !com.criticalrange.CatalystConfig.BLOCK_ENTITY_SLEEP_ENABLED;
        com.criticalrange.CatalystConfig.BLOCK_ENTITY_SLEEP_ENABLED = newState;
        try {
            Class<?> clazz = Class.forName("com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection");
            clazz.getField("$catalystBlockEntitySleepEnabled").setBoolean(null, newState);
        } catch (Exception e) { /* ignore */ }
        return newState;
    }

    private void setBlockEntitySleepInterval(int value) {
        com.criticalrange.CatalystConfig.BLOCK_ENTITY_SLEEP_INTERVAL = value;
        try {
            Class<?> clazz = Class.forName("com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection");
            clazz.getField("$catalystBlockEntitySleepInterval").setInt(null, value);
        } catch (Exception e) { /* ignore */ }
    }

    private boolean toggleStatRecalcThrottle() {
        boolean newState = !com.criticalrange.CatalystConfig.STAT_RECALC_THROTTLE_ENABLED;
        com.criticalrange.CatalystConfig.STAT_RECALC_THROTTLE_ENABLED = newState;
        try {
            Class<?> clazz = Class.forName("com.hypixel.hytale.server.core.entity.StatModifiersManager");
            clazz.getField("$catalystStatRecalcThrottleEnabled").setBoolean(null, newState);
        } catch (Exception e) { /* ignore */ }
        return newState;
    }

    private void setStatRecalcInterval(int value) {
        com.criticalrange.CatalystConfig.STAT_RECALC_INTERVAL = value;
        try {
            Class<?> clazz = Class.forName("com.hypixel.hytale.server.core.entity.StatModifiersManager");
            clazz.getField("$catalystStatRecalcInterval").setInt(null, value);
        } catch (Exception e) { /* ignore */ }
    }

    private boolean toggleBlockUpdateBatching() {
        boolean newState = !com.criticalrange.CatalystConfig.BLOCK_UPDATE_BATCHING_ENABLED;
        com.criticalrange.CatalystConfig.BLOCK_UPDATE_BATCHING_ENABLED = newState;
        try {
            Class<?> clazz = Class.forName("com.hypixel.hytale.server.core.universe.world.WorldNotificationHandler");
            clazz.getField("$catalystBlockUpdateBatchingEnabled").setBoolean(null, newState);
        } catch (Exception e) { /* ignore */ }
        return newState;
    }

    private void setBlockUpdateBatchSize(int value) {
        com.criticalrange.CatalystConfig.BLOCK_UPDATE_BATCH_SIZE = value;
        try {
            Class<?> clazz = Class.forName("com.hypixel.hytale.server.core.universe.world.WorldNotificationHandler");
            clazz.getField("$catalystBlockUpdateBatchSize").setInt(null, value);
        } catch (Exception e) { /* ignore */ }
    }

    private boolean toggleFloodFillLimit() {
        boolean newState = !com.criticalrange.CatalystConfig.FLOOD_FILL_LIMIT_ENABLED;
        com.criticalrange.CatalystConfig.FLOOD_FILL_LIMIT_ENABLED = newState;
        try {
            Class<?> clazz = Class.forName("com.hypixel.hytale.server.spawning.util.FloodFillPositionSelector");
            clazz.getField("$catalystFloodFillLimitEnabled").setBoolean(null, newState);
        } catch (Exception e) { /* ignore */ }
        return newState;
    }

    private void setFloodFillMaxIterations(int value) {
        com.criticalrange.CatalystConfig.FLOOD_FILL_MAX_ITERATIONS = value;
        try {
            Class<?> clazz = Class.forName("com.hypixel.hytale.server.spawning.util.FloodFillPositionSelector");
            clazz.getField("$catalystFloodFillMaxIterations").setInt(null, value);
        } catch (Exception e) { /* ignore */ }
    }

    private boolean togglePathfindingPool() {
        boolean newState = !com.criticalrange.CatalystConfig.PATHFINDING_POOL_ENABLED;
        com.criticalrange.CatalystConfig.PATHFINDING_POOL_ENABLED = newState;
        try {
            Class<?> clazz = Class.forName("com.hypixel.hytale.server.npc.navigation.AStarBase");
            clazz.getField("$catalystPathfindingPoolEnabled").setBoolean(null, newState);
        } catch (Exception e) { /* ignore */ }
        return newState;
    }

    private void setPathfindingPoolSize(int value) {
        com.criticalrange.CatalystConfig.PATHFINDING_POOL_SIZE = value;
        try {
            Class<?> clazz = Class.forName("com.hypixel.hytale.server.npc.navigation.AStarBase");
            clazz.getField("$catalystPathfindingPoolSize").setInt(null, value);
        } catch (Exception e) { /* ignore */ }
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
        com.criticalrange.CatalystConfig.CHUNK_THREAD_PRIORITY = Thread.NORM_PRIORITY;

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

        // Advanced optimization defaults
        com.criticalrange.CatalystConfig.BLOCK_ENTITY_SLEEP_ENABLED = false;
        com.criticalrange.CatalystConfig.BLOCK_ENTITY_SLEEP_INTERVAL = 20;
        com.criticalrange.CatalystConfig.STAT_RECALC_THROTTLE_ENABLED = false;
        com.criticalrange.CatalystConfig.STAT_RECALC_INTERVAL = 5;
        com.criticalrange.CatalystConfig.BLOCK_UPDATE_BATCHING_ENABLED = false;
        com.criticalrange.CatalystConfig.BLOCK_UPDATE_BATCH_SIZE = 64;
        com.criticalrange.CatalystConfig.FLOOD_FILL_LIMIT_ENABLED = false;
        com.criticalrange.CatalystConfig.FLOOD_FILL_MAX_ITERATIONS = 5000;
        com.criticalrange.CatalystConfig.PATHFINDING_POOL_ENABLED = false;
        com.criticalrange.CatalystConfig.PATHFINDING_POOL_SIZE = 512;

        // Sync advanced optimizations to injected fields
        try {
            Class<?> blockSectionClass = Class.forName("com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection");
            blockSectionClass.getField("$catalystBlockEntitySleepEnabled").setBoolean(null, false);
            blockSectionClass.getField("$catalystBlockEntitySleepInterval").setInt(null, 20);
        } catch (Exception e) { /* ignore */ }
        try {
            Class<?> statModifiersClass = Class.forName("com.hypixel.hytale.server.core.entity.StatModifiersManager");
            statModifiersClass.getField("$catalystStatRecalcThrottleEnabled").setBoolean(null, false);
            statModifiersClass.getField("$catalystStatRecalcInterval").setInt(null, 5);
        } catch (Exception e) { /* ignore */ }
        try {
            Class<?> notificationClass = Class.forName("com.hypixel.hytale.server.core.universe.world.WorldNotificationHandler");
            notificationClass.getField("$catalystBlockUpdateBatchingEnabled").setBoolean(null, false);
            notificationClass.getField("$catalystBlockUpdateBatchSize").setInt(null, 64);
        } catch (Exception e) { /* ignore */ }
        try {
            Class<?> floodFillClass = Class.forName("com.hypixel.hytale.server.spawning.util.FloodFillPositionSelector");
            floodFillClass.getField("$catalystFloodFillLimitEnabled").setBoolean(null, false);
            floodFillClass.getField("$catalystFloodFillMaxIterations").setInt(null, 5000);
        } catch (Exception e) { /* ignore */ }
        try {
            Class<?> astarClass = Class.forName("com.hypixel.hytale.server.npc.navigation.AStarBase");
            astarClass.getField("$catalystPathfindingPoolEnabled").setBoolean(null, false);
            astarClass.getField("$catalystPathfindingPoolSize").setInt(null, 512);
        } catch (Exception e) { /* ignore */ }

        // Update advanced UI elements
        commandBuilder.set("#BlockEntitySleepCheckBox.Value", false);
        commandBuilder.set("#BlockEntitySleepIntervalSlider.Value", 20);
        commandBuilder.set("#StatRecalcThrottleCheckBox.Value", false);
        commandBuilder.set("#StatRecalcIntervalSlider.Value", 5);
        commandBuilder.set("#BlockUpdateBatchingCheckBox.Value", false);
        commandBuilder.set("#BlockUpdateBatchSizeSlider.Value", 64);
        commandBuilder.set("#FloodFillLimitCheckBox.Value", false);
        commandBuilder.set("#FloodFillMaxIterationsSlider.Value", 5000);
        commandBuilder.set("#PathfindingPoolCheckBox.Value", false);
        commandBuilder.set("#PathfindingPoolSizeSlider.Value", 512);
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
        static final String KEY_SWITCH_TAB_ADVANCED = "SwitchTabAdvanced";
        static final String KEY_TOGGLE_BLOCK_ENTITY_SLEEP = "ToggleBlockEntitySleep";
        static final String KEY_SET_BLOCK_ENTITY_SLEEP_INTERVAL = "@SetBlockEntitySleepInterval";
        static final String KEY_TOGGLE_STAT_RECALC_THROTTLE = "ToggleStatRecalcThrottle";
        static final String KEY_SET_STAT_RECALC_INTERVAL = "@SetStatRecalcInterval";
        static final String KEY_TOGGLE_BLOCK_UPDATE_BATCHING = "ToggleBlockUpdateBatching";
        static final String KEY_SET_BLOCK_UPDATE_BATCH_SIZE = "@SetBlockUpdateBatchSize";
        static final String KEY_TOGGLE_FLOOD_FILL_LIMIT = "ToggleFloodFillLimit";
        static final String KEY_SET_FLOOD_FILL_MAX_ITERATIONS = "@SetFloodFillMaxIterations";
        static final String KEY_TOGGLE_PATHFINDING_POOL = "TogglePathfindingPool";
        static final String KEY_SET_PATHFINDING_POOL_SIZE = "@SetPathfindingPoolSize";
        static final String KEY_SEARCH_QUERY = "@SearchQuery";

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
                .addField(new KeyedCodec<>(KEY_SWITCH_TAB_ADVANCED, Codec.STRING),
                    (guiData, s) -> guiData.switchTabAdvanced = s, guiData -> guiData.switchTabAdvanced)
                .addField(new KeyedCodec<>(KEY_TOGGLE_BLOCK_ENTITY_SLEEP, Codec.STRING),
                    (guiData, s) -> guiData.toggleBlockEntitySleep = s, guiData -> guiData.toggleBlockEntitySleep)
                .addField(new KeyedCodec<>(KEY_SET_BLOCK_ENTITY_SLEEP_INTERVAL, Codec.INTEGER),
                    (guiData, i) -> guiData.blockEntitySleepIntervalValue = i, guiData -> guiData.blockEntitySleepIntervalValue)
                .addField(new KeyedCodec<>(KEY_TOGGLE_STAT_RECALC_THROTTLE, Codec.STRING),
                    (guiData, s) -> guiData.toggleStatRecalcThrottle = s, guiData -> guiData.toggleStatRecalcThrottle)
                .addField(new KeyedCodec<>(KEY_SET_STAT_RECALC_INTERVAL, Codec.INTEGER),
                    (guiData, i) -> guiData.statRecalcIntervalValue = i, guiData -> guiData.statRecalcIntervalValue)
                .addField(new KeyedCodec<>(KEY_TOGGLE_BLOCK_UPDATE_BATCHING, Codec.STRING),
                    (guiData, s) -> guiData.toggleBlockUpdateBatching = s, guiData -> guiData.toggleBlockUpdateBatching)
                .addField(new KeyedCodec<>(KEY_SET_BLOCK_UPDATE_BATCH_SIZE, Codec.INTEGER),
                    (guiData, i) -> guiData.blockUpdateBatchSizeValue = i, guiData -> guiData.blockUpdateBatchSizeValue)
                .addField(new KeyedCodec<>(KEY_TOGGLE_FLOOD_FILL_LIMIT, Codec.STRING),
                    (guiData, s) -> guiData.toggleFloodFillLimit = s, guiData -> guiData.toggleFloodFillLimit)
                .addField(new KeyedCodec<>(KEY_SET_FLOOD_FILL_MAX_ITERATIONS, Codec.INTEGER),
                    (guiData, i) -> guiData.floodFillMaxIterationsValue = i, guiData -> guiData.floodFillMaxIterationsValue)
                .addField(new KeyedCodec<>(KEY_TOGGLE_PATHFINDING_POOL, Codec.STRING),
                    (guiData, s) -> guiData.togglePathfindingPool = s, guiData -> guiData.togglePathfindingPool)
                .addField(new KeyedCodec<>(KEY_SET_PATHFINDING_POOL_SIZE, Codec.INTEGER),
                    (guiData, i) -> guiData.pathfindingPoolSizeValue = i, guiData -> guiData.pathfindingPoolSizeValue)
                .addField(new KeyedCodec<>(KEY_SEARCH_QUERY, Codec.STRING),
                    (guiData, s) -> guiData.searchQuery = s, guiData -> guiData.searchQuery)
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
        private String switchTabAdvanced;
        private String toggleBlockEntitySleep;
        private Integer blockEntitySleepIntervalValue;
        private String toggleStatRecalcThrottle;
        private Integer statRecalcIntervalValue;
        private String toggleBlockUpdateBatching;
        private Integer blockUpdateBatchSizeValue;
        private String toggleFloodFillLimit;
        private Integer floodFillMaxIterationsValue;
        private String togglePathfindingPool;
        private Integer pathfindingPoolSizeValue;
        private String searchQuery;
    }
}
