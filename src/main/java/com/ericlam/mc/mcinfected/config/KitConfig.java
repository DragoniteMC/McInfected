package com.ericlam.mc.mcinfected.config;

import com.dragonite.mc.dnmc.core.config.yaml.Configuration;
import com.dragonite.mc.dnmc.core.config.yaml.Resource;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.List;
import java.util.Map;

@Resource(locate = "kits.yml")
public class KitConfig extends Configuration {
    public Map<String, Kit> Human;
    public Map<String, Kit> Infected;

    public static class Kit {
        public List<Material> Armor;
        public Inventory Inventory;
        public String Display;
        public List<String> Description;
        public String Icon;
        public List<String> Potions;
        public EntityType Disguise;
        
        public String permission;
    }

    public static class Inventory {
        public List<String> Crackshot;
        public List<String> Normal;
    }
}
