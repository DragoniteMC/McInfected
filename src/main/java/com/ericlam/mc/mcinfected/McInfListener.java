package com.ericlam.mc.mcinfected;

import com.ericlam.mc.eld.ELDependenci;
import com.ericlam.mc.mcinfected.api.McInfectedAPI;
import com.ericlam.mc.mcinfected.config.InfConfig;
import com.ericlam.mc.mcinfected.config.LangConfig;
import com.ericlam.mc.mcinfected.implement.McInfGameStats;
import com.ericlam.mc.mcinfected.implement.McInfPlayer;
import com.ericlam.mc.mcinfected.implement.team.HumanTeam;
import com.ericlam.mc.mcinfected.implement.team.ZombieTeam;
import com.ericlam.mc.mcinfected.main.McInfected;
import com.ericlam.mc.mcinfected.manager.AirDropManager;
import com.ericlam.mc.mcinfected.manager.HunterManager;
import com.ericlam.mc.mcinfected.tasks.GameEndTask;
import com.ericlam.mc.mcinfected.tasks.VotingTask;
import com.ericlam.mc.minigames.core.character.GamePlayer;
import com.ericlam.mc.minigames.core.character.TeamPlayer;
import com.ericlam.mc.minigames.core.event.player.CrackShotDeathEvent;
import com.ericlam.mc.minigames.core.event.player.GamePlayerDeathEvent;
import com.ericlam.mc.minigames.core.event.player.GamePlayerJoinEvent;
import com.ericlam.mc.minigames.core.event.section.GamePreEndEvent;
import com.ericlam.mc.minigames.core.event.state.GameStateSwitchEvent;
import com.ericlam.mc.minigames.core.event.state.InGameStateSwitchEvent;
import com.ericlam.mc.minigames.core.exception.gamestats.PlayerNotExistException;
import com.ericlam.mc.minigames.core.game.GameState;
import com.ericlam.mc.minigames.core.game.GameTeam;
import com.ericlam.mc.minigames.core.main.MinigamesCore;
import com.ericlam.mc.minigames.core.manager.GameUtils;
import com.ericlam.mc.minigames.core.manager.PlayerManager;
import com.dragonite.mc.dnmc.core.managers.YamlManager;
import com.dragonite.mc.dnmc.core.utils.Tools;
import com.shampaggon.crackshot.events.WeaponDamageEntityEvent;
import me.DeeCaaD.CrackShotPlus.API;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.dragonitemc.dragoneconomy.api.AsyncEconomyService;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

public class McInfListener implements Listener {

    private final InfConfig infConfig;
    private final LangConfig msg;
    private final Set<Player> suicideCooldown = new HashSet<>();
    private final AirDropManager airDropManager;
    private final HunterManager hunterManager;
    private double multiplier = 0.0;

    private AsyncEconomyService economyService = ELDependenci.getApi().exposeService(AsyncEconomyService.class);

    public McInfListener(YamlManager yamlManager, AirDropManager airDropManager, HunterManager hunterManager) {
        this.infConfig = yamlManager.getConfigAs(InfConfig.class);
        this.msg = yamlManager.getConfigAs(LangConfig.class);
        this.airDropManager = airDropManager;
        this.hunterManager = hunterManager;

        //Air drop content
        airDropManager.addAirDropHandler(p -> MinigamesCore.getApi().getPlayerManager().getGamePlayer().stream().filter(g -> g.castTo(TeamPlayer.class).getTeam() instanceof HumanTeam).map(g -> g.castTo(McInfPlayer.class)).forEach(infPlayer -> {
            String kit = infPlayer.getHumanKit();
            Player player = infPlayer.getPlayer();
            McInfected.getApi().gainKit(player, kit);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1, 2);
            Title.Times time = Title.Times.times(Duration.ofSeconds(0), Duration.ofSeconds(3), Duration.ofSeconds(1));
            Title t = Title.title(Component.empty(), Component.text("§a全體彈藥已補完"), time);
            player.showTitle(t);
        }));

