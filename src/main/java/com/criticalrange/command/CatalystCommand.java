package com.criticalrange.command;

import com.criticalrange.util.CatalystMetrics;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.command.system.CommandContext;

import javax.annotation.Nonnull;

public class CatalystCommand extends CommandBase {

    public CatalystCommand() {
        super("catalyst", "View Catalyst performance metrics");
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        // Generate the report
        String report = CatalystMetrics.generateReport();
        
        // Send as a formatted message
        context.sendMessage(Message.raw(report));
    }
}
