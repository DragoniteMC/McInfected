package com.ericlam.mc.mcinfected.tasks;

import com.ericlam.mc.mcinfected.implement.McInfPlayer;
import com.ericlam.mc.mcinfected.implement.team.HumanTeam;
import com.ericlam.mc.mcinfected.implement.team.ZombieTeam;
import com.ericlam.mc.mcinfected.main.McInfected;
import com.ericlam.mc.mcinfected.main.SoundUtils;
import com.ericlam.mc.minigames.core.character.GamePlayer;
import com.ericlam.mc.minigames.core.character.TeamPlayer;
import com.ericlam.mc.minigames.core.main.MinigamesCore;
import com.ericlam.mc.minigames.core.manager.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class GameTask extends InfTask {

    @Override
    public void initRun(PlayerManager playerManager) {
        float percent = McInfected.config().getData("alphaPercent", Float.class).orElse(0.25F);
        int alphas = (int) Math.ceil(playerManager.getGamePlayer().size() * percent);
        Random random = new Random();
        int alphasZombie = 0;
        for (GamePlayer g : playerManager.getGamePlayer()) {
            if (alphas <= alphasZombie) break;
            if (random.nextBoolean()) {
                TeamPlayer player = g.castTo(TeamPlayer.class);
                player.setTeam(mcinf.getZombieTeam());
                Bukkit.broadcastMessage(McInfected.config().getMessage("Game.Infected").replace("<player>", player.getPlayer().getDisplayName()));
                alphasZombie++;
            }
        }
        playerManager.getGamePlayer().forEach(p -> McInfected.kitManager().gainKit(p.castTo(McInfPlayer.class)));

    }

    @Override
    public void onCancel() {
        boolean normalEnd = playerManager.getGamePlayer().stream().noneMatch(g -> g.castTo(TeamPlayer.class).getTeam() instanceof HumanTeam);
        if (normalEnd) {
            this.onFinish();
        } else {
            GameEndTask.cancelGame(playerManager.getGamePlayer());
            Bukkit.broadcastMessage(McInfected.config().getMessage("Error.Game.Not_Enough_Players"));
        }
    }

    @Override
    public void onFinish() {
        SoundUtils.playGameSound(true);
    }

    @Override
    public long run(long l) {
        if (l % 30 == 0 || l == 10 || l <= 5) {
            String time = MinigamesCore.getApi().getGameUtils().getTimeWithUnit(l);
            Bukkit.broadcastMessage(McInfected.config().getMessage("Game.Time.Game").replace("<time>", time));
            SoundUtils.playGameSound(false);
        }
        if (l == McInfected.config().getData("compassTime", Long.class).orElse(65L)) {
            playerManager.getGamePlayer()
                    .stream().filter(g -> g.castTo(TeamPlayer.class).getTeam() instanceof ZombieTeam)
                    .forEach(p -> {
                        p.getPlayer().getInventory().addItem(new ItemStack(Material.COMPASS));
                        McInfected.config().getData("compassGain", String[].class).ifPresent(sound -> MinigamesCore.getApi().getGameUtils().playSound(p.getPlayer(), sound));
                    });
        }
        return l;
    }

    @Override
    public long getTotalTime() {
        return McInfected.config().getData("gameTime", Long.class).orElse(150L);
    }

    @Override
    public boolean shouldCancel() {
        boolean noone = playerManager.getGamePlayer().size() < McInfected.config().getData("autoStart", Integer.class).orElse(2);
        boolean normalEnd = playerManager.getGamePlayer().stream().noneMatch(g -> g.castTo(TeamPlayer.class).getTeam() instanceof HumanTeam);
        return noone || normalEnd;
    }
}
