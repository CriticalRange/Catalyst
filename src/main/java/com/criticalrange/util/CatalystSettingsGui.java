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

        // Set checkbox values for lazy loading options
        uiCommandBuilder.set("#LazyToggle #CheckBox.Value", com.criticalrange.CatalystConfig.LAZY_BLOCK_ENTITIES_ENABLED);
        uiCommandBuilder.set("#TickLazyToggle #CheckBox.Value", com.criticalrange.CatalystConfig.LAZY_BLOCK_TICK_ENABLED);
        uiCommandBuilder.set("#FluidLazyToggle #CheckBox.Value", com.criticalrange.CatalystConfig.LAZY_FLUID_ENABLED);

        // Set checkbox values for runtime optimizations
        uiCommandBuilder.set("#EntityDistanceToggle #CheckBox.Value", com.criticalrange.CatalystConfig.ENTITY_DISTANCE_ENABLED);
        uiCommandBuilder.set("#ChunkRateToggle #CheckBox.Value", com.criticalrange.CatalystConfig.CHUNK_RATE_ENABLED);

        // Set slider values for runtime optimizations
        uiCommandBuilder.set("#EntityDistanceSlider.Value", com.criticalrange.CatalystConfig.ENTITY_VIEW_MULTIPLIER);
        uiCommandBuilder.set("#ChunkRateSlider.Value", com.criticalrange.CatalystConfig.CHUNKS_PER_TICK);

        // Set checkbox and slider values for pathfinding
        uiCommandBuilder.set("#PathfindingToggle #CheckBox.Value", com.criticalrange.CatalystConfig.PATHFINDING_ENABLED);
        uiCommandBuilder.set("#MaxPathLengthSlider.Value", com.criticalrange.CatalystConfig.MAX_PATH_LENGTH);
        uiCommandBuilder.set("#OpenNodesSlider.Value", com.criticalrange.CatalystConfig.OPEN_NODES_LIMIT);
        uiCommandBuilder.set("#TotalNodesSlider.Value", com.criticalrange.CatalystConfig.TOTAL_NODES_LIMIT);

        // Add event bindings for lazy loading
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#LazyToggle #CheckBox",
            EventData.of("ToggleLazy", "CAT"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#TickLazyToggle #CheckBox",
            EventData.of("ToggleTickLazy", "CAT"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#FluidLazyToggle #CheckBox",
            EventData.of("ToggleFluidLazy", "CAT"), false);

        // Add event bindings for runtime optimizations
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#EntityDistanceToggle #CheckBox",
            EventData.of("ToggleEntityDistance", "CAT"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#ChunkRateToggle #CheckBox",
            EventData.of("ToggleChunkRate", "CAT"), false);

        // Add event bindings for sliders
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#EntityDistanceSlider",
            EventData.of("SetEntityDistance", "CAT"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#ChunkRateSlider",
            EventData.of("SetChunkRate", "CAT"), false);

        // Add event bindings for pathfinding
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#PathfindingToggle #CheckBox",
            EventData.of("TogglePathfinding", "CAT"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#MaxPathLengthSlider",
            EventData.of("SetMaxPathLength", "CAT"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#OpenNodesSlider",
            EventData.of("SetOpenNodes", "CAT"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#TotalNodesSlider",
            EventData.of("SetTotalNodes", "CAT"), false);

        // Add event binding for reset button
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ResetButton",
            EventData.of("ResetToDefaults", "CAT"), false);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull GuiData data) {
        super.handleDataEvent(ref, store, data);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        boolean needUpdate = false;
        UICommandBuilder commandBuilder = new UICommandBuilder();
        UIEventBuilder eventBuilder = new UIEventBuilder();

        // Lazy loading toggles
        if (data.toggleLazy != null) {
            boolean newState = toggleLazyBlockEntities();
            commandBuilder.set("#LazyToggle #CheckBox.Value", newState);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Lazy Block Entities: " + (newState ? "ENABLED" : "DISABLED")));
            needUpdate = true;
        }

        if (data.toggleTickLazy != null) {
            boolean newState = toggleLazyBlockTick();
            commandBuilder.set("#TickLazyToggle #CheckBox.Value", newState);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Lazy Block Tick: " + (newState ? "ENABLED" : "DISABLED")));
            needUpdate = true;
        }

        if (data.toggleFluidLazy != null) {
            boolean newState = toggleLazyFluid();
            commandBuilder.set("#FluidLazyToggle #CheckBox.Value", newState);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Lazy Fluid: " + (newState ? "ENABLED" : "DISABLED")));
            needUpdate = true;
        }

        // Runtime optimization toggles
        if (data.toggleEntityDistance != null) {
            boolean newState = toggleEntityDistance();
            commandBuilder.set("#EntityDistanceToggle #CheckBox.Value", newState);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Entity View Distance: " + (newState ? "ENABLED" : "DISABLED")));
            needUpdate = true;
        }

        if (data.toggleChunkRate != null) {
            boolean newState = toggleChunkRate();
            commandBuilder.set("#ChunkRateToggle #CheckBox.Value", newState);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Chunk Rate Limit: " + (newState ? "ENABLED" : "DISABLED")));
            needUpdate = true;
        }

        // Slider value changes
        if (data.entityDistanceValue != null) {
            int newValue = (int) Math.round(data.entityDistanceValue);
            newValue = Math.max(8, Math.min(64, newValue));  // Clamp to valid range
            setEntityViewMultiplier(newValue);
            commandBuilder.set("#EntityDistanceSlider.Value", newValue);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Entity View Multiplier: " + newValue));
            needUpdate = true;
        }

        if (data.chunkRateValue != null) {
            int newValue = (int) Math.round(data.chunkRateValue);
            newValue = Math.max(1, Math.min(16, newValue));  // Clamp to valid range
            setChunksPerTick(newValue);
            commandBuilder.set("#ChunkRateSlider.Value", newValue);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Chunks Per Tick: " + newValue));
            needUpdate = true;
        }

        // Pathfinding toggle
        if (data.togglePathfinding != null) {
            boolean newState = togglePathfinding();
            commandBuilder.set("#PathfindingToggle #CheckBox.Value", newState);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Pathfinding Config: " + (newState ? "ENABLED" : "DISABLED")));
            needUpdate = true;
        }

        // Pathfinding slider values
        if (data.maxPathLengthValue != null) {
            int newValue = (int) Math.round(data.maxPathLengthValue);
            newValue = Math.max(50, Math.min(500, newValue));  // Clamp to valid range
            setMaxPathLength(newValue);
            commandBuilder.set("#MaxPathLengthSlider.Value", newValue);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Max Path Length: " + newValue));
            needUpdate = true;
        }

        if (data.openNodesValue != null) {
            int newValue = (int) Math.round(data.openNodesValue);
            newValue = Math.max(20, Math.min(200, newValue));  // Clamp to valid range
            setOpenNodesLimit(newValue);
            commandBuilder.set("#OpenNodesSlider.Value", newValue);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Open Nodes Limit: " + newValue));
            needUpdate = true;
        }

        if (data.totalNodesValue != null) {
            int newValue = (int) Math.round(data.totalNodesValue);
            newValue = Math.max(100, Math.min(1000, newValue));  // Clamp to valid range
            setTotalNodesLimit(newValue);
            commandBuilder.set("#TotalNodesSlider.Value", newValue);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "Total Nodes Limit: " + newValue));
            needUpdate = true;
        }

        // Reset to defaults button
        if (data.resetToDefaults != null) {
            resetToDefaults(commandBuilder);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw(
                "All settings reset to defaults!"));
            needUpdate = true;
        }

        if (needUpdate) {
            this.sendUpdate(commandBuilder, eventBuilder, false);
            // Auto-save config to file
            com.criticalrange.Catalyst.getInstance().saveConfig();
        }
    }

    // ===== Lazy Loading Toggles =====

    private boolean toggleLazyBlockEntities() {
        try {
            Class<?> blockModuleClass = Class.forName("com.hypixel.hytale.server.core.modules.block.BlockModule");
            java.lang.reflect.Field field = blockModuleClass.getField("$catalystLazyBlockEntities");
            boolean currentState = field.getBoolean(null);
            boolean newState = !currentState;
            field.setBoolean(null, newState);
            com.criticalrange.CatalystConfig.LAZY_BLOCK_ENTITIES_ENABLED = newState;
            return newState;
        } catch (Exception e) {
            System.err.println("[Catalyst] Failed to toggle lazy block entities: " + e.getMessage());
            return false;
        }
    }

    private boolean toggleLazyBlockTick() {
        try {
            Class<?> blockTickPluginClass = Class.forName("com.hypixel.hytale.builtin.blocktick.BlockTickPlugin");
            java.lang.reflect.Field field = blockTickPluginClass.getField("$catalystLazyBlockTick");
            boolean currentState = field.getBoolean(null);
            boolean newState = !currentState;
            field.setBoolean(null, newState);
            com.criticalrange.CatalystConfig.LAZY_BLOCK_TICK_ENABLED = newState;
            return newState;
        } catch (Exception e) {
            System.err.println("[Catalyst] Failed to toggle lazy block tick: " + e.getMessage());
            return false;
        }
    }

    private boolean toggleLazyFluid() {
        try {
            Class<?> fluidPluginClass = Class.forName("com.hypixel.hytale.builtin.fluid.FluidPlugin");
            java.lang.reflect.Field field = fluidPluginClass.getField("$catalystLazyFluid");
            boolean currentState = field.getBoolean(null);
            boolean newState = !currentState;
            field.setBoolean(null, newState);
            com.criticalrange.CatalystConfig.LAZY_FLUID_ENABLED = newState;
            return newState;
        } catch (Exception e) {
            System.err.println("[Catalyst] Failed to toggle lazy fluid: " + e.getMessage());
            return false;
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

    // ===== Reset to Defaults =====

    /**
     * Resets all Catalyst settings to their default values.
     */
    private void resetToDefaults(UICommandBuilder commandBuilder) {
        // Lazy loading defaults (all false)
        if (com.criticalrange.CatalystConfig.LAZY_BLOCK_ENTITIES_ENABLED) {
            toggleLazyBlockEntities();  // Toggle to false
        }
        if (com.criticalrange.CatalystConfig.LAZY_BLOCK_TICK_ENABLED) {
            toggleLazyBlockTick();  // Toggle to false
        }
        if (com.criticalrange.CatalystConfig.LAZY_FLUID_ENABLED) {
            toggleLazyFluid();  // Toggle to false
        }

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

        // Update all UI elements to reflect default values
        commandBuilder.set("#LazyToggle #CheckBox.Value", false);
        commandBuilder.set("#TickLazyToggle #CheckBox.Value", false);
        commandBuilder.set("#FluidLazyToggle #CheckBox.Value", false);
        commandBuilder.set("#EntityDistanceToggle #CheckBox.Value", false);
        commandBuilder.set("#EntityDistanceSlider.Value", 32);
        commandBuilder.set("#ChunkRateToggle #CheckBox.Value", false);
        commandBuilder.set("#ChunkRateSlider.Value", 4);
        commandBuilder.set("#PathfindingToggle #CheckBox.Value", false);
        commandBuilder.set("#MaxPathLengthSlider.Value", 200);
        commandBuilder.set("#OpenNodesSlider.Value", 80);
        commandBuilder.set("#TotalNodesSlider.Value", 400);
    }

    /**
     * GUI data for event handling.
     */
    public static class GuiData {
        static final String KEY_TOGGLE_LAZY = "ToggleLazy";
        static final String KEY_TOGGLE_TICK_LAZY = "ToggleTickLazy";
        static final String KEY_TOGGLE_FLUID_LAZY = "ToggleFluidLazy";
        static final String KEY_TOGGLE_ENTITY_DISTANCE = "ToggleEntityDistance";
        static final String KEY_TOGGLE_CHUNK_RATE = "ToggleChunkRate";
        static final String KEY_SET_ENTITY_DISTANCE = "SetEntityDistance";
        static final String KEY_SET_CHUNK_RATE = "SetChunkRate";
        static final String KEY_TOGGLE_PATHFINDING = "TogglePathfinding";
        static final String KEY_SET_MAX_PATH_LENGTH = "SetMaxPathLength";
        static final String KEY_SET_OPEN_NODES = "SetOpenNodes";
        static final String KEY_SET_TOTAL_NODES = "SetTotalNodes";
        static final String KEY_RESET_TO_DEFAULTS = "ResetToDefaults";

        public static final BuilderCodec<GuiData> CODEC = BuilderCodec.builder(GuiData.class, GuiData::new)
                .addField(new KeyedCodec<>(KEY_TOGGLE_LAZY, Codec.STRING),
                    (guiData, s) -> guiData.toggleLazy = s, guiData -> guiData.toggleLazy)
                .addField(new KeyedCodec<>(KEY_TOGGLE_TICK_LAZY, Codec.STRING),
                    (guiData, s) -> guiData.toggleTickLazy = s, guiData -> guiData.toggleTickLazy)
                .addField(new KeyedCodec<>(KEY_TOGGLE_FLUID_LAZY, Codec.STRING),
                    (guiData, s) -> guiData.toggleFluidLazy = s, guiData -> guiData.toggleFluidLazy)
                .addField(new KeyedCodec<>(KEY_TOGGLE_ENTITY_DISTANCE, Codec.STRING),
                    (guiData, s) -> guiData.toggleEntityDistance = s, guiData -> guiData.toggleEntityDistance)
                .addField(new KeyedCodec<>(KEY_TOGGLE_CHUNK_RATE, Codec.STRING),
                    (guiData, s) -> guiData.toggleChunkRate = s, guiData -> guiData.toggleChunkRate)
                .addField(new KeyedCodec<>(KEY_SET_ENTITY_DISTANCE, Codec.DOUBLE),
                    (guiData, d) -> guiData.entityDistanceValue = d, guiData -> guiData.entityDistanceValue)
                .addField(new KeyedCodec<>(KEY_SET_CHUNK_RATE, Codec.DOUBLE),
                    (guiData, d) -> guiData.chunkRateValue = d, guiData -> guiData.chunkRateValue)
                .addField(new KeyedCodec<>(KEY_TOGGLE_PATHFINDING, Codec.STRING),
                    (guiData, s) -> guiData.togglePathfinding = s, guiData -> guiData.togglePathfinding)
                .addField(new KeyedCodec<>(KEY_SET_MAX_PATH_LENGTH, Codec.DOUBLE),
                    (guiData, d) -> guiData.maxPathLengthValue = d, guiData -> guiData.maxPathLengthValue)
                .addField(new KeyedCodec<>(KEY_SET_OPEN_NODES, Codec.DOUBLE),
                    (guiData, d) -> guiData.openNodesValue = d, guiData -> guiData.openNodesValue)
                .addField(new KeyedCodec<>(KEY_SET_TOTAL_NODES, Codec.DOUBLE),
                    (guiData, d) -> guiData.totalNodesValue = d, guiData -> guiData.totalNodesValue)
                .addField(new KeyedCodec<>(KEY_RESET_TO_DEFAULTS, Codec.STRING),
                    (guiData, s) -> guiData.resetToDefaults = s, guiData -> guiData.resetToDefaults)
                .build();

        private String toggleLazy;
        private String toggleTickLazy;
        private String toggleFluidLazy;
        private String toggleEntityDistance;
        private String toggleChunkRate;
        private Double entityDistanceValue;
        private Double chunkRateValue;
        private String togglePathfinding;
        private Double maxPathLengthValue;
        private Double openNodesValue;
        private Double totalNodesValue;
        private String resetToDefaults;
    }
}
