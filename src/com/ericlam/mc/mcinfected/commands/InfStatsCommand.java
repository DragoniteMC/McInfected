package com.ericlam.mc.mcinfected.commands;

import com.hypernite.mc.hnmc.core.misc.commands.CommandNode;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import java.util.List;

public class InfStatsCommand extends CommandNode {

    public InfStatsCommand() {
        super(null, "inf-stats", null, "查看戰績", "[player]" ,"infstats");
    }

    @Override
    public boolean executeCommand(@Nonnull CommandSender commandSender, @Nonnull List<String> list) {

        return false;
    }

    @Override
    public List<String> executeTabCompletion(@Nonnull CommandSender commandSender, @Nonnull List<String> list) {
        return null;
    }
}
