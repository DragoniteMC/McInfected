package com.ericlam.mc.mcinfected.skills;

import org.bukkit.entity.Player;

public interface InfectedSkill {

    void execute(Player self);

    void revert(Player self);

    long getKeepingTime();

    long getCoolDown();
}
