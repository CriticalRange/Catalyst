package com.criticalrange.command;

import com.criticalrange.CatalystConfig;
import com.criticalrange.util.CatalystMetrics;
import com.criticalrange.util.OptimizationMetrics;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.Map;
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

                case "status":
                    showStatus(context);
                    return CompletableFuture.completedFuture(null);

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

                case "save":
                    saveConfig(context);
                    return CompletableFuture.completedFuture(null);

                case "reload":
                    reloadConfig(context);
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
        context.sendMessage(Message.raw("/catalyst status - Show optimization status"));
        context.sendMessage(Message.raw("/catalyst config - Show configuration"));
        context.sendMessage(Message.raw("/catalyst toggle <feature> - Toggle optimization"));
        context.sendMessage(Message.raw("/catalyst menu - Open settings GUI"));
        context.sendMessage(Message.raw("/catalyst save - Save configuration to file"));
        context.sendMessage(Message.raw("/catalyst reload - Reload configuration from file"));
    }

    private void showToggleHelp(CommandContext context) {
        context.sendMessage(Message.raw("Usage: /catalyst toggle <feature>").color("yellow"));
        context.sendMessage(Message.raw("Available features:"));
        context.sendMessage(Message.raw("  lazy - Lazy block entity initialization"));
        context.sendMessage(Message.raw("  ticklazy - Lazy block tick discovery"));
        context.sendMessage(Message.raw("  fluidlazy - Lazy fluid pre-processing"));
        context.sendMessage(Message.raw("  entitydist - Entity view distance optimization"));
        context.sendMessage(Message.raw("  chunkrate - Chunk loading rate optimization"));
        context.sendMessage(Message.raw("  pathfinding - NPC pathfinding limits"));
    }

    private void saveConfig(CommandContext context) {
        com.criticalrange.Catalyst.getInstance().saveConfig();
        context.sendMessage(Message.raw("Configuration saved to file.").color("green"));
    }

    private void reloadConfig(CommandContext context) {
        boolean success = com.criticalrange.Catalyst.getInstance().reloadConfig();
        if (success) {
            context.sendMessage(Message.raw("Configuration reloaded from file.").color("green"));
        } else {
            context.sendMessage(Message.raw("Failed to reload configuration (using defaults).").color("yellow"));
        }
    }

    private void showStatus(CommandContext context) {
        context.sendMessage(Message.raw("=== Catalyst Optimization Status ===").color("yellow"));
        
        Map<String, OptimizationMetrics.MetricData> metrics = OptimizationMetrics.getAllMetrics();
        
        context.sendMessage(Message.raw(""));
        context.sendMessage(Message.raw("-- Lazy Loading (Chunk Optimizations) --").color("gray"));
        
        context.sendMessage(statusLine("Lazy Block Entity", metrics.get("LazyBlockEntity")));
        context.sendMessage(statusLine("Lazy Block Tick", metrics.get("LazyBlockTick")));
        context.sendMessage(statusLine("Lazy Fluid", metrics.get("LazyFluid")));
        
        context.sendMessage(Message.raw(""));
        context.sendMessage(Message.raw("-- Runtime Optimizations --").color("gray"));
        
        // EntityDistance
        OptimizationMetrics.MetricData entityDist = metrics.get("EntityDistance");
        context.sendMessage(statusLine("Entity Distance", entityDist));
        if (entityDist.transformed && entityDist.enabled) {
            Object multiplier = entityDist.getValues().get("multiplier");
            if (multiplier != null) {
                context.sendMessage(Message.raw("    Multiplier: " + multiplier).color("gray"));
            }
        }
        
        // ChunkRate
        OptimizationMetrics.MetricData chunk = metrics.get("ChunkRate");
        context.sendMessage(statusLine("Chunk Rate", chunk));
        if (chunk.transformed) {
            Object cpt = chunk.getValues().get("chunksPerTick");
            context.sendMessage(Message.raw("    Chunks/Tick: " + cpt).color("gray"));
        }
        
        // Pathfinding
        OptimizationMetrics.MetricData pathfinding = metrics.get("Pathfinding");
        context.sendMessage(statusLine("Pathfinding", pathfinding));
        if (pathfinding.transformed && pathfinding.enabled) {
            context.sendMessage(Message.raw("    Max Path Length: " + pathfinding.getValues().get("maxPathLength")).color("gray"));
            context.sendMessage(Message.raw("    Open Nodes Limit: " + pathfinding.getValues().get("openNodesLimit")).color("gray"));
            context.sendMessage(Message.raw("    Total Nodes Limit: " + pathfinding.getValues().get("totalNodesLimit")).color("gray"));
        }
        
        context.sendMessage(Message.raw(""));
        context.sendMessage(Message.raw("Use '/catalyst menu' to toggle options.").color("yellow"));
    }
    
    private Message statusLine(String name, OptimizationMetrics.MetricData data) {
        String status = data.getStatus();
        String color;
        switch (status) {
            case "ACTIVE": color = "green"; break;
            case "DISABLED": color = "red"; break;
            default: color = "gray"; break;
        }
        return Message.raw("  " + name + ": " + status).color(color);
    }

    private void showConfig(CommandContext context) {
        context.sendMessage(Message.raw("=== Catalyst Configuration ===").color("yellow"));
        context.sendMessage(Message.raw("-- Lazy Loading (Chunk Optimization) --").color("gray"));
        context.sendMessage(configLine("Lazy Block Entities", CatalystConfig.LAZY_BLOCK_ENTITIES_ENABLED));
        context.sendMessage(configLine("Lazy Block Tick", CatalystConfig.LAZY_BLOCK_TICK_ENABLED));
        context.sendMessage(configLine("Lazy Fluid", CatalystConfig.LAZY_FLUID_ENABLED));
        context.sendMessage(Message.raw("-- Runtime Optimizations --").color("gray"));
        context.sendMessage(configLine("Entity Distance", CatalystConfig.ENTITY_DISTANCE_ENABLED));
        context.sendMessage(Message.raw("    Multiplier: " + CatalystConfig.ENTITY_VIEW_MULTIPLIER).color("gray"));
        context.sendMessage(configLine("Chunk Rate", CatalystConfig.CHUNK_RATE_ENABLED));
        context.sendMessage(Message.raw("    Chunks/Tick: " + CatalystConfig.CHUNKS_PER_TICK).color("gray"));
        context.sendMessage(Message.raw("-- NPC Pathfinding --").color("gray"));
        context.sendMessage(configLine("Pathfinding Config", CatalystConfig.PATHFINDING_ENABLED));
        context.sendMessage(Message.raw("    Max Path Length: " + CatalystConfig.MAX_PATH_LENGTH).color("gray"));
        context.sendMessage(Message.raw("    Open Nodes Limit: " + CatalystConfig.OPEN_NODES_LIMIT).color("gray"));
        context.sendMessage(Message.raw("    Total Nodes Limit: " + CatalystConfig.TOTAL_NODES_LIMIT).color("gray"));
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

            case "entitydist":
                newState = toggleEntityDistance();
                name = "Entity Distance";
                break;

            case "chunkrate":
                newState = toggleChunkRate();
                name = "Chunk Rate";
                break;

            case "pathfinding":
                newState = togglePathfinding();
                name = "Pathfinding Config";
                break;

            default:
                context.sendMessage(Message.raw("Unknown feature: " + feature).color("red"));
                showToggleHelp(context);
                return;
        }

        String stateStr = newState ? "ENABLED" : "DISABLED";
        String color = newState ? "green" : "red";
        context.sendMessage(Message.raw(name + " is now " + stateStr).color(color));

        // Auto-save config to file
        com.criticalrange.Catalyst.getInstance().saveConfig();
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

    /**
     * Toggles the entity distance flag in Universe using reflection.
     */
    private boolean toggleEntityDistance() {
        try {
            Class<?> universeClass = Class.forName("com.hypixel.hytale.server.core.universe.Universe");
            java.lang.reflect.Field field = universeClass.getField("$catalystEntityDistEnabled");
            boolean currentState = field.getBoolean(null);
            boolean newState = !currentState;
            field.setBoolean(null, newState);
            CatalystConfig.ENTITY_DISTANCE_ENABLED = newState;
            return newState;
        } catch (Exception e) {
            System.err.println("[Catalyst] Failed to toggle entity distance: " + e.getMessage());
            CatalystConfig.ENTITY_DISTANCE_ENABLED = !CatalystConfig.ENTITY_DISTANCE_ENABLED;
            return CatalystConfig.ENTITY_DISTANCE_ENABLED;
        }
    }

    /**
     * Toggles the chunk rate flag in ChunkTracker using reflection.
     */
    private boolean toggleChunkRate() {
        try {
            Class<?> chunkTrackerClass = Class.forName("com.hypixel.hytale.server.core.modules.entity.player.ChunkTracker");
            java.lang.reflect.Field field = chunkTrackerClass.getField("$catalystChunkRateEnabled");
            boolean currentState = field.getBoolean(null);
            boolean newState = !currentState;
            field.setBoolean(null, newState);
            CatalystConfig.CHUNK_RATE_ENABLED = newState;
            return newState;
        } catch (Exception e) {
            System.err.println("[Catalyst] Failed to toggle chunk rate: " + e.getMessage());
            CatalystConfig.CHUNK_RATE_ENABLED = !CatalystConfig.CHUNK_RATE_ENABLED;
            return CatalystConfig.CHUNK_RATE_ENABLED;
        }
    }

    /**
     * Toggles the pathfinding config flag in AStarBase using reflection.
     */
    private boolean togglePathfinding() {
        try {
            Class<?> astarClass = Class.forName("com.hypixel.hytale.server.npc.navigation.AStarBase");
            java.lang.reflect.Field field = astarClass.getField("$catalystPathfindingEnabled");
            boolean currentState = field.getBoolean(null);
            boolean newState = !currentState;
            field.setBoolean(null, newState);
            CatalystConfig.PATHFINDING_ENABLED = newState;
            return newState;
        } catch (Exception e) {
            System.err.println("[Catalyst] Failed to toggle pathfinding: " + e.getMessage());
            CatalystConfig.PATHFINDING_ENABLED = !CatalystConfig.PATHFINDING_ENABLED;
            return CatalystConfig.PATHFINDING_ENABLED;
        }
    }
}
