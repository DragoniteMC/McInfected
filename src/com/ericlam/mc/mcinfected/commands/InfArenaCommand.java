package com.ericlam.mc.mcinfected.commands;

import com.hypernite.mc.hnmc.core.misc.commands.CommandNode;
import com.hypernite.mc.hnmc.core.misc.commands.DefaultCommand;
import com.hypernite.mc.hnmc.core.misc.permission.Perm;

public class InfArenaCommand extends DefaultCommand {

    public InfArenaCommand() {
        super(null, "inf-arena", Perm.OWNER, "場地創建指令", "infarena", "mcinf-arena");
    }
}
