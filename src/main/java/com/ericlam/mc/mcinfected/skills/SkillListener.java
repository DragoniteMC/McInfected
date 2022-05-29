package com.ericlam.mc.mcinfected.skills;

import com.ericlam.mc.mcinfected.api.McInfectedAPI;
import com.ericlam.mc.mcinfected.config.InfConfig;
import com.ericlam.mc.mcinfected.implement.team.HumanTeam;
import com.ericlam.mc.mcinfected.implement.team.ZombieTeam;
import com.ericlam.mc.mcinfected.main.McInfected;
import com.ericlam.mc.mcinfected.manager.HunterManager;
import com.ericlam.mc.minigames.core.character.GamePlayer;
import com.ericlam.mc.minigames.core.character.TeamPlayer;
import com.ericlam.mc.minigames.core.event.player.CrackShotDeathEvent;
import com.ericlam.mc.minigames.core.event.player.GamePlayerDeathEvent;
import com.ericlam.mc.minigames.core.event.state.InGameStateSwitchEvent;
import com.ericlam.mc.minigames.core.main.MinigamesCore;
import com.ericlam.mc.minigames.core.manager.GameUtils;
import me.DeeCaaD.CrackShotPlus.API;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class SkillListener implements Listener {
    private final SkillManager skillManager;
    private final InfConfig infConfig;
    private final GameUtils gameUtils;
    private final HunterManager hunterManager;


    public SkillListener(SkillManager skillManager, InfConfig infConfig, HunterManager hunterManager) {
        this.skillManager = skillManager;
        this.infConfig = infConfig;
        gameUtils = MinigamesCore.getApi().getGameUtils();
        this.hunterManager = hunterManager;
    }

    @EventHandler
    public void onPlayerSwapHand(PlayerSwapHandItemsEvent e) {
        MinigamesCore.getApi().getPlayerManager().findPlayer(e.getPlayer()).ifPresent(g -> {
            if (MinigamesCore.getApi().getGameManager().getInGameState() == McInfected.getPlugin(McInfected.class).getGameEndState())
                return;
            e.setCancelled(true);
            if (g.getStatus() != GamePlayer.Status.GAMING) return;
            if (g.castTo(TeamPlayer.class).getTeam() instanceof ZombieTeam) {
                skillManager.launchSkill(e.getPlayer());
            } else if (g.castTo(TeamPlayer.class).getTeam() instanceof HumanTeam) {
                hunterManager.activateHunter(e.getPlayer());
            }
        });
    }

    @EventHandler
    public void onGameStateSwitch(InGameStateSwitchEvent e) {
        skillManager.clearSkill();
    }

    @EventHandler
    public void onGamePlayerDeath(GamePlayerDeathEvent e) {
        McInfectedAPI api = McInfected.getApi();
        if (e.getKiller() == null) {
            Bukkit.getLogger().info("killer is null");
            return;
        }
        GamePlayer killer = e.getKiller();
        if (!(killer.castTo(TeamPlayer.class).getTeam() instanceof HumanTeam)) {
            Bukkit.getLogger().info("killer is not human");
            return;
        }
        if (!hunterManager.shouldHunterActive()) return;
        boolean melee = false;
        if (e instanceof CrackShotDeathEvent cs) {
            melee = API.getCSDirector().getBoolean(cs.getWeaponTitle() + ".Item_Information.Melee_Mode");
        }
        if (melee) {
            Bukkit.getOnlinePlayers().forEach(p -> gameUtils.playSound(p, infConfig.sounds.hunter.get("Kill").split(":")));
        }else{
            Bukkit.getLogger().info("killer using weapon is not melee");
        }
    }
}
