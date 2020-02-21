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
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class ArenaConfigImpl implements ArenaConfig {

    private final File folder;
    private final LangConfig langConfig;
    private final InfConfig infConfig;
    private final ImmutableMap<String, Integer> allowWarps;

    public ArenaConfigImpl(Plugin plugin, LangConfig langConfig, InfConfig infConfig) {
        this.folder = new File(plugin.getDataFolder(), "Arena");
        this.langConfig = langConfig;
        this.infConfig = infConfig;
        this.allowWarps = ImmutableMap.<String, Integer>builder().put("human", 5).put("zombie", 5).put("airdrop", 5).build();
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
        return allowWarps;
    }

    @Override
    public Location getLobbyLocation() {
        return infConfig.lobby;
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
            infConfig.lobby = location;
            try {
                infConfig.save();
                return true;
            } catch (IOException e) {
                return false;
            }
        });
    }
}
