package com.ericlam.mc.mcinfected;

import com.ericlam.mc.mcinfected.implement.McInfGameStats;
import com.ericlam.mc.mcinfected.implement.McInfPlayer;
import com.ericlam.mc.mcinfected.implement.team.HumanTeam;
import com.ericlam.mc.mcinfected.implement.team.ZombieTeam;
import com.ericlam.mc.mcinfected.main.McInfected;
import com.ericlam.mc.mcinfected.tasks.GameEndTask;
import com.ericlam.mc.minigames.core.character.GamePlayer;
import com.ericlam.mc.minigames.core.character.TeamPlayer;
import com.ericlam.mc.minigames.core.event.player.CrackShotDeathEvent;
import com.ericlam.mc.minigames.core.event.player.GamePlayerDeathEvent;
import com.ericlam.mc.minigames.core.event.section.GamePreEndEvent;
import com.ericlam.mc.minigames.core.exception.gamestats.PlayerNotExistException;
import com.ericlam.mc.minigames.core.game.GameState;
import com.ericlam.mc.minigames.core.game.GameTeam;
import com.ericlam.mc.minigames.core.main.MinigamesCore;
import com.ericlam.mc.minigames.core.manager.PlayerManager;
import com.hypernite.mc.hnmc.core.utils.Tools;
import me.DeeCaaD.CrackShotPlus.API;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

public class McInfListener implements Listener {

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        switch (e.getAction()) {
            case DROP_ALL_CURSOR:
            case DROP_ONE_CURSOR:
                e.setCancelled(true);
            default:
                break;
        }
    }

    @EventHandler
    public void onPickup(PlayerAttemptPickupItemEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (MinigamesCore.getApi().getGameManager().getInGameState() == McInfected.getPlugin(McInfected.class).getGameEndState())
            e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        GameState state = MinigamesCore.getApi().getGameManager().getGameState();
        PlayerManager playerManager = MinigamesCore.getApi().getPlayerManager();
        switch (state) {
            case PRESTART:
            case IN_GAME:
                break;
            default:
                return;
        }

        GamePlayer player = playerManager.buildGamePlayer(e.getPlayer());
        switch (state) {
            case PRESTART:
                playerManager.setGamePlayer(player);
                break;
            case IN_GAME:
                playerManager.setSpectator(player);
                break;
        }
    }


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        if (MinigamesCore.getApi().getGameManager().getGameState() == GameState.PREEND) return;
        Bukkit.broadcastMessage(McInfected.getApi().getConfigManager().getMessage("Command.Game.Left").replace("<player>", e.getPlayer().getDisplayName()));
    }

    @EventHandler
    public void onGamePreEnd(GamePreEndEvent e) {
        GameTeam team = e.getWinnerTeam();
        String path = "Game.Over.Result.".concat(team == null ? "Draw" : team instanceof HumanTeam ? "Human" : "Infected");
        Bukkit.getOnlinePlayers().forEach(p -> p.sendTitle(GameEndTask.getTeamScore(), McInfected.getApi().getConfigManager().getPureMessage(path), 0, 180, 20));
    }

    @EventHandler
    public void onFoodLevel(FoodLevelChangeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDeath(GamePlayerDeathEvent e) {
        TeamPlayer victim = e.getGamePlayer().castTo(TeamPlayer.class);
        Player player = victim.getPlayer();
        if (e.getKiller() != null) {
            TeamPlayer killer = e.getKiller().castTo(TeamPlayer.class);
            String action = null;
            if (killer.getTeam() instanceof HumanTeam) {
                MinigamesCore.getApi().getGameStatsManager().addKills(killer, 1);
                action = "Other";
                if (e instanceof CrackShotDeathEvent) {
                    CrackShotDeathEvent cs = (CrackShotDeathEvent) e;
                    if (API.getCSDirector().getBoolean(cs.getWeaponTitle() + ".Item_Information.Melee_Mode")) {
                        action = "Melee";
                        victim.setTeam(null);
                        victim.castTo(McInfPlayer.class).setKillByMelee(true);
                        MinigamesCore.getApi().getPlayerManager().setSpectator(victim);
                        player.sendTitle("", "§7你因被近身武器致死，無法復活。", 0, 60, 40);
                    } else if (cs.getBullet() instanceof TNTPrimed) {
                        action = "Explosion";
                    } else if (cs.getBullet() instanceof Projectile) {
                        action = "Gun";
                    }
                }
                action = "Death Messages.Human.".concat(action);
            } else if (killer.getTeam() instanceof ZombieTeam) {
                action = "Death Messages.Infected.Normal";
                try {
                    McInfGameStats stats = MinigamesCore.getApi().getGameStatsManager().getGameStats(killer).castTo(McInfGameStats.class);
                    stats.setInfected(stats.getInfected() + 1);
                    McInfected.getApi().getConfigManager().getData("infected", String[].class).ifPresent(s -> MinigamesCore.getApi().getGameUtils().playSound(player, s));
                } catch (PlayerNotExistException ex) {
                    McInfected.getPlugin(McInfected.class).getLogger().log(Level.SEVERE, ex.getMessage());
                }
            }
            if (action != null) {
                Bukkit.broadcastMessage(McInfected.getApi().getConfigManager().getMessage(action).replace("<killer>", killer.getPlayer().getDisplayName()).replace("<killed>", player.getDisplayName()));
            }
        } else {
            Bukkit.broadcastMessage(McInfected.getApi().getConfigManager().getMessage("Death Messages.Self").replace("<killed>", player.getDisplayName()));
        }

        if (victim.getTeam() instanceof ZombieTeam) {
            if (victim.getStatus() == GamePlayer.Status.SPECTATING) return;
            McInfected.getApi().getConfigManager().getData("respawn", String[].class).ifPresent(s -> MinigamesCore.getApi().getGameUtils().playSound(player, s));
            Optional.ofNullable(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).ifPresent(a -> player.setHealth(a.getBaseValue()));
            List<Location> respawn = MinigamesCore.getApi().getArenaManager().getFinalArena().getWarp("zombie");
            player.teleportAsync(respawn.get(Tools.randomWithRange(0, respawn.size() - 1)));
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1, false, false));
        } else if (victim.getTeam() instanceof HumanTeam) {
            victim.setTeam(McInfected.getPlugin(McInfected.class).getZombieTeam());
            McInfected.getApi().removePreviousKit(player, true);
            McInfected.getApi().gainKit(victim.castTo(McInfPlayer.class));
        }

    }
}
