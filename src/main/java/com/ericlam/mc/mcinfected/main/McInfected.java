package com.ericlam.mc.mcinfected.main;

import com.ericlam.mc.mcinfected.McInfListener;
import com.ericlam.mc.mcinfected.api.McInfectedAPI;
import com.ericlam.mc.mcinfected.commands.InfArenaCommand;
import com.ericlam.mc.mcinfected.config.InfConfig;
import com.ericlam.mc.mcinfected.config.KitConfig;
import com.ericlam.mc.mcinfected.config.LangConfig;
import com.ericlam.mc.mcinfected.implement.ArenaConfigImpl;
import com.ericlam.mc.mcinfected.implement.McInfPlayer;
import com.ericlam.mc.mcinfected.implement.mechanic.McInfArenaMechanic;
import com.ericlam.mc.mcinfected.implement.mechanic.McInfGameStatsMechanic;
import com.ericlam.mc.mcinfected.implement.mechanic.McInfPlayerMechanic;
import com.ericlam.mc.mcinfected.implement.team.HumanTeam;
import com.ericlam.mc.mcinfected.implement.team.ZombieTeam;
import com.ericlam.mc.mcinfected.manager.AirDropManager;
import com.ericlam.mc.mcinfected.manager.HunterManager;
import com.ericlam.mc.mcinfected.manager.KitManager;
import com.ericlam.mc.mcinfected.manager.MiscManager;
import com.ericlam.mc.mcinfected.skills.InfectedSkill;
import com.ericlam.mc.mcinfected.skills.SkillListener;
import com.ericlam.mc.mcinfected.skills.SkillManager;
import com.ericlam.mc.mcinfected.skills.impl.*;
import com.ericlam.mc.mcinfected.tasks.*;
import com.ericlam.mc.minigames.core.character.GamePlayer;
import com.ericlam.mc.minigames.core.event.section.GameVotingEvent;
import com.ericlam.mc.minigames.core.game.GameState;
import com.ericlam.mc.minigames.core.game.InGameState;
import com.ericlam.mc.minigames.core.main.MinigamesCore;
import com.ericlam.mc.minigames.core.registable.Compulsory;
import com.ericlam.mc.minigames.core.registable.Voluntary;
import com.dragonite.mc.dnmc.core.builders.InventoryBuilder;
import com.dragonite.mc.dnmc.core.builders.ItemStackBuilder;
import com.dragonite.mc.dnmc.core.main.DragoniteMC;
import com.dragonite.mc.dnmc.core.managers.YamlManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.function.Consumer;

public final class McInfected extends JavaPlugin implements Listener, McInfectedAPI {

    private static McInfectedAPI api;

    private final HumanTeam humanTeam = new HumanTeam();
    private final ZombieTeam zombieTeam = new ZombieTeam();
    private final InGameState preStartState = new InGameState("preStart", null);
    private final InGameState gameEndState = new InGameState("gameEnd", null);

    private final SkillManager skillManager = new SkillManager();
    private final AirDropManager airDropManager = new AirDropManager();

    private final Map<UUID, Double> tops = new LinkedHashMap<>();

    private YamlManager configManager;
    private KitManager kitManager;
    private MiscManager miscManager;

    private HunterManager hunterManager;

    public static McInfectedAPI getApi() {
        return api;
    }

    public InGameState getGameEndState() {
        return gameEndState;
    }

    public InGameState getPreStartState() {
        return preStartState;
    }

    @Override
    public void registerSkill(String kit, InfectedSkill skill) {
        skillManager.register(kit, skill);
    }

    @Override
    public void launchSkillEffect(Player p) {
        Optional.ofNullable(miscManager).ifPresent(m -> m.setTint(p));
    }

    @Override
    public void removeSkillEffect(Player p) {
        Optional.ofNullable(miscManager).ifPresent(m -> m.removeTint(p));
    }

    @Override
    public HumanTeam getHumanTeam() {
        return humanTeam;
    }

    @Override
    public ZombieTeam getZombieTeam() {
        return zombieTeam;
    }

    @Override
    public YamlManager getConfigManager() {
        return configManager;
    }

    @Override
    public void addLeaderBoard(Player player, double wrld) {
        if (tops.containsKey(player.getUniqueId())) {
            double oldwrld = tops.get(player.getUniqueId());
            tops.put(player.getUniqueId(), oldwrld + wrld);
            return;
        }
        tops.put(player.getUniqueId(), wrld);
    }

    @Override
    public Map<UUID, Double> getLeaderBoard() {
        return tops;
    }

    @Override
    public void addAirDropHandler(Consumer<McInfPlayer> playerHandler) {
        airDropManager.addAirDropHandler(playerHandler);
    }

    @Override
    public void gainKit(McInfPlayer player) {
        kitManager.gainKit(player);
    }

