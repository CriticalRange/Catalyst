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
            .insert(Message.raw("\n-- Lazy Loading (Chunk Optimization) --\n").color("yellow"))
            .insert(statusLine("Lazy Block Entities", com.criticalrange.CatalystConfig.LAZY_BLOCK_ENTITIES_ENABLED, "toggle lazy"))
            .insert(Message.raw("\n"))
            .insert(statusLine("Lazy Block Tick", com.criticalrange.CatalystConfig.LAZY_BLOCK_TICK_ENABLED, "toggle ticklazy"))
            .insert(Message.raw("\n"))
            .insert(statusLine("Lazy Fluid", com.criticalrange.CatalystConfig.LAZY_FLUID_ENABLED, "toggle fluidlazy"))
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
