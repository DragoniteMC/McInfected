package com.ericlam.mc.mcinfected.implement.mechanic;

import com.ericlam.mc.mcinfected.implement.McInfPlayer;
import com.ericlam.mc.mcinfected.main.McInfected;
import com.ericlam.mc.mcinfected.tasks.VotingTask;
import com.ericlam.mc.minigames.core.character.GamePlayer;
import com.ericlam.mc.minigames.core.character.GamePlayerHandler;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class McInfPlayerMechanic implements GamePlayerHandler {

    @Override
    public void onPlayerStatusChange(GamePlayer gamePlayer, GamePlayer.Status status) {
        Player target = gamePlayer.getPlayer();
        McInfected.getApi().removePreviousKit(target, false);
        target.setGlowing(false);
        target.setGameMode(status == GamePlayer.Status.SPECTATING ? GameMode.SPECTATOR : GameMode.ADVENTURE);
        if (status == GamePlayer.Status.SPECTATING || status == GamePlayer.Status.GAMING) {
            VotingTask.addPlayer(gamePlayer);
        }
    }

    @Override
    public void onPlayerRemove(GamePlayer gamePlayer) {

    }

    @Override
    public GamePlayer createGamePlayer(Player player) {
        return new McInfPlayer(player);
    }

    @Override
    public int requireStart() {
        return 2;
    }
}
