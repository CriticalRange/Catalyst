package com.criticalrange.command;

import com.criticalrange.CatalystConfig;
import com.criticalrange.util.CatalystMetrics;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.concurrent.CompletableFuture;

/**
 * Main command for Catalyst mod.
 * 
 * Usage:
 *   /catalyst - Show performance report
 *   /catalyst menu - Open settings GUI
 *   /catalyst toggle [feature] - Toggle a feature on/off
 *   /catalyst reset - Reset all statistics
 *   /catalyst tps - Show current TPS
 *   /catalyst config - Show current configuration
 */
public class CatalystCommand extends AbstractAsyncCommand {

    /**
     * Creates a new Catalyst command.
     */
    public CatalystCommand() {
        super("catalyst", "View Catalyst performance metrics");
        this.setAllowsExtraArguments(true);
    }

    @NonNullDecl
    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext context) {
        String[] args = context.getInputString().split(" ");

        if (args.length > 1) {
            String subCmd = args[1].toLowerCase();

            switch (subCmd) {
                case "menu":
                    return openMenuAsync(context);
                    
                case "toggle":
                    if (args.length > 2) {
                        handleToggle(context, args[2].toLowerCase());
                    } else {
                        showToggleHelp(context);
                    }
                    return CompletableFuture.completedFuture(null);
                    
                case "reset":
                    CatalystMetrics.reset();
                    context.sendMessage(Message.raw("Statistics reset successfully.").color("green"));
                    return CompletableFuture.completedFuture(null);
                    
                case "tps":
                    double tps = CatalystMetrics.getCurrentTPS();
                    String tpsColor = tps > 19 ? "green" : (tps > 17 ? "yellow" : "red");
                    context.sendMessage(Message.raw(String.format("Current TPS: %.2f", tps)).color(tpsColor));
                    context.sendMessage(Message.raw(String.format("Total ticks: %d", CatalystMetrics.getTickCount())));
                    return CompletableFuture.completedFuture(null);
                    
                case "config":
                    showConfig(context);
                    return CompletableFuture.completedFuture(null);
                    
                case "help":
                    showHelp(context);
                    return CompletableFuture.completedFuture(null);
                    
                default:
                    context.sendMessage(Message.raw("Unknown subcommand: " + subCmd).color("red"));
                    showHelp(context);
                    return CompletableFuture.completedFuture(null);
            }
        }

        // Default: Generate the report
        String report = CatalystMetrics.generateReport();
        context.sendMessage(Message.raw(report));
        context.sendMessage(Message.raw("\nType '/catalyst help' for available commands.").color("yellow"));
        return CompletableFuture.completedFuture(null);
    }

    private void showHelp(CommandContext context) {
        context.sendMessage(Message.raw("§e=== Catalyst Commands ==="));
        context.sendMessage(Message.raw("§f/catalyst §7- Show performance report"));
        context.sendMessage(Message.raw("§f/catalyst tps §7- Show current TPS"));
        context.sendMessage(Message.raw("§f/catalyst config §7- Show configuration"));
        context.sendMessage(Message.raw("§f/catalyst toggle <feature> §7- Toggle optimization"));
        context.sendMessage(Message.raw("§f/catalyst reset §7- Reset statistics"));
        context.sendMessage(Message.raw("§f/catalyst menu §7- Open settings GUI"));
    }

    private void showToggleHelp(CommandContext context) {
        context.sendMessage(Message.raw("§eUsage: /catalyst toggle <feature>"));
        context.sendMessage(Message.raw("§fAvailable features:"));
        context.sendMessage(Message.raw("  §7tick, tracking, movement, lighting, physics, ai, network, lazy, ticklazy, fluidlazy"));
    }

    private void showConfig(CommandContext context) {
        context.sendMessage(Message.raw("§e=== Catalyst Configuration ==="));
        context.sendMessage(configLine("Tick Optimization", CatalystConfig.TICK_OPTIMIZATION_ENABLED));
        context.sendMessage(configLine("Entity Tracking", CatalystConfig.ENTITY_TRACKING_ENABLED));
        context.sendMessage(configLine("Chunk Cache", CatalystConfig.CHUNK_CACHE_ENABLED));
        context.sendMessage(configLine("Lighting Optimization", CatalystConfig.LIGHTING_OPTIMIZATION_ENABLED));
        context.sendMessage(configLine("Movement Optimization", CatalystConfig.MOVEMENT_OPTIMIZATION_ENABLED));
        context.sendMessage(configLine("Network Optimization", CatalystConfig.NETWORK_OPTIMIZATION_ENABLED));
        context.sendMessage(configLine("Physics Optimization", CatalystConfig.PHYSICS_OPTIMIZATION_ENABLED));
        context.sendMessage(configLine("AI Optimization", CatalystConfig.AI_OPTIMIZATION_ENABLED));
        context.sendMessage(configLine("Lazy Block Entities", CatalystConfig.LAZY_BLOCK_ENTITIES_ENABLED));
        context.sendMessage(configLine("Lazy Block Tick", CatalystConfig.LAZY_BLOCK_TICK_ENABLED));
        context.sendMessage(configLine("Lazy Fluid", CatalystConfig.LAZY_FLUID_ENABLED));
    }

    private Message configLine(String name, boolean enabled) {
        String status = enabled ? "§aENABLED" : "§cDISABLED";
        return Message.raw("  §f" + name + ": " + status);
    }

    private CompletableFuture<Void> openMenuAsync(CommandContext context) {
        CommandSender sender = context.sender();
        if (!(sender instanceof Player player)) {
            context.sendMessage(Message.raw("You must be a player to use the GUI.").color("red"));
            return CompletableFuture.completedFuture(null);
        }

        player.getWorldMapTracker().tick(0);

        var ref = player.getReference();
        if (ref == null || !ref.isValid()) {
            context.sendMessage(Message.raw("Player reference is invalid.").color("red"));
            return CompletableFuture.completedFuture(null);
        }

        var store = ref.getStore();
        World world = store.getExternalData().getWorld();

        if (world == null) {
            context.sendMessage(Message.raw("Failed to get world.").color("red"));
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
            if (playerRef != null) {
                com.criticalrange.util.CatalystGUI.openPage(ref, store, playerRef);
            }
        }, world);
    }

    private void handleToggle(CommandContext context, String feature) {
        boolean newState = false;
        String name = "";

        switch (feature) {
            case "tick":
                CatalystConfig.TICK_OPTIMIZATION_ENABLED = !CatalystConfig.TICK_OPTIMIZATION_ENABLED;
                newState = CatalystConfig.TICK_OPTIMIZATION_ENABLED;
                name = "Tick Optimization";
                break;
            case "tracking":
                CatalystConfig.ENTITY_TRACKING_ENABLED = !CatalystConfig.ENTITY_TRACKING_ENABLED;
                newState = CatalystConfig.ENTITY_TRACKING_ENABLED;
                name = "Entity Tracking";
                break;
            case "movement":
                CatalystConfig.MOVEMENT_OPTIMIZATION_ENABLED = !CatalystConfig.MOVEMENT_OPTIMIZATION_ENABLED;
                newState = CatalystConfig.MOVEMENT_OPTIMIZATION_ENABLED;
                name = "Movement Optimization";
                break;
            case "lighting":
                CatalystConfig.LIGHTING_OPTIMIZATION_ENABLED = !CatalystConfig.LIGHTING_OPTIMIZATION_ENABLED;
                newState = CatalystConfig.LIGHTING_OPTIMIZATION_ENABLED;
                name = "Lighting Optimization";
                break;
            case "physics":
                CatalystConfig.PHYSICS_OPTIMIZATION_ENABLED = !CatalystConfig.PHYSICS_OPTIMIZATION_ENABLED;
                newState = CatalystConfig.PHYSICS_OPTIMIZATION_ENABLED;
                name = "Physics Optimization";
                break;
            case "ai":
                CatalystConfig.AI_OPTIMIZATION_ENABLED = !CatalystConfig.AI_OPTIMIZATION_ENABLED;
                newState = CatalystConfig.AI_OPTIMIZATION_ENABLED;
                name = "AI Optimization";
                break;
            case "network":
                CatalystConfig.NETWORK_OPTIMIZATION_ENABLED = !CatalystConfig.NETWORK_OPTIMIZATION_ENABLED;
                newState = CatalystConfig.NETWORK_OPTIMIZATION_ENABLED;
                name = "Network Optimization";
                break;
            case "chunk":
                CatalystConfig.CHUNK_CACHE_ENABLED = !CatalystConfig.CHUNK_CACHE_ENABLED;
                newState = CatalystConfig.CHUNK_CACHE_ENABLED;
                name = "Chunk Cache";
                break;
            case "lazy":
                // Toggle lazy block entities using reflection
                newState = toggleLazyBlockEntities();
                name = "Lazy Block Entities";
                break;
            case "ticklazy":
                // Toggle lazy block tick discovery using reflection
                newState = toggleLazyBlockTick();
                name = "Lazy Block Tick";
                break;
            case "fluidlazy":
                // Toggle lazy fluid processing using reflection
                newState = toggleLazyFluid();
                name = "Lazy Fluid";
                break;
            default:
                context.sendMessage(Message.raw("Unknown feature: " + feature).color("red"));
                showToggleHelp(context);
                return;
        }

        String stateStr = newState ? "ENABLED" : "DISABLED";
        String color = newState ? "green" : "red";
        context.sendMessage(Message.raw(name + " is now " + stateStr).color(color));
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
            CatalystConfig.LAZY_BLOCK_ENTITIES_ENABLED = newState;  // Keep config in sync for GUI
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
            CatalystConfig.LAZY_BLOCK_TICK_ENABLED = newState;  // Keep config in sync for GUI
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
            CatalystConfig.LAZY_FLUID_ENABLED = newState;  // Keep config in sync for GUI
            return newState;
        } catch (Exception e) {
            System.err.println("[Catalyst] Failed to toggle lazy fluid: " + e.getMessage());
            return false;
        }
    }
}
