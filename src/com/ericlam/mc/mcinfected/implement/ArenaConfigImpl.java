package com.ericlam.mc.mcinfected.implement;

import com.ericlam.mc.mcinfected.config.InfConfig;
import com.ericlam.mc.mcinfected.config.LangConfig;
import com.ericlam.mc.minigames.core.arena.ArenaConfig;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.concurrent.CompletableFuture;

public class ArenaConfigImpl implements ArenaConfig {

    private final Plugin plugin;
    private final LangConfig langConfig;
    private final InfConfig infConfig;

    public ArenaConfigImpl(Plugin plugin, LangConfig langConfig, InfConfig infConfig) {
        this.plugin = plugin;
        this.langConfig = langConfig;
        this.infConfig = infConfig;
    }

    @Override
    public File getArenaFolder() {
        return new File(plugin.getDataFolder(), "Arena");
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
        return ImmutableMap.<String, Integer>builder().put("human", 5).put("zombie", 5).put("airdrop", 5).build();
    }

    @Override
    public Location getLobbyLocation() {
        return infConfig.lobby != null ? Location.deserialize(infConfig.lobby) : null;
    }

    @Override
    public String getFallBackServer() {
        return infConfig.fallbackServer;
    }

    @Override
    public String getGamePrefix() {
        return langConfig.getPrefix();
    }

    @Override
    public CompletableFuture<Boolean> setLobbyLocation(Location location) {
        return CompletableFuture.supplyAsync(() -> {
            infConfig.getConfiguration().createSection("lobby", location.serialize());
            return infConfig.save();
        });
    }
}
