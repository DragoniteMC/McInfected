package com.ericlam.mc.mcinfected.tasks;

import com.ericlam.mc.mcinfected.implement.team.HumanTeam;
import com.ericlam.mc.mcinfected.implement.team.ZombieTeam;
import com.ericlam.mc.mcinfected.main.McInfected;
import com.ericlam.mc.mcinfected.main.SoundUtils;
import com.ericlam.mc.mcinfected.manager.HunterManager;
import com.ericlam.mc.minigames.core.arena.Arena;
import com.ericlam.mc.minigames.core.character.GamePlayer;
import com.ericlam.mc.minigames.core.character.TeamPlayer;
import com.ericlam.mc.minigames.core.factory.compass.CompassTracker;
import com.ericlam.mc.minigames.core.factory.scoreboard.GameBoard;
import com.ericlam.mc.minigames.core.main.MinigamesCore;
import com.ericlam.mc.minigames.core.manager.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.List;

public class VotingTask extends InfTask {

    static BossBar bossBar;
    static CompassTracker tracker;
    private static GameBoard gameBoard;
    private boolean loaded = false;
    private Arena arena;

    public static void addPlayer(GamePlayer player) {
        if (gameBoard == null) return;
        gameBoard.addPlayer(player);
        bossBar.addPlayer(player.getPlayer());
        HunterManager.addPlayer(player);
    }

    public static void switchTeam(TeamPlayer player) {
        if (gameBoard == null) return;
        gameBoard.switchTeam(player);
    }

    static void updateBoard(long l, List<GamePlayer> gamePlayers, String stats) {
        if (gameBoard == null) return;
        String time = MinigamesCore.getApi().getGameUtils().getTimer(l);
        String dis = MinigamesCore.getApi().getArenaManager().getFinalArena().getDisplayName();
        gameBoard.setTitle(dis.concat("§7 - §f").concat(time));
        long humans = gamePlayers.stream().filter(g -> g.castTo(TeamPlayer.class).getTeam() instanceof HumanTeam).count();
        long zombies = gamePlayers.stream().filter(g -> g.castTo(TeamPlayer.class).getTeam() instanceof ZombieTeam).count();
        gameBoard.setLine("human", "§a 人類傭兵:§f ".concat(humans + ""));
        gameBoard.setLine("zombie", "§c 生化幽靈:§f ".concat(zombies + ""));
        stats = ChatColor.translateAlternateColorCodes('&', stats);
        gameBoard.setLine("stats", "§e 狀態:§r ".concat(stats));
    }

    @Override
    public void initRun(PlayerManager playerManager) {
        playerManager.getWaitingPlayer().forEach(p -> {
            Player player = p.getPlayer();
            player.sendMessage(msg.get("Game.Start"));
            player.sendTitle("", msg.getPure("Game.Start-Title"), 20, 60, 20);
        });
    }

    @Override
    public void onCancel() {
        Bukkit.broadcastMessage(msg.get("Error.Command.countdown-cancel"));
    }

    @Override
    public void onFinish() {
        McInfected mcinf = McInfected.getPlugin(McInfected.class);
        bossBar = Bukkit.createBossBar(msg.getPure("Picture.Bar.Title").replace("<z>", "0").replace("<h>", "0"), BarColor.PURPLE, BarStyle.SOLID);
        hunterManager.initializeBar();
        gameBoard = MinigamesCore.getProperties().getGameFactory()
                .getScoreboardFactory()
                .setTitle(arena.getDisplayName().concat("§7 - §f00:00"))
                .addLine("§c§l§m-----------", 13)
                .addLine("§f", 12)
                .setLine("human", "§a 人類傭兵:§f 0", 11)
                .setLine("zombie", "§c 生化幽靈:§f 0", 10)
                .addLine("§1", 9)
                .setLine("stats", "§e 狀態:§a 未感染", 8)
                .addLine("§2", 7)
                .addTeamSetting(McInfected.getApi().getHumanTeam(), Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER)
                .build();
        tracker = MinigamesCore.getProperties().getGameFactory()
                .getCompassFactory()
                .setTrackerRange(200)
                .setCaughtText("&e捕捉傭兵: &a<target> &7| &e範圍: &a<distance>")
                .setSearchingText("&e搜索中...", "&a搜索中...", "&c搜索中...", "&b搜索中...")
                .setTeamTarget(mcinf.getZombieTeam(), mcinf.getHumanTeam())
                .build();
        tracker.launch();
    }

    @Override
    public long run(long l) {
        if (l % 30 == 0 || l == 20 || (l <= 10 && l > 5)) {
            String time = MinigamesCore.getApi().getGameUtils().getTimeWithUnit(l - 5);
            Bukkit.broadcastMessage(msg.get("Game.Time.Voting").replace("<time>", time));
            SoundUtils.playVoteSound(false);
        } else if (l == 5) {
            SoundUtils.playVoteSound(true);
            MinigamesCore.getApi().getLobbyManager().runFinalResult();
            loaded = true;
            arena = MinigamesCore.getApi().getArenaManager().getFinalArena();
            MinigamesCore.getApi().getGameUtils().unLagIterate(arena.getWarp("human"), loc -> loc.getChunk().load(true), 10L);
            Bukkit.broadcastMessage(msg.get("Game.Arena Selected").replace("<arena>", arena.getDisplayName()));
            Bukkit.broadcastMessage(msg.get("Game.Time.PreGame"));
        }
        int level = (int) l;
        Bukkit.getOnlinePlayers().forEach(p -> p.setLevel(level));
        return l;
    }

    @Override
    public long getTotalTime() {
        return infConfig.gameTime.voting;
    }

    @Override
    public boolean shouldCancel() {
        return playerManager.getWaitingPlayer().size() < infConfig.game.autoStart && !loaded;
    }
}
