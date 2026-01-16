package com.criticalrange.util;

import com.hypixel.hytale.server.core.Message;

/**
 * Generates the Catalyst settings menu Message.
 *
 * <p>Creates a formatted UI showing all optimization features and their status,
 * with clickable links to toggle them.</p>
 */
public class CatalystMenu {

    /**
     * Private constructor to prevent instantiation.
     */
    private CatalystMenu() {
    }

    /**
     * Generates the Catalyst settings menu Message.
     *
     * @return A Message containing the formatted menu
     */
    public static Message generateMenu() {
        return Message.raw("")
            .insert(Message.raw("\n╔══════════════════════════════════════════════════════╗\n").color("gold").bold(true))
            .insert(Message.raw("║             Catalyst Control Panel                   ║\n").color("gold").bold(true))
            .insert(Message.raw("╠══════════════════════════════════════════════════════╣\n").color("gold").bold(true))
            .insert(Message.raw("║ ").color("gold"))
            .insert(statusLine("Tick Opt", com.criticalrange.CatalystConfig.TICK_OPTIMIZATION_ENABLED, "toggle tick"))
            .insert(Message.raw("   "))
            .insert(statusLine("Tracking", com.criticalrange.CatalystConfig.ENTITY_TRACKING_ENABLED, "toggle tracking"))
            .insert(Message.raw(" ║\n").color("gold"))
            .insert(Message.raw("║ ").color("gold"))
            .insert(statusLine("Lighting", com.criticalrange.CatalystConfig.LIGHTING_OPTIMIZATION_ENABLED, "toggle lighting"))
            .insert(Message.raw("   "))
            .insert(statusLine("Movement", com.criticalrange.CatalystConfig.MOVEMENT_OPTIMIZATION_ENABLED, "toggle movement"))
            .insert(Message.raw(" ║\n").color("gold"))
            .insert(Message.raw("╠══════════════════════════════════════════════════════╣\n").color("gold").bold(true))
            .insert(Message.raw("║             Advanced Optimizations                   ║\n").color("yellow").bold(true))
            .insert(Message.raw("╠══════════════════════════════════════════════════════╣\n").color("gold").bold(true))
            .insert(Message.raw("║ ").color("gold"))
            .insert(statusLine("Physics", com.criticalrange.CatalystConfig.PHYSICS_OPTIMIZATION_ENABLED, "toggle physics"))
            .insert(Message.raw("    "))
            .insert(statusLine("AI Logic", com.criticalrange.CatalystConfig.AI_OPTIMIZATION_ENABLED, "toggle ai"))
            .insert(Message.raw(" ║\n").color("gold"))
            .insert(Message.raw("║ ").color("gold"))
            .insert(statusLine("Network", com.criticalrange.CatalystConfig.NETWORK_OPTIMIZATION_ENABLED, "toggle network"))
            .insert(Message.raw("   "))
            .insert(statusLine("ChunkGen", com.criticalrange.CatalystConfig.CHUNK_CACHE_ENABLED, "toggle chunks"))
            .insert(Message.raw(" ║\n").color("gold"))
            .insert(Message.raw("╠══════════════════════════════════════════════════════╣\n").color("gold").bold(true))
            .insert(Message.raw("║ Type ").color("gold"))
            .insert(Message.raw("/catalyst toggle <name>").color("aqua"))
            .insert(Message.raw(" to switch      ║\n").color("gold"))
            .insert(Message.raw("╚══════════════════════════════════════════════════════╝\n").color("gold").bold(true));
    }

    /**
     * Creates a status line with enabled/disabled indicator and clickable link.
     *
     * @param name The feature name
     * @param enabled Whether the feature is enabled
     * @param command The command to run when clicked
     * @return A Message containing the formatted status line
     */
    private static Message statusLine(String name, boolean enabled, String command) {
        String status = enabled ? "[ENABLED] " : "[DISABLED]";
        String color = enabled ? "green" : "red";

        // Try to make it clickable
        return Message.raw(name + ": ").color("gray")
                      .insert(Message.raw(status).color(color).link("/catalyst " + command));
    }
}
