package com.criticalrange.util;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentAccessor;
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

public class CatalystSettingsGui extends InteractiveCustomUIPage<CatalystSettingsGui.GuiData> {

    public CatalystSettingsGui(@Nonnull PlayerRef playerRef, @Nonnull CustomPageLifetime lifetime) {
        super(playerRef, lifetime, GuiData.CODEC);
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        // Load the base UI file
        uiCommandBuilder.append("Pages/CriticalRange_Catalyst_Settings.ui");

        // Set checkbox values
        uiCommandBuilder.set("#TickToggle #CheckBox.Value", com.criticalrange.CatalystConfig.TICK_OPTIMIZATION_ENABLED);
        uiCommandBuilder.set("#PhysicsToggle #CheckBox.Value", com.criticalrange.CatalystConfig.PHYSICS_OPTIMIZATION_ENABLED);
        uiCommandBuilder.set("#AIToggle #CheckBox.Value", com.criticalrange.CatalystConfig.AI_OPTIMIZATION_ENABLED);
        uiCommandBuilder.set("#NetworkToggle #CheckBox.Value", com.criticalrange.CatalystConfig.NETWORK_OPTIMIZATION_ENABLED);
        uiCommandBuilder.set("#LightingToggle #CheckBox.Value", com.criticalrange.CatalystConfig.LIGHTING_OPTIMIZATION_ENABLED);
        uiCommandBuilder.set("#MovementToggle #CheckBox.Value", com.criticalrange.CatalystConfig.MOVEMENT_OPTIMIZATION_ENABLED);
        uiCommandBuilder.set("#LazyToggle #CheckBox.Value", com.criticalrange.CatalystConfig.LAZY_BLOCK_ENTITIES_ENABLED);
        uiCommandBuilder.set("#TickLazyToggle #CheckBox.Value", com.criticalrange.CatalystConfig.LAZY_BLOCK_TICK_ENABLED);
        uiCommandBuilder.set("#FluidLazyToggle #CheckBox.Value", com.criticalrange.CatalystConfig.LAZY_FLUID_ENABLED);

        // Add event bindings
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#TickToggle #CheckBox", EventData.of("ToggleTick", "CAT"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#PhysicsToggle #CheckBox", EventData.of("TogglePhysics", "CAT"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#AIToggle #CheckBox", EventData.of("ToggleAI", "CAT"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#NetworkToggle #CheckBox", EventData.of("ToggleNetwork", "CAT"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#LightingToggle #CheckBox", EventData.of("ToggleLighting", "CAT"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#MovementToggle #CheckBox", EventData.of("ToggleMovement", "CAT"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#LazyToggle #CheckBox", EventData.of("ToggleLazy", "CAT"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#TickLazyToggle #CheckBox", EventData.of("ToggleTickLazy", "CAT"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#FluidLazyToggle #CheckBox", EventData.of("ToggleFluidLazy", "CAT"), false);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull GuiData data) {
        super.handleDataEvent(ref, store, data);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        boolean needUpdate = false;
        UICommandBuilder commandBuilder = new UICommandBuilder();
        UIEventBuilder eventBuilder = new UIEventBuilder();

        if (data.toggleTick != null) {
            com.criticalrange.CatalystConfig.TICK_OPTIMIZATION_ENABLED = !com.criticalrange.CatalystConfig.TICK_OPTIMIZATION_ENABLED;
            commandBuilder.set("#TickToggle #CheckBox.Value", com.criticalrange.CatalystConfig.TICK_OPTIMIZATION_ENABLED);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw("Tick Optimization: " + (com.criticalrange.CatalystConfig.TICK_OPTIMIZATION_ENABLED ? "ENABLED" : "DISABLED")));
            needUpdate = true;
        }

