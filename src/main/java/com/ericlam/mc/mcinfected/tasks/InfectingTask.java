package com.ericlam.mc.mcinfected.tasks;

import com.dragonite.mc.dnmc.core.main.DragoniteMC;
import com.ericlam.mc.csweapon.CustomCSWeapon;
import com.ericlam.mc.mcinfected.main.SoundUtils;
import com.ericlam.mc.mcinfected.manager.HunterManager;
import com.ericlam.mc.minigames.core.character.TeamPlayer;
import com.ericlam.mc.minigames.core.function.CircularIterator;
import com.ericlam.mc.minigames.core.game.GameState;
import com.ericlam.mc.minigames.core.main.MinigamesCore;
import com.ericlam.mc.minigames.core.manager.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class InfectingTask extends InfTask {

    @Override
    public void initRun(PlayerManager playerManager) {
        CustomCSWeapon.getApi().getMolotovManager().resetFires();
        playerManager.getTotalPlayers().forEach(playerManager::setGamePlayer);
        MinigamesCore.getApi().getGameManager().setState(GameState.PRESTART);
        Bukkit.broadcastMessage(msg.get("Game.Infecting"));
        Bukkit.broadcastMessage(msg.get("Game.Kit-Chose"));
        List<Location> locations = MinigamesCore.getApi().getArenaManager().getFinalArena().getWarp("human");
        CircularIterator<Location> locationCircularIterator = new CircularIterator<>(locations);
        playerManager.getGamePlayer().forEach(p -> {
            p.castTo(TeamPlayer.class).setTeam(mcinf.getHumanTeam());
            VotingTask.bossBar.addPlayer(p.getPlayer());
            HunterManager.addPlayer(p);
            p.getPlayer().teleportAsync(locationCircularIterator.next());
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1, false, false));
        });
    }

    @Override
    public void onCancel() {
        GameEndTask.cancelGame(playerManager.getGamePlayer());
        Bukkit.broadcastMessage(msg.get("Error.Game.Not Enough Players"));
    }

    @Override
    public void onFinish() {
        SoundUtils.playInfectSound(true);
    }

    @Override
    public long run(long l) {
        if (l % 30 == 0 || l == 20 || l <= 5) {
            String time = MinigamesCore.getApi().getGameUtils().getTimeWithUnit(l);
            Bukkit.broadcastMessage(msg.get("Game.Time.Infecting").replace("<time>", time));
            SoundUtils.playInfectSound(false);
        }
        int level = (int) l;
        Bukkit.getOnlinePlayers().forEach(p -> p.setLevel(level));
        VotingTask.updateBoard(l, playerManager.getGamePlayer(), "&a未感染");
        return l;
    }

    @Override
    public long getTotalTime() {
        return infConfig.gameTime.infecting;
    }

    @Override
    public boolean shouldCancel() {
        return playerManager.getGamePlayer().size() < 2;
    }
}
