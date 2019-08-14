package com.ericlam.mc.mcinfected.main;

import com.ericlam.mc.mcinfected.McInfConfig;
import com.ericlam.mc.mcinfected.McInfListener;
import com.ericlam.mc.mcinfected.implement.McInfPlayer;
import com.ericlam.mc.mcinfected.implement.mechanic.McInfArenaMechanic;
import com.ericlam.mc.mcinfected.implement.mechanic.McInfGameStatsMechanic;
import com.ericlam.mc.mcinfected.implement.mechanic.McInfPlayerMechanic;
import com.ericlam.mc.mcinfected.implement.team.HumanTeam;
import com.ericlam.mc.mcinfected.implement.team.ZombieTeam;
import com.ericlam.mc.mcinfected.manager.KitManager;
import com.ericlam.mc.minigames.core.character.TeamPlayer;
import com.ericlam.mc.minigames.core.event.section.GamePreStartEvent;
import com.ericlam.mc.minigames.core.game.InGameState;
import com.ericlam.mc.minigames.core.main.MinigamesCore;
import com.ericlam.mc.minigames.core.registable.Compulsory;
import com.hypernite.mc.hnmc.core.builders.InventoryBuilder;
import com.hypernite.mc.hnmc.core.main.HyperNiteMC;
import com.hypernite.mc.hnmc.core.managers.ConfigManager;
import com.hypernite.mc.hnmc.core.misc.commands.AdvCommandNodeBuilder;
import com.hypernite.mc.hnmc.core.misc.commands.CommandNode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class McInfected extends JavaPlugin implements Listener {

    private static ConfigManager configManager;
    private static KitManager kitManager;
    private HumanTeam humanTeam = new HumanTeam();
    private ZombieTeam zombieTeam = new ZombieTeam();
    private InGameState preStartState = new InGameState("preStart", null);

    public InGameState getPreStartState() {
        return preStartState;
    }

    public HumanTeam getHumanTeam() {
        return humanTeam;
    }

    public ZombieTeam getZombieTeam() {
        return zombieTeam;
    }

    public static ConfigManager config(){
        return configManager;
    }

    public static KitManager kitManager(){
        return kitManager;
    }

    @Override
    public void onEnable() {
        McInfConfig config = new McInfConfig(this);
        configManager = HyperNiteMC.getAPI().registerConfig(config);
        configManager.setMsgConfig("lang.yml");
        kitManager = new KitManager(configManager.getConfig("kits.yml"));
        Compulsory com = MinigamesCore.getRegistration().getCompulsory();
        com.registerVoteGUI(new InventoryBuilder(1, "&c地圖投票"), 0, 2, 4, 6, 8);
        com.registerArenaMechanic(new McInfArenaMechanic());
        com.registerGamePlayerHandler(new McInfPlayerMechanic());
        com.registerGameStatsHandler(new McInfGameStatsMechanic());

        CommandNode testCommand = new AdvCommandNodeBuilder<Player>("inf-test").description("no").placeholder("<kit>")
                .execute((player, list) -> {
                    String kit = list.get(0);
                    McInfPlayer infPlayer = new McInfPlayer(player);
                    infPlayer.setTeam(zombieTeam);
                    infPlayer.setZombieKit(kit);
                    try{
                        kitManager.gainKit(infPlayer);
                    }catch (IllegalStateException e){
                        player.sendMessage(e.getMessage());
                    }
                    return true;
                }).build();

        HyperNiteMC.getAPI().getCommandRegister().registerCommand(this, testCommand);
        getServer().getPluginManager().registerEvents(new McInfListener(), this);
        getServer().getPluginManager().registerEvents(this, this);
    }


    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventHandler
    public void onPreStart(GamePreStartEvent e) {
        e.getGamingPlayer().forEach(p -> p.castTo(TeamPlayer.class).setTeam(humanTeam));
    }
}
