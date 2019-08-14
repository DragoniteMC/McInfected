package com.ericlam.mc.mcinfected.implement.mechanic;

import com.ericlam.mc.mcinfected.implement.McInfArena;
import com.ericlam.mc.minigames.core.arena.Arena;
import com.ericlam.mc.minigames.core.arena.ArenaMechanic;
import com.ericlam.mc.minigames.core.arena.CreateArena;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class McInfArenaMechanic implements ArenaMechanic {

    @Override
    public CreateArena loadCreateArena(FileConfiguration fileConfiguration, Arena arena) {
        return new McInfArena(arena.getArenaName(), arena.getDisplayName(), arena.getAuthor(), arena.getWorld(), arena.getLocationsMap(), arena.getDescription());
    }

    @Override
    public CreateArena createArena(@Nonnull String s, @Nonnull Player player) {
        return new McInfArena(s, player);
    }

    @Override
    public void saveExtraArenaSetting(FileConfiguration fileConfiguration, Arena arena) {
        //
    }
}
