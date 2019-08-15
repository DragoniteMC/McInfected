package com.ericlam.mc.mcinfected.skills;

import com.ericlam.mc.mcinfected.main.McInfected;
import com.ericlam.mc.minigames.core.main.MinigamesCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SkillManager {

    private final Map<String, InfectedSkill> kitSkillMap = new HashMap<>();
    private final Map<Player, Boolean> cooldownMap = new ConcurrentHashMap<>();
    private final Map<Player, InfectedSkill> skillUsing = new HashMap<>();

    public void register(String kit, InfectedSkill skill) {
        this.kitSkillMap.put(kit, skill);
    }

    public void launchSkill(Player player) {
        String kit = McInfected.getApi().currentKit(player);
        if (kit == null) return;
        boolean cooldown = cooldownMap.getOrDefault(player, false);
        if (cooldown) return;
        InfectedSkill skill = this.kitSkillMap.get(kit);
        if (skill == null) return;
        if (skillUsing.containsKey(player)) return;
        skillUsing.put(player, skill);
        skill.execute(player);
        McInfected.getApi().getConfigManager().getData("skillLaunch", String[].class).ifPresent(s -> MinigamesCore.getApi().getGameUtils().playSound(player, s));
        McInfected.getApi().launchSkillEffect(player);
        Plugin plugin = McInfected.getPlugin(McInfected.class);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!skillUsing.containsKey(player)) return;
            skill.revert(player);
            skillUsing.remove(player);
            McInfected.getApi().getConfigManager().getData("skillCooldown", String[].class).ifPresent(s -> MinigamesCore.getApi().getGameUtils().playSound(player, s));
            McInfected.getApi().removeSkillEffect(player);
            new SkillCooldownRunnable(player, skill.getCoolDown(), cooldownMap).runTaskTimer(plugin, 0L, 2L);
        }, skill.getKeepingTime() * 20L);
    }

    void clearSkill() {
        skillUsing.forEach((k, v) -> {
            v.revert(k);
            McInfected.getApi().getConfigManager().getData("skillCooldown", String[].class).ifPresent(s -> MinigamesCore.getApi().getGameUtils().playSound(k, s));
            McInfected.getApi().removeSkillEffect(k);
        });
        skillUsing.clear();
    }
}
