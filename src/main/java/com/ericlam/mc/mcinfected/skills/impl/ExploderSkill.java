package com.ericlam.mc.mcinfected.skills.impl;

import com.ericlam.mc.mcinfected.skills.InfectedSkill;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ExploderSkill implements InfectedSkill {

    @Override
    public void execute(Player self) {
        Location location = self.getLocation();
        location.createExplosion(self, 8.0f, false, false);
    }

    @Override
    public void revert(Player self) {

    }

    @Override
    public long getKeepingTime() {
        return 1;
    }

    @Override
    public long getCoolDown() {
        return 30;
    }
}
