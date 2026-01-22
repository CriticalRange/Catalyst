package com.criticalrange.command;

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
 * <p>Usage: /catalyst - Opens the settings GUI</p>
 */
public class CatalystCommand extends AbstractAsyncCommand {

    public CatalystCommand() {
        super("catalyst", "Open Catalyst settings menu");
    }

    @NonNullDecl
    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext context) {
        CommandSender sender = context.sender();
        if (!(sender instanceof Player player)) {
            context.sendMessage(Message.raw("You must be a player to use this command.").color("red"));
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
}
