package com.ericlam.mc.mcinfected.tasks;

import com.ericlam.mc.mcinfected.implement.team.HumanTeam;
import com.ericlam.mc.mcinfected.main.McInfected;
import com.ericlam.mc.minigames.core.character.GamePlayer;
import com.ericlam.mc.minigames.core.character.TeamPlayer;
import com.ericlam.mc.minigames.core.main.MinigamesCore;
import com.ericlam.mc.minigames.core.manager.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class GameEndTask extends InfTask {

    private static int humanWins = 0;
    private static int zombieWins = 0;
    private int currentRound = 1;

    static void cancelGame(List<GamePlayer> gamePlayers) {
        McInfected inf = McInfected.getPlugin(McInfected.class);
        MinigamesCore.getApi().getGameManager().endGame(gamePlayers, humanWins == zombieWins ? null : humanWins > zombieWins ? inf.getHumanTeam() : inf.getZombieTeam(), true);
    }

    private List<GamePlayer> getHumans() {
        return playerManager.getGamePlayer().stream().filter(g -> g.castTo(TeamPlayer.class).getTeam() instanceof HumanTeam).collect(Collectors.toList());
    }

    @Override
    public void initRun(PlayerManager playerManager) {
        boolean zombieWin = playerManager.getGamePlayer().stream().noneMatch(g -> g.castTo(TeamPlayer.class).getTeam() instanceof HumanTeam);
        if (zombieWin) {
            zombieWins++;
        } else {
            getHumans().forEach(p -> {
                Player player = p.getPlayer();
                player.setGlowing(true);
                MinigamesCore.getApi().getFireWorkManager().spawnFireWork(player);
            });
            humanWins++;
        }
        currentRound++;
        String title = McInfected.config().getPureMessage("Game.Over.".concat(zombieWin ? "Infected" : "Humans").concat("_Win"));
        Bukkit.getOnlinePlayers().forEach(p -> p.sendTitle(title, "", 20, 60, 20));
    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onFinish() {
        int maxRound = McInfected.config().getData("maxRound", Integer.class).orElse(5);
        getHumans().forEach(p -> p.getPlayer().setGlowing(false));
        if (currentRound < maxRound) {
            MinigamesCore.getApi().getScheduleManager().jumpInto(mcinf.getPreStartState(), false);
        }
    }

    @Override
    public long run(long l) {
        return l;
    }

    @Override
    public long getTotalTime() {
        return 5;
    }

    @Override
    public boolean shouldCancel() {
        return false;
    }
}
