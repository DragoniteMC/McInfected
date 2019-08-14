package com.ericlam.mc.mcinfected.tasks;

import com.ericlam.mc.mcinfected.main.McInfected;
import com.ericlam.mc.mcinfected.main.SoundUtils;
import com.ericlam.mc.minigames.core.arena.Arena;
import com.ericlam.mc.minigames.core.main.MinigamesCore;
import com.ericlam.mc.minigames.core.manager.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class VotingTask extends InfTask {

    private boolean loaded = false;

    @Override
    public void initRun(PlayerManager playerManager) {
        playerManager.getWaitingPlayer().forEach(p -> {
            Player player = p.getPlayer();
            player.sendMessage(McInfected.config().getMessage("Game.Start"));
            player.sendTitle("", McInfected.config().getPureMessage("Game.Start-Title"), 20, 60, 20);
        });
    }

    @Override
    public void onCancel() {
        Bukkit.broadcastMessage(McInfected.config().getMessage("Error.Command.countdown-cancel"));
    }

    @Override
    public void onFinish() {
    }

    @Override
    public long run(long l) {
        if (l % 30 == 0 || l == 20 || (l < 10 && l > 5)) {
            String time = MinigamesCore.getApi().getGameUtils().getTimeWithUnit(l - 5);
            Bukkit.broadcastMessage(McInfected.config().getMessage("Game.Time.Voting").replace("<time>", time));
            SoundUtils.playVoteSound(false);
        } else if (l == 5) {
            SoundUtils.playVoteSound(true);
            MinigamesCore.getApi().getLobbyManager().runFinalResult();
            loaded = true;
            Arena arena = MinigamesCore.getApi().getArenaManager().getFinalArena();
            Bukkit.broadcastMessage(McInfected.config().getMessage("Game.Arena_Selected").replace("<arena>", arena.getDisplayName()));
            Bukkit.broadcastMessage(McInfected.config().getMessage("Game.Time.PreGame"));
        }
        return l;
    }

    @Override
    public long getTotalTime() {
        return McInfected.config().getData("votingTime", Long.class).orElse(30L);
    }

    @Override
    public boolean shouldCancel() {
        return playerManager.getWaitingPlayer().size() < McInfected.config().getData("autoStart", Integer.class).orElse(2) && !loaded;
    }
}
