package com.ericlam.mc.mcinfected.skills;

import com.ericlam.mc.mcinfected.implement.team.ZombieTeam;
import com.ericlam.mc.mcinfected.main.McInfected;
import com.ericlam.mc.minigames.core.character.TeamPlayer;
import com.ericlam.mc.minigames.core.event.state.InGameStateSwitchEvent;
import com.ericlam.mc.minigames.core.main.MinigamesCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class SkillListener implements Listener {
    private SkillManager skillManager;

    public SkillListener(SkillManager skillManager) {
        this.skillManager = skillManager;
    }

    @EventHandler
    public void onPlayerSwapHand(PlayerSwapHandItemsEvent e) {
        MinigamesCore.getApi().getPlayerManager().findPlayer(e.getPlayer()).ifPresent(g -> {
            if (MinigamesCore.getApi().getGameManager().getInGameState() == McInfected.getPlugin(McInfected.class).getGameEndState())
                return;
            e.setCancelled(true);
            if (g.castTo(TeamPlayer.class).getTeam() instanceof ZombieTeam) {
                skillManager.launchSkill(e.getPlayer());
            }
        });
    }

    @EventHandler
    public void onGameStateSwitch(InGameStateSwitchEvent e) {
        skillManager.clearSkill();
    }
}