    @Override
    public void gainKit(Player target, String kit) {
        kitManager.gainKit(target, kit);
    }

    @Override
    public String currentKit(Player player) {
        return kitManager.getCurrentUsing(player);
    }

    @Override
    public void removePreviousKit(Player player, boolean invClear) {
        kitManager.removeLastKit(player, invClear);
    }

    @Override
    public Inventory getKitSelector(boolean human) {
        return kitManager.getKitSelector(human);
    }

    public AirDropManager getAirDropManager() {
        return airDropManager;
    }


    public HunterManager getHunterManager() {
        return hunterManager;
    }

    @Override
    public void onEnable() {
        api = this;
        if (getServer().getPluginManager().isPluginEnabled("MinigameMiscs")) {
            this.miscManager = new MiscManager();
        }
        configManager = DragoniteMC.getAPI().getFactory().getConfigFactory(this)
                .register("config.yml", InfConfig.class)
                .register("kits.yml", KitConfig.class)
                .register("lang.yml", LangConfig.class)
                .dump();
        LangConfig msg = configManager.getConfigAs(LangConfig.class);
        kitManager = new KitManager(configManager);
        hunterManager = new HunterManager(configManager);
        Compulsory com = MinigamesCore.getRegistration().getCompulsory();
        com.registerVoteGUI(new InventoryBuilder(1, "&c地圖投票"), 0, 2, 4, 6, 8);
        com.registerArenaMechanic(new McInfArenaMechanic());
        com.registerGamePlayerHandler(new McInfPlayerMechanic());
        com.registerGameStatsHandler(new McInfGameStatsMechanic());
        InfConfig infConfig = configManager.getConfigAs(InfConfig.class);
        com.registerArenaConfig(new ArenaConfigImpl(this, configManager.getConfigAs(LangConfig.class), infConfig));
        com.registerLobbyTask(new VotingTask());
        com.registerEndTask(new PreEndTask());
        com.registerArenaCommand(new InfArenaCommand(), this);
        Voluntary vol = MinigamesCore.getRegistration().getVoluntary();
        vol.addJoinItem(7, new ItemStackBuilder(Material.PLAYER_HEAD).displayName("&a選擇 人類 職業").onInteract(e -> {
            if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
            e.setCancelled(true);
            MinigamesCore.getApi().getPlayerManager().findPlayer(e.getPlayer()).ifPresent(p -> e.getPlayer().openInventory(getKitSelector(true)));
        }).build());
        vol.addJoinItem(8, new ItemStackBuilder(Material.SKELETON_SKULL).displayName("&c選擇 生化幽靈 職業").onInteract(e -> {
            if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
            e.setCancelled(true);
            MinigamesCore.getApi().getPlayerManager().findPlayer(e.getPlayer()).ifPresent(p -> e.getPlayer().openInventory(getKitSelector(false)));
        }).build());
        vol.registerGameTask(preStartState, new InfectingTask());
        vol.registerGameTask(new InGameState("gaming", null), new GameTask());
        vol.registerGameTask(gameEndState, new GameEndTask());
        // register skill
        skillManager.register("Speeder", new SpeederSkill());
        skillManager.register("Giant", new GiantSkill());
        skillManager.register("Leaper", new LeaperSkill());
        skillManager.register("Exploder", new ExploderSkill());
        skillManager.register("Blocker", new BlockerSkill());
        skillManager.register("ChemWitch", new ChemWitchSkill());
        getServer().getPluginManager().registerEvents(new McInfListener(configManager, airDropManager, hunterManager), this);
        getServer().getPluginManager().registerEvents(this, this);
    }


    @Override
    public void onDisable() {
        airDropManager.removeAirDrop(); //防止突然關服忘了清理空投
    }

    @EventHandler
    public void onGameVoting(GameVotingEvent e) {
        getServer().getPluginManager().registerEvents(new SkillListener(skillManager, configManager.getConfigAs(InfConfig.class), hunterManager), this);
    }

    @EventHandler
    public void onCommandPreProcess(PlayerCommandPreprocessEvent e) {
        GameState state = MinigamesCore.getApi().getGameManager().getGameState();
        //if (state != GameState.PRESTART) return;
        Optional<GamePlayer> gamePlayerOptional = MinigamesCore.getApi().getPlayerManager().findPlayer(e.getPlayer());
        if (gamePlayerOptional.isEmpty()) return;
        GamePlayer gamePlayer = gamePlayerOptional.get();
        if (e.getMessage().startsWith("/human")) {
            e.setCancelled(true);
            e.getPlayer().openInventory(getKitSelector(true));
        } else if (e.getMessage().startsWith("/zombie")) {
            e.setCancelled(true);
            e.getPlayer().openInventory(getKitSelector(false));
        }
    }
}
