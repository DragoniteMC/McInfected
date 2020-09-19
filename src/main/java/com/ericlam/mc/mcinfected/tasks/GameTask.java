package com.ericlam.mc.mcinfected.tasks;

import com.ericlam.mc.mcinfected.implement.McInfPlayer;
import com.ericlam.mc.mcinfected.implement.team.HumanTeam;
import com.ericlam.mc.mcinfected.implement.team.ZombieTeam;
import com.ericlam.mc.mcinfected.main.McInfected;
import com.ericlam.mc.mcinfected.main.SoundUtils;
import com.ericlam.mc.minigames.core.arena.Arena;
import com.ericlam.mc.minigames.core.character.GamePlayer;
import com.ericlam.mc.minigames.core.character.TeamPlayer;
import com.ericlam.mc.minigames.core.game.GameState;
import com.ericlam.mc.minigames.core.main.MinigamesCore;
import com.ericlam.mc.minigames.core.manager.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class GameTask extends InfTask {

    static List<GamePlayer> alphasZombies = new ArrayList<>();

    @Override
    public void initRun(PlayerManager playerManager) {
        MinigamesCore.getApi().getGameManager().setState(GameState.IN_GAME);
        float percent = infConfig.game.alphaPercent;
        int alphas = Math.round(playerManager.getGamePlayer().size() * percent);
        Random random = new Random();
        alphasZombies.clear();
        LinkedList<GamePlayer> list = new LinkedList<>(playerManager.getGamePlayer());
        while (alphasZombies.size() < alphas) {
            int i = random.nextInt(list.size());
            TeamPlayer player = list.remove(i).castTo(TeamPlayer.class);
            player.setTeam(mcinf.getZombieTeam());
            alphasZombies.add(player);
            Bukkit.broadcastMessage(msg.get("Game.Infected").replace("<player>", player.getPlayer().getDisplayName()));
        }
        playerManager.getGamePlayer().forEach(p -> McInfected.getApi().gainKit(p.castTo(McInfPlayer.class)));

    }

    @Override
    public void onCancel() {
        this.onFinish();
        boolean noone = playerManager.getGamePlayer().size() + getDeathGamer() < infConfig.game.autoStart;
        if (noone) {
            GameEndTask.cancelGame(playerManager.getGamePlayer());
            Bukkit.broadcastMessage(msg.get("Error.Game.Not Enough Players"));
        } else {
            MinigamesCore.getApi().getScheduleManager().jumpInto(mcinf.getGameEndState(), false);
        }
    }

    @Override
    public void onFinish() {
        airDropManager.removeAirDrop();
    }

    @Override
    public long run(long l) {
        if (l % 30 == 0 || l == 10 || l <= 5) {
            String time = MinigamesCore.getApi().getGameUtils().getTimeWithUnit(l);
            Bukkit.broadcastMessage(msg.get("Game.Time.Game").replace("<time>", time));
            SoundUtils.playGameSound(false);
        }
        if (l == infConfig.compassGivenInSec) {
            playerManager.getGamePlayer()
                    .stream().filter(g -> g.castTo(TeamPlayer.class).getTeam() instanceof ZombieTeam)
                    .forEach(p -> {
                        p.getPlayer().getInventory().addItem(new ItemStack(Material.COMPASS));
                        MinigamesCore.getApi().getGameUtils().playSound(p.getPlayer(), infConfig.sounds.compass.split(":"));
                    });
        }
        int level = (int) l;
        if (l == getTotalTime() / 2 && !hunterManager.isNotified()) {
            Arena arena = MinigamesCore.getApi().getArenaManager().getFinalArena();
            List<Location> locations = arena.getWarp("airdrop");
            airDropManager.spawnAirDrop(locations);
            airDropManager.notifyAirDrop(playerManager.getGamePlayer().stream().filter(t -> t.castTo(TeamPlayer.class).getTeam() instanceof HumanTeam).collect(Collectors.toList()));
        }
        Bukkit.getOnlinePlayers().forEach(p -> p.setLevel(level));
        VotingTask.bossBar.setProgress((double) l / getTotalTime());
        VotingTask.updateBoard(l, playerManager.getGamePlayer(), "&c母體已出現");
        if (hunterManager.shouldHunterActive()) {
            VotingTask.bossBar.setVisible(false);
            hunterManager.setBarVisible(true);
            hunterManager.updateHunterBossBar();
            hunterManager.notifyHunters();
            airDropManager.removeAirDrop();
        }
        return l;
    }

    @Override
    public long getTotalTime() {
        return infConfig.gameTime.game;
    }

    private long getDeathGamer() {
        return playerManager.getSpectators().stream().filter(e -> e.castTo(TeamPlayer.class).getTeam() != null).count();
    }

    @Override
    public boolean shouldCancel() {
        boolean noone = playerManager.getGamePlayer().size() + getDeathGamer() < infConfig.game.autoStart;
        boolean normalEnd = playerManager.getGamePlayer().stream().noneMatch(g -> g.castTo(TeamPlayer.class).getTeam() instanceof HumanTeam) || playerManager.getGamePlayer().stream().noneMatch(g -> g.castTo(TeamPlayer.class).getTeam() instanceof ZombieTeam);
        return noone || normalEnd;
    }
}
