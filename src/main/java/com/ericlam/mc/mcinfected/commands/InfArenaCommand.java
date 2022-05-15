package com.ericlam.mc.mcinfected.commands;

import com.dragonite.mc.dnmc.core.misc.commands.DefaultCommand;
import com.dragonite.mc.dnmc.core.misc.permission.Perm;

public class InfArenaCommand extends DefaultCommand {

    public InfArenaCommand() {
        super(null, "inf-arena", Perm.OWNER, "場地創建指令", "infarena", "mcinf-arena");
    }
}
