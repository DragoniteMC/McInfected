package com.ericlam.mc.mcinfected.implement.mechanic;

import com.ericlam.mc.mcinfected.implement.McInfPlayer;
import com.ericlam.mc.mcinfected.manager.KitManager;
import com.ericlam.mc.minigames.core.character.GamePlayer;
import com.ericlam.mc.minigames.core.character.GamePlayerHandler;
import org.bukkit.entity.Player;

public class McInfPlayerMechanic implements GamePlayerHandler {

    @Override
    public void onPlayerStatusChange(GamePlayer gamePlayer, GamePlayer.Status status) {
        Player target = gamePlayer.getPlayer();
        KitManager.removeLastKit(target);
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
