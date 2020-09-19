package com.ericlam.mc.mcinfected.manager;

import com.ericlam.mc.mcinfected.config.InfConfig;
import com.ericlam.mc.mcinfected.config.LangConfig;
import com.ericlam.mc.mcinfected.implement.team.HumanTeam;
import com.ericlam.mc.mcinfected.implement.team.ZombieTeam;
import com.ericlam.mc.mcinfected.main.McInfected;
import com.ericlam.mc.mcinfected.tasks.BossbarUpdateRunnable;
import com.ericlam.mc.minigames.core.character.GamePlayer;
import com.ericlam.mc.minigames.core.character.TeamPlayer;
import com.ericlam.mc.minigames.core.main.MinigamesCore;
import com.ericlam.mc.minigames.core.manager.PlayerManager;
import com.hypernite.mc.hnmc.core.managers.YamlManager;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class HunterManager {

    private static BossBar hunterBossBar;
    private final PlayerManager playerManager;
    private final InfConfig infConfig;
    private final LangConfig msg;
    private boolean notified;

    public HunterManager(YamlManager yamlManager) {
        this.infConfig = yamlManager.getConfigAs(InfConfig.class);
        this.msg = yamlManager.getConfigAs(LangConfig.class);
        this.playerManager = MinigamesCore.getApi().getPlayerManager();
    }

    public static void addPlayer(GamePlayer player) {
        if (hunterBossBar != null) {
            hunterBossBar.addPlayer(player.getPlayer());
        }
    }

    public void setBarVisible(boolean visible) {
        if (hunterBossBar != null) {
            hunterBossBar.setVisible(visible);
            if (!visible) notified = false;
        }
    }

    public void initializeBar() {
        hunterBossBar = Bukkit.createBossBar(msg.getPure("Picture.Bar.Hunter"), BarColor.WHITE, BarStyle.SOLID);
        hunterBossBar.setProgress(0.5);
        hunterBossBar.setVisible(false);
    }

    public boolean shouldHunterActive() {
        if (notified) return false;
        var gamePlayers = playerManager.getGamePlayer();
        if (gamePlayers.size() < 1) return false;
        float hunterPercent = infConfig.game.hunterPercent;
        List<GamePlayer> humans = gamePlayers.stream().filter(g -> g.castTo(TeamPlayer.class).getTeam() instanceof HumanTeam).collect(Collectors.toList());
        int hunterSize = (int) Math.floor(gamePlayers.size() * hunterPercent);
        return hunterSize >= humans.size();
    }

    public void notifyHunters() {
        playerManager.getGamePlayer().stream().filter(g -> g.castTo(TeamPlayer.class).getTeam() instanceof HumanTeam).collect(Collectors.toList()).forEach(g -> {
            Player player = g.getPlayer();
            player.setGlowing(true);
            MinigamesCore.getApi().getGameUtils().playSound(player, infConfig.sounds.hunter.get("Active").split(":"));
            player.sendTitle("", "§a按 F 可以化身成幽靈獵手。", 0, 100, 0);
        });
        this.notified = true;
    }

    public boolean isNotified() {
        return notified;
    }

    public void updateHunterBossBar() {
        var gamePlayers = playerManager.getGamePlayer();
        long humans = gamePlayers.stream().filter(g -> g.castTo(TeamPlayer.class).getTeam() instanceof HumanTeam).count();
        long zombies = gamePlayers.stream().filter(g -> g.castTo(TeamPlayer.class).getTeam() instanceof ZombieTeam).count();
        String tit = McInfected.getApi().getConfigManager().getConfigAs(LangConfig.class).getPure("Picture.Bar.Hunter");
        hunterBossBar.setTitle(tit.replace("<h>", "§k0").replace("<z>", "§k0"));
        hunterBossBar.setColor(BarColor.RED);
        new BossbarUpdateRunnable(hunterBossBar, humans, zombies).runTaskLater(McInfected.getPlugin(McInfected.class), 3L);
    }

    public void activateHunter(Player player) {
        if (!shouldHunterActive()) return;
        playerManager.findPlayer(player).ifPresent(g -> {
            String hunterKit = infConfig.defaultKit.get("hunter");
            String using = McInfected.getApi().currentKit(g.getPlayer());
            if (using != null && using.equals(hunterKit)) return;
            McInfected.getApi().gainKit(g.getPlayer(), hunterKit);
            MinigamesCore.getApi().getGameUtils().playSound(player, infConfig.sounds.hunter.get("Burn").split(":"));
            player.sendTitle("", "§b已化身成幽靈獵手。", 0, 30, 0);
            player.setGlowing(true);
            Optional.ofNullable(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).ifPresent(a -> {
                a.setBaseValue(2048);
                player.setHealth(a.getBaseValue());
                player.setHealthScale(20);
            });
        });
    }

}
