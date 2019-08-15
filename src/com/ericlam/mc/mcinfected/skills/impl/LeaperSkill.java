package com.ericlam.mc.mcinfected.skills.impl;

import com.ericlam.mc.mcinfected.skills.InfectedSkill;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class LeaperSkill implements InfectedSkill {

    @Override
    public void execute(Player self) {
        self.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 200, 3));
    }

    @Override
    public void revert(Player self) {
        self.removePotionEffect(PotionEffectType.JUMP);
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
