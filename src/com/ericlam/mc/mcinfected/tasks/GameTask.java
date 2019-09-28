package com.ericlam.mc.mcinfected.tasks;

import com.ericlam.mc.mcinfected.config.InfConfig;
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
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class GameTask extends InfTask {

    public static StorageMinecart airdrop = null;
    static List<GamePlayer> alphasZombies = new ArrayList<>();
    private boolean notifiedHunter = false;

    public static boolean shouldHunterActivate(final List<GamePlayer> gamePlayers) {
        if (gamePlayers.size() < 1) return false;
        float hunterPercent = McInfected.getApi().getConfigManager().getConfigAs(InfConfig.class).hunterPercent;
        List<GamePlayer> humans = gamePlayers.stream().filter(g -> g.castTo(TeamPlayer.class).getTeam() instanceof HumanTeam).collect(Collectors.toList());
        int hunterSize = (int) Math.floor(gamePlayers.size() * hunterPercent);
        return hunterSize >= humans.size();
    }

    @Override
    public void initRun(PlayerManager playerManager) {
        MinigamesCore.getApi().getGameManager().setState(GameState.IN_GAME);
        float percent = infConfig.alphaPercent;
        int alphas = Math.round(playerManager.getGamePlayer().size() * percent);
        Random random = new Random();
        alphasZombies.clear();
        LinkedList<GamePlayer> list = new LinkedList<>(playerManager.getGamePlayer());
        while (alphasZombies.size() < alphas) {
            int i = random.nextInt(list.size());
            TeamPlayer player = list.remove(i).castTo(TeamPlayer.class);
            player.setTeam(mcinf.getZombieTeam());
            alphasZombies.add(player);
            Bukkit.broadcastMessage(McInfected.getApi().getConfigManager().getMessage("Game.Infected").replace("<player>", player.getPlayer().getDisplayName()));
        }
        playerManager.getGamePlayer().forEach(p -> McInfected.getApi().gainKit(p.castTo(McInfPlayer.class)));

    }

    @Override
    public void onCancel() {
        this.onFinish();
        boolean noone = playerManager.getGamePlayer().size() + getDeathGamer() < infConfig.autoStart;
        if (noone) {
            GameEndTask.cancelGame(playerManager.getGamePlayer());
            Bukkit.broadcastMessage(McInfected.getApi().getConfigManager().getMessage("Error.Game.Not Enough Players"));
        } else {
            MinigamesCore.getApi().getScheduleManager().jumpInto(mcinf.getGameEndState(), false);
        }
    }

    @Override
    public void onFinish() {
        if (airdrop != null) airdrop.remove();
        this.notifiedHunter = false;
    }

    @Override
    public long run(long l) {
        if (l % 30 == 0 || l == 10 || l <= 5) {
            String time = MinigamesCore.getApi().getGameUtils().getTimeWithUnit(l);
            Bukkit.broadcastMessage(McInfected.getApi().getConfigManager().getMessage("Game.Time.Game").replace("<time>", time));
            SoundUtils.playGameSound(false);
        }
        if (l == infConfig.compassGiven) {
            playerManager.getGamePlayer()
                    .stream().filter(g -> g.castTo(TeamPlayer.class).getTeam() instanceof ZombieTeam)
                    .forEach(p -> {
                        p.getPlayer().getInventory().addItem(new ItemStack(Material.COMPASS));
                        MinigamesCore.getApi().getGameUtils().playSound(p.getPlayer(), infConfig.sounds.get("Compass").split(":"));
                    });
        }
        int level = (int) l;
        if (l == getTotalTime() / 2 && !notifiedHunter) {
            Arena arena = MinigamesCore.getApi().getArenaManager().getFinalArena();
            List<Location> locations = arena.getWarp("airdrop");
            Location randomLoc = locations.get(new Random().nextInt(locations.size()));
            airdrop = (StorageMinecart) arena.getWorld().spawnEntity(randomLoc, EntityType.MINECART_CHEST);
            airdrop.setCustomName("§e補救箱");
            airdrop.setInvulnerable(true);
            airdrop.setGlowing(true);
            airdrop.setSlowWhenEmpty(true);
            airdrop.setCustomNameVisible(true);
            playerManager.getGamePlayer().stream().filter(t -> t.castTo(TeamPlayer.class).getTeam() instanceof HumanTeam).forEach(p -> {
                p.getPlayer().sendTitle("", "§a補救箱已送達。", 0, 60, 20);
                p.getPlayer().playSound(randomLoc, Sound.ENTITY_ENDERMAN_STARE, 50, 3);
            });
            MinigamesCore.getApi().getFireWorkManager().spawnFireWork(List.of(randomLoc));
        }
        Bukkit.getOnlinePlayers().forEach(p -> p.setLevel(level));
        VotingTask.bossBar.setProgress((double) l / getTotalTime());
        VotingTask.updateBoard(l, playerManager.getGamePlayer(), "&c母體已出現");
        if (shouldHunterActivate(playerManager.getGamePlayer()) && !notifiedHunter) {
            VotingTask.hunterBossBar.setVisible(true);
            VotingTask.bossBar.setVisible(false);
            VotingTask.updateHunterBossBar(playerManager.getGamePlayer());
            playerManager.getGamePlayer().stream().filter(g -> g.castTo(TeamPlayer.class).getTeam() instanceof HumanTeam).forEach(g -> {
                Player player = g.getPlayer();
                player.setGlowing(true);
                MinigamesCore.getApi().getGameUtils().playSound(player, infConfig.soundHunter.get("Active").split(":"));
                player.sendTitle("", "§a按 F 可以化身成幽靈獵手。", 0, 100, 0);
            });
            if (airdrop != null) airdrop.remove();
            this.notifiedHunter = true;
        }
        return l;
    }

    @Override
    public long getTotalTime() {
        return infConfig.gameTime;
    }

    private long getDeathGamer() {
        return playerManager.getSpectators().stream().filter(e -> e.castTo(TeamPlayer.class).getTeam() != null).count();
    }

    @Override
    public boolean shouldCancel() {
        boolean noone = playerManager.getGamePlayer().size() + getDeathGamer() < infConfig.autoStart;
        boolean normalEnd = playerManager.getGamePlayer().stream().noneMatch(g -> g.castTo(TeamPlayer.class).getTeam() instanceof HumanTeam) || playerManager.getGamePlayer().stream().noneMatch(g -> g.castTo(TeamPlayer.class).getTeam() instanceof ZombieTeam);
        return noone || normalEnd;
    }
}
