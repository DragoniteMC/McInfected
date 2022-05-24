package com.ericlam.mc.mcinfected.skills.impl;

import com.ericlam.mc.mcinfected.implement.team.ZombieTeam;
import com.ericlam.mc.mcinfected.skills.InfectedSkill;
import com.ericlam.mc.minigames.core.character.GamePlayer;
import com.ericlam.mc.minigames.core.character.TeamPlayer;
import com.ericlam.mc.minigames.core.main.MinigamesCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class ChemWitchSkill implements InfectedSkill {

    @Override
    public void execute(Player self) {
        List<GamePlayer> zombies = MinigamesCore.getApi().getPlayerManager().getGamePlayer().stream().filter(g -> g.castTo(TeamPlayer.class).getTeam() instanceof ZombieTeam).collect(Collectors.toList());
        zombies.forEach(g -> {
            Player player = g.getPlayer();
            Title.Times time = Title.Times.times(Duration.ofSeconds(0), Duration.ofSeconds(2), Duration.ofSeconds(0));
            Title t = Title.title(Component.empty(), Component.text("§e全體殭屍速度 +20%, 持續 10 秒"), time);
            player.showTitle(t);
            player.setWalkSpeed(player.getWalkSpeed() * 1.2f);
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 2, 0.9f);
        });
    }

    @Override
    public void revert(Player self) {
        List<GamePlayer> zombies = MinigamesCore.getApi().getPlayerManager().getGamePlayer().stream().filter(g -> g.castTo(TeamPlayer.class).getTeam() instanceof ZombieTeam).collect(Collectors.toList());
        zombies.forEach(g -> g.getPlayer().setWalkSpeed(g.getPlayer().getWalkSpeed() * 0.83333f));
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
