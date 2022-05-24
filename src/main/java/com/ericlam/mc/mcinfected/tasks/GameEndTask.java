package com.ericlam.mc.mcinfected.tasks;

import com.ericlam.mc.eld.ELDependenci;
import com.ericlam.mc.mcinfected.implement.McInfGameStats;
import com.ericlam.mc.mcinfected.implement.team.HumanTeam;
import com.ericlam.mc.mcinfected.implement.team.ZombieTeam;
import com.ericlam.mc.mcinfected.main.McInfected;
import com.ericlam.mc.minigames.core.character.GamePlayer;
import com.ericlam.mc.minigames.core.character.TeamPlayer;
import com.ericlam.mc.minigames.core.exception.gamestats.PlayerNotExistException;
import com.ericlam.mc.minigames.core.main.MinigamesCore;
import com.ericlam.mc.minigames.core.manager.PlayerManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import org.dragonitemc.dragoneconomy.api.AsyncEconomyService;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class GameEndTask extends InfTask {

    private static int humanWins = 0;
    private static int zombieWins = 0;
    private int currentRound = 0;

    private AsyncEconomyService economyService = ELDependenci.getApi().exposeService(AsyncEconomyService.class);

    public static String getTeamScore() {
        String zombieWin = ChatColor.RED.toString() + ChatColor.BOLD.toString() + GameEndTask.zombieWins + ChatColor.RESET.toString();
        String humanWin = ChatColor.GREEN.toString() + ChatColor.BOLD.toString() + GameEndTask.humanWins + ChatColor.RESET.toString();
        return zombieWin.concat("§7§l : §r").concat(humanWin);
    }

    static void cancelGame(List<GamePlayer> gamePlayers) {
        McInfected inf = McInfected.getPlugin(McInfected.class);
        MinigamesCore.getApi().getGameManager().endGame(gamePlayers, humanWins == zombieWins ? null : humanWins > zombieWins ? inf.getHumanTeam() : inf.getZombieTeam(), true);
    }

    private static void addLose(GamePlayer g) {
        try {
            McInfGameStats stats = MinigamesCore.getApi().getGameStatsManager().getGameStats(g).castTo(McInfGameStats.class);
            stats.setLoses(stats.getLoses() + 1);
        } catch (PlayerNotExistException e) {
            McInfected.getProvidingPlugin(McInfected.class).getLogger().warning(e.getGamePlayer().getName() + " is not exist");
        }
    }

    private List<GamePlayer> getHumans() {
        return playerManager.getGamePlayer().stream().filter(g -> g.castTo(TeamPlayer.class).getTeam() instanceof HumanTeam).collect(Collectors.toList());
    }

    @Override
    public void initRun(PlayerManager playerManager) {
        boolean zombieWin = playerManager.getGamePlayer().stream().noneMatch(g -> g.castTo(TeamPlayer.class).getTeam() instanceof HumanTeam);
        String title = msg.getPure("Game.Over.".concat(zombieWin ? "Infected" : "Humans").concat(" Win"));
        Title.Times time = Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(5), Duration.ofSeconds(1));
        Title t = Title.title(Component.text(title), Component.empty(), time);
        Bukkit.getOnlinePlayers().forEach(p -> p.showTitle(t));
        if (zombieWin) {
            zombieWins++;
            VotingTask.bossBar.setColor(BarColor.RED);
            GameTask.alphasZombies.forEach(p -> {
                MinigamesCore.getApi().getGameStatsManager().addWins(p, 1);
            });
            playerManager.getTotalPlayers().stream().filter(g -> g.castTo(TeamPlayer.class).getTeam() instanceof ZombieTeam && !GameTask.alphasZombies.contains(g))
                    .forEach(GameEndTask::addLose);
        } else {
            getHumans().forEach(p -> {
                Player player = p.getPlayer();
                player.setGlowing(true);
                MinigamesCore.getApi().getFireWorkManager().spawnFireWork(player);
                MinigamesCore.getApi().getGameStatsManager().addWins(p, 1);
                var reward = infConfig.reward.human;
                economyService.depositPlayer(p.getPlayer().getUniqueId(), reward).thenRunSync(updateResult -> p.getPlayer().sendMessage("§6+" + reward + " $WRLD (成功存活)")).join();
            });
            GameTask.alphasZombies.forEach(GameEndTask::addLose);
            humanWins++;
            VotingTask.bossBar.setColor(BarColor.GREEN);
            Bukkit.broadcastMessage(msg.get("Game.Over.Survivors").replace("<humans>", getHumans().stream().map(e -> e.getPlayer().getDisplayName()).collect(Collectors.joining(", "))));
        }
        currentRound++;
        VotingTask.updateBoard(0, playerManager.getGamePlayer(), zombieWin ? "&4全部感染" : "&a抵抗成功");
    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onFinish() {
        int maxRound = infConfig.game.maxRound;
        getHumans().forEach(p -> p.getPlayer().setGlowing(false));
        int matchPoint = (int) Math.ceil((double) maxRound / 2);
        if (matchPoint % 2 == 0) matchPoint++;
        String mpTitle = msg.getPure("Picture.Bar.Mp");
        Title.Times time = Title.Times.times(Duration.ofSeconds(0), Duration.ofSeconds(2), Duration.ofSeconds(1));
        Title t = Title.title(Component.empty(), Component.text(mpTitle), time);
        if (zombieWins == matchPoint - 1 || humanWins == matchPoint - 1) {
            Bukkit.getOnlinePlayers().forEach(p -> p.showTitle(t));
        }
        VotingTask.bossBar.setTitle(msg.getPure("Picture.Bar.Title")
                .replace("<z>", zombieWins + "")
                .replace("<h>", humanWins + ""));
        VotingTask.bossBar.setProgress(1.0);
        VotingTask.bossBar.setColor(BarColor.PURPLE);
        if (currentRound == maxRound || humanWins == matchPoint || zombieWins == matchPoint) {
            cancelGame(playerManager.getGamePlayer());
        } else {
            MinigamesCore.getApi().getScheduleManager().jumpInto(mcinf.getPreStartState(), false);
        }
    }

    @Override
    public long run(long l) {
        if (l == 4) {
            VotingTask.bossBar.setVisible(true);
            hunterManager.setBarVisible(false);
        }
        return l;
    }

    @Override
    public long getTotalTime() {
        return 7;
    }

    @Override
    public boolean shouldCancel() {
        return false;
    }
}
