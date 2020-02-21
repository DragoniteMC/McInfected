package com.ericlam.mc.mcinfected.skills.impl;

import com.ericlam.mc.mcinfected.implement.team.HumanTeam;
import com.ericlam.mc.mcinfected.skills.InfectedSkill;
import com.ericlam.mc.minigames.core.character.GamePlayer;
import com.ericlam.mc.minigames.core.character.TeamPlayer;
import com.ericlam.mc.minigames.core.main.MinigamesCore;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Optional;

public class BlockerSkill implements InfectedSkill {

    @Override
    public void execute(Player self) {
        self.getLocation().getNearbyPlayers(10)
                .stream()
                .map(p -> MinigamesCore.getApi().getPlayerManager().findPlayer(p))
                .filter(Optional::isPresent).map(Optional::get)
                .filter(g -> g.castTo(TeamPlayer.class).getTeam() instanceof HumanTeam)
                .map(GamePlayer::getPlayer)
                .forEach(p -> {
                    p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 1, 0.9f);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 2));
                });
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
