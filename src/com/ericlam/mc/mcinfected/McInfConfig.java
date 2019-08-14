package com.ericlam.mc.mcinfected;

import com.ericlam.mc.minigames.core.arena.ArenaConfig;
import com.google.common.collect.ImmutableMap;
import com.hypernite.mc.hnmc.core.config.ConfigSetter;
import com.hypernite.mc.hnmc.core.config.Extract;
import com.hypernite.mc.hnmc.core.utils.converters.LocationSerializer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class McInfConfig extends ConfigSetter implements ArenaConfig {

    @Extract private long gameTime;
    @Extract private long infectingTime;
    @Extract private long votingTime;
    @Extract private int maxRound;

    @Extract private float alphaPercent;
    @Extract private int autoStart;

    @Extract private String humanDefault;
    @Extract private String zombieDefault;

    @Extract private long compassTime;

    @Extract private String[] voteCount;
    @Extract private String[] voteFinal;

    @Extract private String[] infectCount;
    @Extract private String[] infectFinal;

    @Extract private String[] gameCount;
    @Extract private String[] gameFinal;

    @Extract private String[] compassGain;
    @Extract private String[] infected;
    @Extract private String[] respawn;

    private ConfigurationSection lobbySection;

    private final File folder;
    private String fallbackSer;
    private String prefix;

    private FileConfiguration config;
    private final File configFile;

    public McInfConfig(Plugin plugin) {
        super(plugin, "config.yml", "kits.yml", "lang.yml");
        this.folder = new File(plugin.getDataFolder(), "Arena");
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
    }

    @Override
    public void loadConfig(Map<String, FileConfiguration> map) {
        config = map.get("config.yml");
        this.prefix = map.get("lang.yml").getString("prefix");

        this.gameTime = config.getLong("time.game");
        this.infectingTime = config.getLong("time.infecting");
        this.votingTime = config.getLong("time.voting");
        this.maxRound = config.getInt("time.max-round");

        this.alphaPercent = (float)config.getDouble("game.alpha-percent");
        this.autoStart = config.getInt("game.auto-start");

        this.humanDefault = config.getString("default-kit.human");
        this.zombieDefault = config.getString("default-kit.zombie");

        this.compassTime = config.getLong("compass-give-in-sec");

        this.voteCount = config.getString("Sounds.Voting.Countdown").split(":");
        this.voteFinal = config.getString("Sounds.Voting.Final").split(":");

        this.infectCount = config.getString("Sounds.Infecting.Countdown").split(":");
        this.infectFinal = config.getString("Sounds.Infecting.Final").split(":");

        this.gameCount = config.getString("Sounds.Game.Countdown").split(":");
        this.gameFinal = config.getString("Sounds.Game.Final").split(":");

        this.compassGain = config.getString("Sounds.Compass").split(":");
        this.infected = config.getString("Sounds.Infected").split(":");
        this.respawn = config.getString("Sounds.Respawn").split(":");

        this.lobbySection = config.getConfigurationSection("lobby");
        this.fallbackSer = config.getString("fallback-server");
    }

    @Override
    public File getArenaFolder() {
        return folder;
    }

    @Override
    public int getMaxLoadArena() {
        return 5;
    }

    @Override
    public void setExtraWorldSetting(@Nonnull World world) {
    }

    @Override
    public ImmutableMap<String, Integer> getAllowWarps() {
        return ImmutableMap.<String, Integer>builder().put("human", 5).put("zombie", 5).build();
    }

    @Override
    public Location getLobbyLocation() {
        return lobbySection == null ? null : LocationSerializer.mapToLocation(lobbySection).orElse(null);
    }

    @Override
    public String getFallBackServer() {
        return fallbackSer;
    }

    @Override
    public String getGamePrefix() {
        return prefix;
    }

    @Override
    public CompletableFuture<Boolean> setLobbyLocation(Location location) {
        return CompletableFuture.supplyAsync(()->{
            config.createSection("lobby", LocationSerializer.locToConfigSection(location));
            try {
                config.save(configFile);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        });
    }
}
