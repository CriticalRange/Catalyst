package com.criticalrange.util;

import com.hypixel.hytale.server.core.Message;

/**
 * Generates the Catalyst settings menu Message.
 *
 * <p>Creates a formatted UI showing all optimization features and their status,
 * with clickable links to toggle them.</p>
 */
public class CatalystMenu {

    private CatalystMenu() {
    }

    /**
     * Generates the Catalyst settings menu Message.
     *
     * @return A Message containing the formatted menu
     */
    public static Message generateMenu() {
        return Message.raw("")
            .insert(Message.raw("\n============================================\n").color("gold").bold(true))
            .insert(Message.raw("         Catalyst Control Panel              \n").color("gold").bold(true))
            .insert(Message.raw("============================================\n").color("gold").bold(true))
            .insert(Message.raw("\n-- Runtime Optimizations --\n").color("yellow"))
            .insert(statusLine("Entity Distance", com.criticalrange.CatalystConfig.ENTITY_DISTANCE_ENABLED, "toggle entitydist"))
            .insert(Message.raw("\n"))
            .insert(statusLine("Chunk Rate", com.criticalrange.CatalystConfig.CHUNK_RATE_ENABLED, "toggle chunkrate"))
            .insert(Message.raw("\n\n============================================\n").color("gold").bold(true))
            .insert(Message.raw("Type ").color("gray"))
            .insert(Message.raw("/catalyst toggle <name>").color("aqua"))
            .insert(Message.raw(" to switch\n").color("gray"))
            .insert(Message.raw("============================================\n").color("gold").bold(true));
    }

    /**
     * Creates a status line with enabled/disabled indicator.
     */
    private static Message statusLine(String name, boolean enabled, String command) {
        String status = enabled ? "[ENABLED]" : "[DISABLED]";
        String color = enabled ? "green" : "red";

        return Message.raw("  " + name + ": ").color("gray")
                      .insert(Message.raw(status).color(color).link("/catalyst " + command));
    }
}
