package com.ericlam.mc.mcinfected.implement;

import com.ericlam.mc.minigames.core.arena.CreateArena;
import net.milkbowl.vault.chat.Chat;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class McInfArena implements CreateArena {
    private String arena;
    private String displayName;
    private String author;
    private World world;
    private Map<String, List<Location>> warp;
    private List<String> description;
    private boolean changed;

    public McInfArena(String arena, String displayName, String author, World world, Map<String, List<Location>> warp, List<String> description) {
        this.arena = arena;
        this.displayName = displayName;
        this.author = author;
        this.world = world;
        this.warp = warp;
        this.description = description;
        this.changed = false;
    }

    public McInfArena(String arena, Player player){
        this(arena, arena, player.getDisplayName(), player.getWorld(), new HashMap<>(), new ArrayList<>());
    }

    @Override
    public void setAuthor(String s) {
        this.author = s;
    }

    @Override
    public void setWorld(World world) {
        this.world = world;
    }

    @Override
    public void setArenaName(String s) {
        this.arena = s;
    }

    @Override
    public void setDisplayName(String s) {
        this.displayName = ChatColor.translateAlternateColorCodes('&', s);
    }

    @Override
    public void setLocationMap(Map<String, List<Location>> map) {
        this.warp = map;
    }

    @Override
    public boolean isChanged() {
        return changed;
    }

    @Override
    public void setChanged(Boolean aBoolean) {
        this.changed = aBoolean;
    }

    @Override
    public boolean isSetupCompleted() {
        boolean human = Optional.ofNullable(warp.get("human")).map(w->w.size() > 1).orElse(false);
        boolean zombie = Optional.ofNullable(warp.get("zombie")).map(w->w.size() > 1).orElse(false);
        return human && zombie;
    }

    @Override
    public String getAuthor() {
        return author;
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public String getArenaName() {
        return arena;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public Map<String, List<Location>> getLocationsMap() {
        return warp;
    }

    @Override
    public List<String> getDescription() {
        return description;
    }

    @Override
    public String[] getInfo() {
        String[] info =  new String[]{
                "§7場地名稱:§f ".concat(arena),
                "§7顯示名稱:§f ".concat(displayName),
                "§7場地作者:§f ".concat(author),
                "§7所在世界:§f ".concat(world.getName()),
                "§7地標:§f ".concat(warp.entrySet().stream().map(e->e.getKey()+"("+e.getValue().size()+")").collect(Collectors.joining(", "))),
                "§7場地描述:§f "
        };
        String[] desp = description.stream().map(e->" ".repeat(5).concat(ChatColor.translateAlternateColorCodes('&',e))).toArray(String[]::new);
        return (String[]) ArrayUtils.addAll(info, desp);
    }
}
