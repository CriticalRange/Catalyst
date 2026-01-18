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
 * <p>Usage:</p>
 * <ul>
 *   <li>/catalyst - Show performance report</li>
 *   <li>/catalyst menu - Open settings GUI</li>
 *   <li>/catalyst toggle [feature] - Toggle lazy loading optimization</li>
 *   <li>/catalyst config - Show current configuration</li>
 * </ul>
 */
public class CatalystCommand extends AbstractAsyncCommand {

    public CatalystCommand() {
        super("catalyst", "View Catalyst performance metrics and toggle optimizations");
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
        context.sendMessage(Message.raw("=== Catalyst Commands ===").color("yellow"));
        context.sendMessage(Message.raw("/catalyst - Show performance report"));
        context.sendMessage(Message.raw("/catalyst config - Show configuration"));
        context.sendMessage(Message.raw("/catalyst toggle <feature> - Toggle lazy loading"));
        context.sendMessage(Message.raw("/catalyst menu - Open settings GUI"));
    }

    private void showToggleHelp(CommandContext context) {
        context.sendMessage(Message.raw("Usage: /catalyst toggle <feature>").color("yellow"));
        context.sendMessage(Message.raw("Available features:"));
        context.sendMessage(Message.raw("  lazy - Lazy block entity initialization"));
        context.sendMessage(Message.raw("  ticklazy - Lazy block tick discovery"));
        context.sendMessage(Message.raw("  fluidlazy - Lazy fluid pre-processing"));
    }

    private void showConfig(CommandContext context) {
        context.sendMessage(Message.raw("=== Catalyst Configuration ===").color("yellow"));
        context.sendMessage(Message.raw("-- Lazy Loading (Chunk Optimization) --").color("gray"));
        context.sendMessage(configLine("Lazy Block Entities", CatalystConfig.LAZY_BLOCK_ENTITIES_ENABLED));
        context.sendMessage(configLine("Lazy Block Tick", CatalystConfig.LAZY_BLOCK_TICK_ENABLED));
        context.sendMessage(configLine("Lazy Fluid", CatalystConfig.LAZY_FLUID_ENABLED));
    }

    private Message configLine(String name, boolean enabled) {
        String status = enabled ? "ENABLED" : "DISABLED";
        String color = enabled ? "green" : "red";
        return Message.raw("  " + name + ": " + status).color(color);
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
            case "lazy":
                newState = toggleLazyBlockEntities();
                name = "Lazy Block Entities";
                break;

            case "ticklazy":
                newState = toggleLazyBlockTick();
                name = "Lazy Block Tick";
                break;

            case "fluidlazy":
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
     */
    private boolean toggleLazyBlockEntities() {
        try {
            Class<?> blockModuleClass = Class.forName("com.hypixel.hytale.server.core.modules.block.BlockModule");
            java.lang.reflect.Field field = blockModuleClass.getField("$catalystLazyBlockEntities");
            boolean currentState = field.getBoolean(null);
            boolean newState = !currentState;
            field.setBoolean(null, newState);
            CatalystConfig.LAZY_BLOCK_ENTITIES_ENABLED = newState;
            return newState;
        } catch (Exception e) {
            System.err.println("[Catalyst] Failed to toggle lazy block entities: " + e.getMessage());
            return false;
        }
    }

    /**
     * Toggles the lazy block tick flag in BlockTickPlugin using reflection.
     */
    private boolean toggleLazyBlockTick() {
        try {
            Class<?> blockTickPluginClass = Class.forName("com.hypixel.hytale.builtin.blocktick.BlockTickPlugin");
            java.lang.reflect.Field field = blockTickPluginClass.getField("$catalystLazyBlockTick");
            boolean currentState = field.getBoolean(null);
            boolean newState = !currentState;
            field.setBoolean(null, newState);
            CatalystConfig.LAZY_BLOCK_TICK_ENABLED = newState;
            return newState;
        } catch (Exception e) {
            System.err.println("[Catalyst] Failed to toggle lazy block tick: " + e.getMessage());
            return false;
        }
    }

    /**
     * Toggles the lazy fluid flag in FluidPlugin using reflection.
     */
    private boolean toggleLazyFluid() {
        try {
            Class<?> fluidPluginClass = Class.forName("com.hypixel.hytale.builtin.fluid.FluidPlugin");
            java.lang.reflect.Field field = fluidPluginClass.getField("$catalystLazyFluid");
            boolean currentState = field.getBoolean(null);
            boolean newState = !currentState;
            field.setBoolean(null, newState);
            CatalystConfig.LAZY_FLUID_ENABLED = newState;
            return newState;
        } catch (Exception e) {
            System.err.println("[Catalyst] Failed to toggle lazy fluid: " + e.getMessage());
            return false;
        }
    }
}
