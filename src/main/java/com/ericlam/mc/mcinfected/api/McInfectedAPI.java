package com.ericlam.mc.mcinfected.api;

import com.ericlam.mc.mcinfected.implement.McInfPlayer;
import com.ericlam.mc.mcinfected.implement.team.HumanTeam;
import com.ericlam.mc.mcinfected.implement.team.ZombieTeam;
import com.ericlam.mc.mcinfected.skills.InfectedSkill;
import com.dragonite.mc.dnmc.core.managers.YamlManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public interface McInfectedAPI {

    void registerSkill(String kit, InfectedSkill skill);

    void launchSkillEffect(Player p);

    void removeSkillEffect(Player p);

    HumanTeam getHumanTeam();

    ZombieTeam getZombieTeam();

    YamlManager getConfigManager();

    void addAirDropHandler(Consumer<McInfPlayer> playerHandler);

    void gainKit(McInfPlayer player);

    void gainKit(Player target, String kit);

    String currentKit(Player player);

    void removePreviousKit(Player player, boolean invClear);

    Inventory getKitSelector(boolean human);

    void addLeaderBoard(Player player, double wrld);

    Map<UUID, Double> getLeaderBoard();


}
