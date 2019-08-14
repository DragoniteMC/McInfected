package com.ericlam.mc.mcinfected.tasks;

import com.ericlam.mc.mcinfected.main.McInfected;
import com.ericlam.mc.mcinfected.main.SoundUtils;
import com.ericlam.mc.minigames.core.main.MinigamesCore;
import com.ericlam.mc.minigames.core.manager.PlayerManager;
import org.bukkit.Bukkit;

public class InfectingTask extends InfTask {
    @Override
    public void initRun(PlayerManager playerManager) {
        playerManager.getTotalPlayers().forEach(playerManager::setGamePlayer);
        Bukkit.broadcastMessage(McInfected.config().getMessage("Game.Infecting"));
    }

    @Override
    public void onCancel() {
        GameEndTask.cancelGame(playerManager.getGamePlayer());
        Bukkit.broadcastMessage(McInfected.config().getMessage("Error.Game.Not_Enough_Players"));
    }

    @Override
    public void onFinish() {
        SoundUtils.playInfectSound(true);
    }

    @Override
    public long run(long l) {
        if (l % 30 == 0 || l == 20 || l <= 5) {
            String time = MinigamesCore.getApi().getGameUtils().getTimeWithUnit(l);
            Bukkit.broadcastMessage(McInfected.config().getMessage("Game.Infecting").replace("<time>", time));
            SoundUtils.playInfectSound(false);
        }
        return l;
    }

    @Override
    public long getTotalTime() {
        return McInfected.config().getData("infectingTime", Long.class).orElse(25L);
    }

    @Override
    public boolean shouldCancel() {
        return playerManager.getGamePlayer().size() < McInfected.config().getData("autoStart", Integer.class).orElse(2);
    }
}
