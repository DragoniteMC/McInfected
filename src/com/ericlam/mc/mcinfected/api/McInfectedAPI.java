package com.ericlam.mc.mcinfected.api;

import com.ericlam.mc.mcinfected.implement.McInfPlayer;
import com.ericlam.mc.mcinfected.implement.team.HumanTeam;
import com.ericlam.mc.mcinfected.implement.team.ZombieTeam;
import com.ericlam.mc.mcinfected.skills.InfectedSkill;
import com.ericlam.mc.minigames.core.character.GamePlayer;
import com.hypernite.mc.hnmc.core.managers.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public interface McInfectedAPI {

    void registerSkill(String kit, InfectedSkill skill);

    void launchSkillEffect(Player p);

    void removeSkillEffect(Player p);

    HumanTeam getHumanTeam();

    ZombieTeam getZombieTeam();

    ConfigManager getConfigManager();

    void gainKit(McInfPlayer player);

    void gainKit(Player target, String kit);

    String currentKit(Player player);

    void removePreviousKit(Player player, boolean invClear);

    Inventory getKitSelector(GamePlayer player, boolean human);


}