        airDropManager.addAirDropHandler(infPlayer -> {
            Player player = infPlayer.getPlayer();
            AttributeInstance instance = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (instance == null) return;
            instance.setBaseValue(200.0);
            player.setHealth(instance.getBaseValue());
            player.setHealthScale(20.0);
            Title.Times time = Title.Times.times(Duration.ofSeconds(0), Duration.ofSeconds(3), Duration.ofSeconds(1));
            Title t = Title.title(Component.empty(), Component.text("§a獲得: 防化服"), time);
            player.showTitle(t);
        });

    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        switch (e.getAction()) {
            case DROP_ALL_CURSOR:
            case DROP_ONE_CURSOR:
                e.setCancelled(true);
                return;
            default:
                break;
        }
    }

    @EventHandler
    public void onGameStateSwitch(InGameStateSwitchEvent e) {
        if (e.getInGameState() != McInfected.getPlugin(McInfected.class).getGameEndState()) return;
        this.multiplier = 0.0;
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
    public void onPlayerJoin(GamePlayerJoinEvent e) {
        GamePlayer player = e.getGamePlayer();
        GameState state = e.getGameState();
        PlayerManager playerManager = MinigamesCore.getApi().getPlayerManager();
        switch (state) {
            case PRESTART:
            case IN_GAME:
                break;
            default:
                return;
        }
        MinigamesCore.getApi().getGameStatsManager().loadGameStats(player);
        VotingTask.addPlayer(player);
        Location loc = null;
        switch (state) {
            case PRESTART:
                playerManager.setGamePlayer(player);
                player.castTo(TeamPlayer.class).setTeam(McInfected.getPlugin(McInfected.class).getHumanTeam());
                List<Location> locs = MinigamesCore.getApi().getArenaManager().getFinalArena().getWarp("human");
                loc = locs.get(Tools.randomWithRange(0, locs.size() - 1));
                break;
            case IN_GAME:
                playerManager.setSpectator(player);
                loc = playerManager.getGamePlayer().get(Tools.randomWithRange(0, playerManager.getGamePlayer().size() - 1)).getPlayer().getLocation();
                break;
        }
        if (loc != null) player.getPlayer().teleportAsync(loc);
    }


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        if (MinigamesCore.getApi().getGameManager().getGameState() == GameState.PREEND) return;
        Bukkit.broadcastMessage(msg.get("Command.Game.Left").replace("<player>", e.getPlayer().getDisplayName()));
    }

    @EventHandler
    public void onGamePreEnd(GamePreEndEvent e) {
        GameTeam team = e.getWinnerTeam();
        String path = "Game.Over.Result.".concat(team == null ? "Draw" : team instanceof HumanTeam ? "Human" : "Infected");
        Title.Times time = Title.Times.times(Duration.ofSeconds(0), Duration.ofSeconds(9), Duration.ofSeconds(1));
        Title t = Title.title(Component.text(GameEndTask.getTeamScore()), Component.text(msg.getPure(path)), time);
        Bukkit.getOnlinePlayers().forEach(p -> p.showTitle(t));
    }

    @EventHandler
    public void onInteractAirDrop(PlayerInteractEntityEvent e) {
        airDropManager.onInteractAirDrop(e);
    }

    @EventHandler
    public void onFoodLevel(FoodLevelChangeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onDamage(WeaponDamageEntityEvent e) {
        if (!(e.getVictim() instanceof Player)) return;
        Optional<GamePlayer> vic = MinigamesCore.getApi().getPlayerManager().findPlayer((Player) e.getVictim());
        Optional<GamePlayer> att = MinigamesCore.getApi().getPlayerManager().findPlayer(e.getPlayer());
        if (att.isEmpty() || vic.isEmpty()) return;
        TeamPlayer attacker = att.get().castTo(TeamPlayer.class);
        TeamPlayer victim = vic.get().castTo(TeamPlayer.class);
        if (attacker.getTeam() instanceof ZombieTeam || victim.getTeam() instanceof HumanTeam) return;
        String using = McInfected.getApi().currentKit(e.getPlayer());
        String hunterKit = infConfig.defaultKit.get("hunter");
        if (using != null && using.equals(hunterKit)) return;
        final double originalDamage = e.getDamage();
        e.setDamage(originalDamage * (1 + multiplier));
    }

    @EventHandler(ignoreCancelled = true)
    public void onWeaponEntityDamage(WeaponDamageEntityEvent e) {
        if (!(e.getVictim() instanceof Player)) return;
        MinigamesCore.getApi().getPlayerManager().findPlayer((Player) e.getVictim()).ifPresent(g -> {
            if (!(g.castTo(TeamPlayer.class).getTeam() instanceof HumanTeam)) return;
            boolean antiVirusSuit = g.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() == 200.0;
            double health = g.getPlayer().getHealth() - e.getDamage();
            boolean oneChance = health < 100 && health > 0;
            if (antiVirusSuit && oneChance) {
                Title.Times time = Title.Times.times(Duration.ofSeconds(0), Duration.ofSeconds(2), Duration.ofSeconds(0));
                g.getPlayer().showTitle(Title.title(Component.empty(), Component.text("§c你的防化服已失效"), time));
                g.getPlayer().playSound(g.getPlayer().getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1, 1);
                e.getPlayer().showTitle(Title.title(Component.empty(), Component.text("§4§l☣ §4感染失敗"), time));
                e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_ANVIL_DESTROY, 1, 1);
            }
        });
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
                        MinigamesCore.getApi().getPlayerManager().setSpectator(victim);
                        Title.Times time = Title.Times.times(Duration.ofSeconds(0), Duration.ofSeconds(3), Duration.ofSeconds(2));
                        Title t = Title.title(Component.empty(), Component.text("§7你因被近身武器致死，無法復活。"), time);
                        player.showTitle(t);
                        var reward = infConfig.reward.knife;
                        economyService.depositPlayer(killer.getPlayer().getUniqueId(), reward).thenRunSync(updateResult -> killer.getPlayer().sendMessage("§6+" + reward + " $WRLD (擊殺殭屍)")).join();
                        McInfected.getApi().addLeaderBoard(killer.getPlayer(), reward);
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
                    var reward = infConfig.reward.zombie;
                    economyService.depositPlayer(killer.getPlayer().getUniqueId(), reward).thenRunSync(updateResult -> killer.getPlayer().sendMessage("§6+" + reward + " $WRLD (感染人類)")).join();
                    McInfected.getApi().addLeaderBoard(killer.getPlayer(), reward);
                    MinigamesCore.getApi().getGameUtils().playSound(player, infConfig.sounds.infected.split(":"));
                } catch (PlayerNotExistException ex) {
                    McInfected.getPlugin(McInfected.class).getLogger().log(Level.SEVERE, ex.getMessage());
                }
            }
            if (action != null) {
                Bukkit.broadcastMessage(msg.get(action).replace("<killer>", killer.getPlayer().getDisplayName()).replace("<killed>", player.getDisplayName()));
            }
        } else {
            Bukkit.broadcastMessage(msg.get("Death Messages.Self").replace("<killed>", player.getDisplayName()));
        }

        if (victim.getTeam() instanceof ZombieTeam) {
            if (victim.getStatus() == GamePlayer.Status.SPECTATING) {
                hunterManager.updateHunterBossBar();
                return;
            }
            MinigamesCore.getApi().getPlayerManager().setSpectator(victim);
            player.sendMessage(msg.get("Game.Respawn"));
            Bukkit.getScheduler().runTaskLater(McInfected.getPlugin(McInfected.class), () -> {
                MinigamesCore.getApi().getGameUtils().playSound(player, infConfig.sounds.respawn.split(":"));
                Optional.ofNullable(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).ifPresent(a -> player.setHealth(a.getBaseValue()));
                List<Location> respawn = MinigamesCore.getApi().getArenaManager().getFinalArena().getWarp("zombie");
                //McInfected.getApi().removePreviousKit(player, true);
                player.teleportAsync(respawn.get(Tools.randomWithRange(0, respawn.size() - 1)));
                MinigamesCore.getApi().getPlayerManager().setGamePlayer(victim);
                //McInfected.getApi().gainKit(victim.castTo(McInfPlayer.class));
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1, false, false));
            }, 60L);
        } else if (victim.getTeam() instanceof HumanTeam) {
            McInfectedAPI api = McInfected.getApi();
            GameUtils utils = MinigamesCore.getApi().getGameUtils();
            String using = api.currentKit(e.getGamePlayer().getPlayer());
            if (using != null) {
                String hunterKit = infConfig.defaultKit.get("hunter");
                if (using.equals(hunterKit)) {
                    Bukkit.getOnlinePlayers().forEach(p -> utils.playSound(p, infConfig.sounds.hunter.get("Death").split(":")));
                    MinigamesCore.getApi().getPlayerManager().setSpectator(victim);
                    hunterManager.updateHunterBossBar();
                    return;
                }
            }
            victim.setTeam(McInfected.getPlugin(McInfected.class).getZombieTeam());
            McInfected.getApi().removePreviousKit(player, true);
            McInfected.getApi().gainKit(victim.castTo(McInfPlayer.class));
            multiplier += infConfig.damageMultiplier;
            String showMulti = new DecimalFormat("##.##").format(multiplier * 100);
            String title = msg.getPure("Game.Damage-Indicator").replace("<value>", showMulti + "");
            Title.Times time = Title.Times.times(Duration.ofSeconds(0), Duration.ofSeconds(1), Duration.ofSeconds(1));
            Title t = Title.title(Component.empty(), Component.text(title), time);
            MinigamesCore.getApi().getPlayerManager().getGamePlayer().stream().filter(g -> g.castTo(TeamPlayer.class).getTeam() instanceof HumanTeam).forEach(g -> {
                g.getPlayer().showTitle(t);
                g.getPlayer().playSound(g.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            });
        }
        hunterManager.updateHunterBossBar();
    }

    @EventHandler
    public void onStateSwitch(GameStateSwitchEvent e) {
        if (e.getGameState() != GameState.PRESTART) return;
        suicideCooldown.clear();
    }

    @EventHandler
    public void onCommandSuicide(PlayerCommandPreprocessEvent e) {
        Optional<GamePlayer> gamePlayerOptional = MinigamesCore.getApi().getPlayerManager().findPlayer(e.getPlayer());
        if (gamePlayerOptional.isEmpty()) return;
        GamePlayer gamePlayer = gamePlayerOptional.get();
        if (gamePlayer.getStatus() != GamePlayer.Status.GAMING) return;
        switch (e.getMessage().toLowerCase()) {
            case "/suicide":
            case "/kill":
            case "/death":
            case "/die":
                break;
            default:
                return;
        }
        e.setCancelled(true);
        if (!(gamePlayer.castTo(TeamPlayer.class).getTeam() instanceof ZombieTeam)) {
            e.getPlayer().sendMessage(msg.get("Error.Game.Not An Infected"));
            return;
        }
        if (suicideCooldown.contains(e.getPlayer())) {
            e.getPlayer().sendMessage(msg.get("Error.Command.Suicide Not Cooled Down"));
            return;
        }
        e.getPlayer().damage(2000);
        suicideCooldown.add(e.getPlayer());
        Bukkit.getScheduler().runTaskLater(McInfected.getPlugin(McInfected.class), () -> suicideCooldown.remove(e.getPlayer()), 600L);
    }
}
