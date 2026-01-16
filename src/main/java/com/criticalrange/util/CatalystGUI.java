package com.criticalrange.util;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * GUI utilities for Catalyst mod settings interface.
 *
 * <p>Provides methods to open the Catalyst settings GUI for players.</p>
 */
public class CatalystGUI {

    /**
     * Private constructor to prevent instantiation.
     */
    private CatalystGUI() {
    }

    /**
     * Opens the Catalyst settings GUI for a player from a command context.
     *
     * @param context The command context containing the player
     */
    public static void open(CommandContext context) {
        try {
            // Check if the sender is a player
            if (!context.isPlayer()) {
                context.sendMessage(Message.raw("You must be a player to use the GUI.").color("red"));
                return;
            }

            // Get the player object from the command context
            Player player = context.senderAs(Player.class);

            if (player == null) {
                context.sendMessage(Message.raw("Failed to get player object.").color("red"));
                return;
            }

            Ref<EntityStore> ref = player.getReference();
            if (ref == null || !ref.isValid()) {
                context.sendMessage(Message.raw("Player reference is invalid.").color("red"));
                return;
            }

            Store<EntityStore> store = ref.getStore();
            PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());

            if (playerRef == null) {
                context.sendMessage(Message.raw("Failed to get player reference component.").color("red"));
                return;
            }

            // Open the custom GUI page
            openPage(ref, store, playerRef);

            System.out.println("[Catalyst] Opened settings GUI for player: " + player.getDisplayName());

        } catch (Exception e) {
            context.sendMessage(Message.raw("ERROR: " + e.getClass().getSimpleName() + ": " + e.getMessage()).color("red"));
            System.err.println("[Catalyst] Error opening GUI:");
            e.printStackTrace();
        }
    }

    /**
     * Opens the Catalyst settings GUI page for a player.
     *
     * @param ref The entity reference
     * @param store The entity store
     * @param playerRef The player reference component
     */
    public static void openPage(Ref<EntityStore> ref, Store<EntityStore> store, PlayerRef playerRef) {
        try {
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null) {
                System.err.println("[Catalyst] Failed to get player from store");
                return;
            }

            player.getPageManager().openCustomPage(ref, store, new CatalystSettingsGui(playerRef, CustomPageLifetime.CanDismiss));
            System.out.println("[Catalyst] Opened settings GUI for player: " + player.getDisplayName());

        } catch (Exception e) {
            System.err.println("[Catalyst] Error opening page:");
            e.printStackTrace();
        }
    }
}
