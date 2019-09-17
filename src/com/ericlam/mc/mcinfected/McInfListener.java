package com.ericlam.mc.mcinfected;

import com.ericlam.mc.mcinfected.api.McInfectedAPI;
import com.ericlam.mc.mcinfected.config.InfConfig;
import com.ericlam.mc.mcinfected.implement.McInfGameStats;
import com.ericlam.mc.mcinfected.implement.McInfPlayer;
import com.ericlam.mc.mcinfected.implement.team.HumanTeam;
import com.ericlam.mc.mcinfected.implement.team.ZombieTeam;
import com.ericlam.mc.mcinfected.main.McInfected;
import com.ericlam.mc.mcinfected.tasks.GameEndTask;
import com.ericlam.mc.mcinfected.tasks.GameTask;
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
import com.hypernite.mc.hnmc.core.managers.YamlManager;
import com.hypernite.mc.hnmc.core.utils.Tools;
import com.shampaggon.crackshot.events.WeaponDamageEntityEvent;
import me.DeeCaaD.CrackShotPlus.API;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;

public class McInfListener implements Listener {

    private double multiplier = 0.0;
    private final InfConfig infConfig;
    private final Set<Player> suicideCooldown = new HashSet<>();
    private final List<Consumer<McInfPlayer>> airdropHandlers = new LinkedList<>();

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

    public McInfListener(InfConfig infConfig) {
        this.infConfig = infConfig;
        //Air drop content
        airdropHandlers.add(p -> MinigamesCore.getApi().getPlayerManager().getGamePlayer().stream().filter(g -> g.castTo(TeamPlayer.class).getTeam() instanceof HumanTeam).map(g -> g.castTo(McInfPlayer.class)).forEach(infPlayer -> {
            String kit = infPlayer.getHumanKit();
            Player player = infPlayer.getPlayer();
            McInfected.getApi().gainKit(player, kit);
            player.sendTitle("", "§a彈藥已補完", 0, 60, 20);
        }));

        airdropHandlers.add(infPlayer -> {
            Player player = infPlayer.getPlayer();
            AttributeInstance instance = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (instance == null) return;
            instance.setBaseValue(200.0);
            player.setHealth(instance.getBaseValue());
            player.setHealthScale(20.0);
            player.sendTitle("", "§a獲得: 防化服", 0, 60, 20);
        });

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
                loc = playerManager.getGamePlayer().get(Tools.randomWithRange(0, playerManager.getGamePlayer().size())).getPlayer().getLocation();
                break;
        }
        if (loc != null) player.getPlayer().teleportAsync(loc);
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
    public void onInteractAirDrop(PlayerInteractEntityEvent e) {
        if (GameTask.airdrop == null) return;
        StorageMinecart minecart = GameTask.airdrop;
        if (e.getRightClicked() != minecart) return;
        MinigamesCore.getApi().getPlayerManager().findPlayer(e.getPlayer()).ifPresent(g -> {
            McInfPlayer player = g.castTo(McInfPlayer.class);
            if (player.getStatus() != GamePlayer.Status.GAMING) return;
            e.setCancelled(true);
            if (!(player.getTeam() instanceof HumanTeam)) return;
            GameTask.airdrop.remove();
            Random random = new Random();
            this.airdropHandlers.get(random.nextInt(this.airdropHandlers.size())).accept(player);
        });
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
                g.getPlayer().sendTitle("", "§c你的防化服已失效", 0, 40, 20);
                g.getPlayer().playSound(g.getPlayer().getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1, 1);
                e.getPlayer().sendTitle("", "§4§l☣ §4感染失敗", 0, 40, 20);
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
                    MinigamesCore.getApi().getGameUtils().playSound(player, infConfig.sounds.get("Infected").split(":"));
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
            if (victim.getStatus() == GamePlayer.Status.SPECTATING) {
                VotingTask.updateHunterBossBar(MinigamesCore.getApi().getPlayerManager().getGamePlayer());
                return;
            }
            MinigamesCore.getApi().getGameUtils().playSound(player, infConfig.sounds.get("Respawn").split(":"));
            Optional.ofNullable(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).ifPresent(a -> player.setHealth(a.getBaseValue()));
            List<Location> respawn = MinigamesCore.getApi().getArenaManager().getFinalArena().getWarp("zombie");
            //McInfected.getApi().removePreviousKit(player, true);
            player.teleportAsync(respawn.get(Tools.randomWithRange(0, respawn.size() - 1)));
            //McInfected.getApi().gainKit(victim.castTo(McInfPlayer.class));
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1, false, false));
        } else if (victim.getTeam() instanceof HumanTeam) {
            McInfectedAPI api = McInfected.getApi();
            GameUtils utils = MinigamesCore.getApi().getGameUtils();
            String using = api.currentKit(e.getGamePlayer().getPlayer());
            if (using != null) {
                String hunterKit = infConfig.defaultKit.get("hunter");
                if (using.equals(hunterKit)) {
                    Bukkit.getOnlinePlayers().forEach(p -> utils.playSound(p, infConfig.soundHunter.get("Death").split(":")));
                    MinigamesCore.getApi().getPlayerManager().setSpectator(victim);
                    VotingTask.updateHunterBossBar(MinigamesCore.getApi().getPlayerManager().getGamePlayer());
                    return;
                }
            }
            victim.setTeam(McInfected.getPlugin(McInfected.class).getZombieTeam());
            McInfected.getApi().removePreviousKit(player, true);
            McInfected.getApi().gainKit(victim.castTo(McInfPlayer.class));
            YamlManager cf = McInfected.getApi().getConfigManager();
            multiplier += infConfig.damageMultiplier;
            String showMulti = new DecimalFormat("##.##").format(multiplier * 100);
            String title = cf.getPureMessage("Game.Damage-Indicator").replace("<value>", showMulti + "");
            MinigamesCore.getApi().getPlayerManager().getGamePlayer().stream().filter(g -> g.castTo(TeamPlayer.class).getTeam() instanceof HumanTeam).forEach(g -> {
                g.getPlayer().sendTitle("", title, 0, 30, 20);
                g.getPlayer().playSound(g.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            });
        }
        VotingTask.updateHunterBossBar(MinigamesCore.getApi().getPlayerManager().getGamePlayer());
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
            e.getPlayer().sendMessage(McInfected.getApi().getConfigManager().getMessage("Error.Game.Not An Infected"));
            return;
        }
        if (suicideCooldown.contains(e.getPlayer())) {
            e.getPlayer().sendMessage(McInfected.getApi().getConfigManager().getMessage("Error.Command.Suicide Not Cooled Down"));
            return;
        }
        e.getPlayer().damage(2000);
        suicideCooldown.add(e.getPlayer());
        Bukkit.getScheduler().runTaskLater(McInfected.getPlugin(McInfected.class), () -> suicideCooldown.remove(e.getPlayer()), 600L);
    }
}
