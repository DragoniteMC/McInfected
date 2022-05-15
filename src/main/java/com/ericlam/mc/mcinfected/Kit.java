package com.ericlam.mc.mcinfected;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class Kit {

    private final String displayName;
    private final ItemStack[] armors;
    private final ItemStack[] inventory;
    private final List<String> description;
    private final ItemStack icon;
    private final List<PotionEffect> potionEffects;

    private final String permission;
    private EntityType disguise;

    public Kit(String displayName, ItemStack[] armors, ItemStack[] inventory, List<String> description, ItemStack icon, List<PotionEffect> potionEffects, @Nullable String permission) {
        this.displayName = displayName;
        this.armors = armors;
        this.inventory = inventory;
        this.description = description.stream().map(e -> ChatColor.translateAlternateColorCodes('&', e)).collect(Collectors.toList());
        this.icon = icon;
        this.potionEffects = potionEffects;
        this.disguise = EntityType.PLAYER;
        this.permission = permission;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ItemStack[] getArmors() {
        return armors;
    }

    public ItemStack[] getInventory() {
        return inventory;
    }

    public List<String> getDescription() {
        return description;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public List<PotionEffect> getPotionEffects() {
        return potionEffects;
    }

    public EntityType getDisguise() {
        return disguise;
    }

    public void setDisguise(EntityType disguise) {
        this.disguise = disguise;
    }


    @Nullable
    public String getPermission() {
        return permission;
    }
}
