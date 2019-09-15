package com.ericlam.mc.mcinfected.skills;

import com.ericlam.mc.mcinfected.api.McInfectedAPI;
import com.ericlam.mc.mcinfected.config.InfConfig;
import com.ericlam.mc.mcinfected.implement.team.HumanTeam;
import com.ericlam.mc.mcinfected.implement.team.ZombieTeam;
import com.ericlam.mc.mcinfected.main.McInfected;
import com.ericlam.mc.mcinfected.tasks.GameTask;
import com.ericlam.mc.minigames.core.character.GamePlayer;
import com.ericlam.mc.minigames.core.character.TeamPlayer;
import com.ericlam.mc.minigames.core.event.player.CrackShotDeathEvent;
import com.ericlam.mc.minigames.core.event.player.GamePlayerDeathEvent;
import com.ericlam.mc.minigames.core.event.state.InGameStateSwitchEvent;
import com.ericlam.mc.minigames.core.main.MinigamesCore;
import com.ericlam.mc.minigames.core.manager.GameUtils;
import me.DeeCaaD.CrackShotPlus.API;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import java.util.Optional;

public class SkillListener implements Listener {
    private SkillManager skillManager;
    private InfConfig infConfig;
    private GameUtils gameUtils;

    public SkillListener(SkillManager skillManager, InfConfig infConfig) {
        this.skillManager = skillManager;
        this.infConfig = infConfig;
        gameUtils = MinigamesCore.getApi().getGameUtils();
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
                if (GameTask.shouldHunterActivate(MinigamesCore.getApi().getPlayerManager().getGamePlayer())) {
                    String hunterKit = infConfig.defaultKit.get("hunter");
                    String using = McInfected.getApi().currentKit(g.getPlayer());
                    if (using != null && using.equals(hunterKit)) return;
                    McInfected.getApi().gainKit(g.getPlayer(), hunterKit);
                    gameUtils.playSound(e.getPlayer(), infConfig.soundHunter.get("Burn").split(":"));
                    e.getPlayer().sendTitle("", "§b已化身成幽靈獵手。", 0, 30, 0);
                    e.getPlayer().setGlowing(true);
                    Player player = e.getPlayer();
                    Optional.ofNullable(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).ifPresent(a -> {
                        a.setBaseValue(2048);
                        player.setHealth(a.getBaseValue());
                        player.setHealthScale(20);
                    });
                }
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
        if (e.getKiller() == null) return;
        GamePlayer killer = e.getKiller();
        if (!(killer.castTo(TeamPlayer.class).getTeam() instanceof HumanTeam)) return;
        String using = api.currentKit(killer.getPlayer());
        if (using == null) return;
        String hunterKit = infConfig.defaultKit.get("Hunter");
        if (!using.equals(hunterKit)) return;
        boolean melee = false;
        if (e instanceof CrackShotDeathEvent) {
            CrackShotDeathEvent cs = (CrackShotDeathEvent) e;
            melee = API.getCSDirector().getBoolean(cs.getWeaponTitle() + ".Item_Information.Melee_Mode");
        }
        if (melee) {
            Bukkit.getOnlinePlayers().forEach(p -> gameUtils.playSound(p, infConfig.soundHunter.get("Kill").split(":")));
        }


    }
}
