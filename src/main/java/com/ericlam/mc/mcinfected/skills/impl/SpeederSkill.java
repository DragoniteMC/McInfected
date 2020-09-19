package com.ericlam.mc.mcinfected.skills.impl;

import com.ericlam.mc.mcinfected.skills.InfectedSkill;
import org.bukkit.entity.Player;

public class SpeederSkill implements InfectedSkill {

    @Override
    public void execute(Player self) {
        self.setWalkSpeed(self.getWalkSpeed() * 1.4f);
    }

    @Override
    public void revert(Player self) {
        self.setWalkSpeed(0.25f);
    }

    @Override
    public long getKeepingTime() {
        return 10;
    }

    @Override
    public long getCoolDown() {
        return 30;
    }
}
