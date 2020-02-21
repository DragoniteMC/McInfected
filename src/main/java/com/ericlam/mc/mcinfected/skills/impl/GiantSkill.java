package com.ericlam.mc.mcinfected.skills.impl;

import com.ericlam.mc.csweapon.CustomCSWeapon;
import com.ericlam.mc.mcinfected.skills.InfectedSkill;
import org.bukkit.entity.Player;

public class GiantSkill implements InfectedSkill {

    @Override
    public void execute(Player self) {
        CustomCSWeapon.getApi().getKnockBackManager().setCustomKnockBack(self, false);
    }

    @Override
    public void revert(Player self) {
        CustomCSWeapon.getApi().getKnockBackManager().setCustomKnockBack(self, true);
    }

    @Override
    public long getKeepingTime() {
        return 10;
    }

    @Override
    public long getCoolDown() {
        return 45;
    }
}
