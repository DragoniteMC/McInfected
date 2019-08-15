package com.ericlam.mc.mcinfected.tasks;

import com.ericlam.mc.minigames.core.function.CircularIterator;
import com.ericlam.mc.minigames.core.game.GameState;
import com.ericlam.mc.minigames.core.main.MinigamesCore;
import com.ericlam.mc.minigames.core.manager.PlayerManager;
import org.bukkit.boss.BarColor;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class PreEndTask extends InfTask {

    private int change = 0;

    @Override
    public void initRun(PlayerManager playerManager) {

        CircularIterator<BarColor> colors = new CircularIterator<>(List.of(BarColor.GREEN, BarColor.RED, BarColor.YELLOW, BarColor.PINK, BarColor.PURPLE));
        new BukkitRunnable() {
            @Override
            public void run() {
                if (change >= 10) {
                    cancel();
                }
                VotingTask.bossBar.setColor(colors.next());
                playerManager.getGamePlayer().forEach(p -> {
                    MinigamesCore.getApi().getFireWorkManager().spawnFireWork(p.getPlayer());
                    p.getPlayer().getInventory().clear();
                });
                change++;
            }
        }.runTaskTimer(mcinf, 0L, 10L);
        VotingTask.updateBoard(0, playerManager.getGamePlayer(), "&a遊戲完結");
    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onFinish() {
        MinigamesCore.getApi().getGameManager().setState(GameState.ENDED);
    }

    @Override
    public long run(long l) {
        return l;
    }

    @Override
    public long getTotalTime() {
        return 10;
    }

    @Override
    public boolean shouldCancel() {
        return false;
    }
}