        if (data.togglePhysics != null) {
            com.criticalrange.CatalystConfig.PHYSICS_OPTIMIZATION_ENABLED = !com.criticalrange.CatalystConfig.PHYSICS_OPTIMIZATION_ENABLED;
            commandBuilder.set("#PhysicsToggle #CheckBox.Value", com.criticalrange.CatalystConfig.PHYSICS_OPTIMIZATION_ENABLED);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw("Physics Optimization: " + (com.criticalrange.CatalystConfig.PHYSICS_OPTIMIZATION_ENABLED ? "ENABLED" : "DISABLED")));
            needUpdate = true;
        }

        if (data.toggleAI != null) {
            com.criticalrange.CatalystConfig.AI_OPTIMIZATION_ENABLED = !com.criticalrange.CatalystConfig.AI_OPTIMIZATION_ENABLED;
            commandBuilder.set("#AIToggle #CheckBox.Value", com.criticalrange.CatalystConfig.AI_OPTIMIZATION_ENABLED);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw("AI Optimization: " + (com.criticalrange.CatalystConfig.AI_OPTIMIZATION_ENABLED ? "ENABLED" : "DISABLED")));
            needUpdate = true;
        }

        if (data.toggleNetwork != null) {
            com.criticalrange.CatalystConfig.NETWORK_OPTIMIZATION_ENABLED = !com.criticalrange.CatalystConfig.NETWORK_OPTIMIZATION_ENABLED;
            commandBuilder.set("#NetworkToggle #CheckBox.Value", com.criticalrange.CatalystConfig.NETWORK_OPTIMIZATION_ENABLED);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw("Network Optimization: " + (com.criticalrange.CatalystConfig.NETWORK_OPTIMIZATION_ENABLED ? "ENABLED" : "DISABLED")));
            needUpdate = true;
        }

        if (data.toggleLighting != null) {
            com.criticalrange.CatalystConfig.LIGHTING_OPTIMIZATION_ENABLED = !com.criticalrange.CatalystConfig.LIGHTING_OPTIMIZATION_ENABLED;
            commandBuilder.set("#LightingToggle #CheckBox.Value", com.criticalrange.CatalystConfig.LIGHTING_OPTIMIZATION_ENABLED);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw("Lighting Optimization: " + (com.criticalrange.CatalystConfig.LIGHTING_OPTIMIZATION_ENABLED ? "ENABLED" : "DISABLED")));
            needUpdate = true;
        }

        if (data.toggleMovement != null) {
            com.criticalrange.CatalystConfig.MOVEMENT_OPTIMIZATION_ENABLED = !com.criticalrange.CatalystConfig.MOVEMENT_OPTIMIZATION_ENABLED;
            commandBuilder.set("#MovementToggle #CheckBox.Value", com.criticalrange.CatalystConfig.MOVEMENT_OPTIMIZATION_ENABLED);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw("Movement Optimization: " + (com.criticalrange.CatalystConfig.MOVEMENT_OPTIMIZATION_ENABLED ? "ENABLED" : "DISABLED")));
            needUpdate = true;
        }

        if (data.toggleLazy != null) {
            boolean newState = toggleLazyBlockEntities();
            commandBuilder.set("#LazyToggle #CheckBox.Value", newState);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw("Lazy Block Entities: " + (newState ? "ENABLED" : "DISABLED")));
            needUpdate = true;
        }

        if (data.toggleTickLazy != null) {
            boolean newState = toggleLazyBlockTick();
            commandBuilder.set("#TickLazyToggle #CheckBox.Value", newState);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw("Lazy Block Tick: " + (newState ? "ENABLED" : "DISABLED")));
            needUpdate = true;
        }

        if (data.toggleFluidLazy != null) {
            boolean newState = toggleLazyFluid();
            commandBuilder.set("#FluidLazyToggle #CheckBox.Value", newState);
            player.sendMessage(com.hypixel.hytale.server.core.Message.raw("Lazy Fluid: " + (newState ? "ENABLED" : "DISABLED")));
            needUpdate = true;
        }

        if (needUpdate) {
            this.sendUpdate(commandBuilder, eventBuilder, false);
        }
    }

    /**
     * Toggles the lazy block entities flag in BlockModule using reflection.
     *
     * @return The new state (true if enabled, false if disabled)
     */
    private boolean toggleLazyBlockEntities() {
        try {
            Class<?> blockModuleClass = Class.forName("com.hypixel.hytale.server.core.modules.block.BlockModule");
            java.lang.reflect.Field field = blockModuleClass.getField("$catalystLazyBlockEntities");
            boolean currentState = field.getBoolean(null);
            boolean newState = !currentState;
            field.setBoolean(null, newState);
            com.criticalrange.CatalystConfig.LAZY_BLOCK_ENTITIES_ENABLED = newState;  // Keep config in sync for GUI
            return newState;
        } catch (Exception e) {
            System.err.println("[Catalyst] Failed to toggle lazy block entities: " + e.getMessage());
            return false;
        }
    }

    /**
     * Toggles the lazy block tick flag in BlockTickPlugin using reflection.
     *
     * @return The new state (true if enabled, false if disabled)
     */
    private boolean toggleLazyBlockTick() {
        try {
            Class<?> blockTickPluginClass = Class.forName("com.hypixel.hytale.builtin.blocktick.BlockTickPlugin");
            java.lang.reflect.Field field = blockTickPluginClass.getField("$catalystLazyBlockTick");
            boolean currentState = field.getBoolean(null);
            boolean newState = !currentState;
            field.setBoolean(null, newState);
            com.criticalrange.CatalystConfig.LAZY_BLOCK_TICK_ENABLED = newState;  // Keep config in sync for GUI
            return newState;
        } catch (Exception e) {
            System.err.println("[Catalyst] Failed to toggle lazy block tick: " + e.getMessage());
            return false;
        }
    }

    /**
     * Toggles the lazy fluid flag in FluidPlugin using reflection.
     *
     * @return The new state (true if enabled, false if disabled)
     */
    private boolean toggleLazyFluid() {
        try {
            Class<?> fluidPluginClass = Class.forName("com.hypixel.hytale.builtin.fluid.FluidPlugin");
            java.lang.reflect.Field field = fluidPluginClass.getField("$catalystLazyFluid");
            boolean currentState = field.getBoolean(null);
            boolean newState = !currentState;
            field.setBoolean(null, newState);
            com.criticalrange.CatalystConfig.LAZY_FLUID_ENABLED = newState;  // Keep config in sync for GUI
            return newState;
        } catch (Exception e) {
            System.err.println("[Catalyst] Failed to toggle lazy fluid: " + e.getMessage());
            return false;
        }
    }

    public static class GuiData {
        static final String KEY_TOGGLE_TICK = "ToggleTick";
        static final String KEY_TOGGLE_PHYSICS = "TogglePhysics";
        static final String KEY_TOGGLE_AI = "ToggleAI";
        static final String KEY_TOGGLE_NETWORK = "ToggleNetwork";
        static final String KEY_TOGGLE_LIGHTING = "ToggleLighting";
        static final String KEY_TOGGLE_MOVEMENT = "ToggleMovement";
        static final String KEY_TOGGLE_LAZY = "ToggleLazy";
        static final String KEY_TOGGLE_TICK_LAZY = "ToggleTickLazy";
        static final String KEY_TOGGLE_FLUID_LAZY = "ToggleFluidLazy";

        public static final BuilderCodec<GuiData> CODEC = BuilderCodec.builder(GuiData.class, GuiData::new)
                .addField(new KeyedCodec<>(KEY_TOGGLE_TICK, Codec.STRING), (guiData, s) -> guiData.toggleTick = s, guiData -> guiData.toggleTick)
                .addField(new KeyedCodec<>(KEY_TOGGLE_PHYSICS, Codec.STRING), (guiData, s) -> guiData.togglePhysics = s, guiData -> guiData.togglePhysics)
                .addField(new KeyedCodec<>(KEY_TOGGLE_AI, Codec.STRING), (guiData, s) -> guiData.toggleAI = s, guiData -> guiData.toggleAI)
                .addField(new KeyedCodec<>(KEY_TOGGLE_NETWORK, Codec.STRING), (guiData, s) -> guiData.toggleNetwork = s, guiData -> guiData.toggleNetwork)
                .addField(new KeyedCodec<>(KEY_TOGGLE_LIGHTING, Codec.STRING), (guiData, s) -> guiData.toggleLighting = s, guiData -> guiData.toggleLighting)
                .addField(new KeyedCodec<>(KEY_TOGGLE_MOVEMENT, Codec.STRING), (guiData, s) -> guiData.toggleMovement = s, guiData -> guiData.toggleMovement)
                .addField(new KeyedCodec<>(KEY_TOGGLE_LAZY, Codec.STRING), (guiData, s) -> guiData.toggleLazy = s, guiData -> guiData.toggleLazy)
                .addField(new KeyedCodec<>(KEY_TOGGLE_TICK_LAZY, Codec.STRING), (guiData, s) -> guiData.toggleTickLazy = s, guiData -> guiData.toggleTickLazy)
                .addField(new KeyedCodec<>(KEY_TOGGLE_FLUID_LAZY, Codec.STRING), (guiData, s) -> guiData.toggleFluidLazy = s, guiData -> guiData.toggleFluidLazy)
                .build();

        private String toggleTick;
        private String togglePhysics;
        private String toggleAI;
        private String toggleNetwork;
        private String toggleLighting;
        private String toggleMovement;
        private String toggleLazy;
        private String toggleTickLazy;
        private String toggleFluidLazy;
    }
}
