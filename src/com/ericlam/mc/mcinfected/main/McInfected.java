package com.ericlam.mc.mcinfected.main;

import com.ericlam.mc.mcinfected.Kit;
import com.ericlam.mc.mcinfected.McInfConfig;
import com.ericlam.mc.mcinfected.McInfListener;
import com.ericlam.mc.mcinfected.api.McInfectedAPI;
import com.ericlam.mc.mcinfected.commands.InfArenaCommand;
import com.ericlam.mc.mcinfected.implement.McInfPlayer;
import com.ericlam.mc.mcinfected.implement.mechanic.McInfArenaMechanic;
import com.ericlam.mc.mcinfected.implement.mechanic.McInfGameStatsMechanic;
import com.ericlam.mc.mcinfected.implement.mechanic.McInfPlayerMechanic;
import com.ericlam.mc.mcinfected.implement.team.HumanTeam;
import com.ericlam.mc.mcinfected.implement.team.ZombieTeam;
import com.ericlam.mc.mcinfected.manager.KitManager;
import com.ericlam.mc.mcinfected.skills.InfectedSkill;
import com.ericlam.mc.mcinfected.skills.SkillListener;
import com.ericlam.mc.mcinfected.skills.SkillManager;
import com.ericlam.mc.mcinfected.skills.impl.*;
import com.ericlam.mc.mcinfected.tasks.*;
import com.ericlam.mc.minigames.core.character.GamePlayer;
import com.ericlam.mc.minigames.core.game.GameState;
import com.ericlam.mc.minigames.core.game.InGameState;
import com.ericlam.mc.minigames.core.main.MinigamesCore;
import com.ericlam.mc.minigames.core.registable.Compulsory;
import com.ericlam.mc.minigames.core.registable.Voluntary;
import com.hypernite.mc.hnmc.core.builders.InventoryBuilder;
import com.hypernite.mc.hnmc.core.builders.ItemStackBuilder;
import com.hypernite.mc.hnmc.core.listener.ItemEventAction;
import com.hypernite.mc.hnmc.core.main.HyperNiteMC;
import com.hypernite.mc.hnmc.core.managers.ConfigManager;
import fr.mrsheepsheep.tinthealth.THAPI;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class McInfected extends JavaPlugin implements Listener, McInfectedAPI {

    private static McInfectedAPI api;
    private final Map<ItemStack, ItemEventAction<InventoryClickEvent>> clickMap = new ConcurrentHashMap<>();
    private final Map<GamePlayer, Inventory> zombieInv = new ConcurrentHashMap<>();
    private HumanTeam humanTeam = new HumanTeam();
    private ZombieTeam zombieTeam = new ZombieTeam();
    private InGameState preStartState = new InGameState("preStart", null);
    private final Map<GamePlayer, Inventory> humanInv = new ConcurrentHashMap<>();
    private ConfigManager configManager;
    private KitManager kitManager;
    private SkillManager skillManager = new SkillManager();
    private InGameState gameEndState = new InGameState("gameEnd", null);
    private boolean tintEnabled;

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
        if (!tintEnabled) return;
        THAPI.setTint(p, 100);
    }

    @Override
    public void removeSkillEffect(Player p) {
        if (!tintEnabled) return;
        THAPI.removeTint(p);
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
    public ConfigManager getConfigManager() {
        return configManager;
    }

    @Override
    public void gainKit(McInfPlayer player) {
        kitManager.gainKit(player);
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
    public Inventory getKitSelector(GamePlayer player, boolean human) {
        Map<String, Kit> kits;
        if (human) {
            if (humanInv.containsKey(player)) return humanInv.get(player);
            kits = kitManager.getKitMap().entrySet().stream().filter(e -> e.getValue().getDisguise() == EntityType.PLAYER).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } else {
            if (zombieInv.containsKey(player)) return zombieInv.get(player);
            kits = kitManager.getKitMap().entrySet().stream().filter(e -> e.getValue().getDisguise() != EntityType.PLAYER).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        McInfPlayer infPlayer = player.castTo(McInfPlayer.class);
        int row = (int) Math.ceil(kits.size() / 9);
        InventoryBuilder builder = new InventoryBuilder(row == 0 ? 1 : row, "&9選擇職業");
        kits.forEach((k, v) -> {
            v.getDescription().add(0, configManager.getPureMessage("Command.Kit.Choose").replace("<kit>", v.getDisplayName()));
            v.getDescription().add(1, " ");
            ItemStack stack = new ItemStackBuilder(v.getIcon()).displayName(v.getDisplayName()).lore(v.getDescription()).build();
            this.clickMap.put(stack, e -> {
                e.setCancelled(true);
                Player clicked = (Player) e.getWhoClicked();
                if (human) infPlayer.setHumanKit(k);
                else infPlayer.setZombieKit(k);
                clicked.sendMessage(configManager.getMessage("Command.Kit.Chosen").replace("<team>", human ? "人類" : "生化幽靈").replace("<kit>", v.getDisplayName()));
            });
            builder.item(stack);
        });
        Inventory inv = builder.build();
        if (human) this.humanInv.put(player, inv);
        else this.zombieInv.put(player, inv);
        return inv;
    }

    @Override
    public void onEnable() {
        api = this;
        tintEnabled = getServer().getPluginManager().getPlugin("TintHealth") != null;
        McInfConfig config = new McInfConfig(this);
        configManager = HyperNiteMC.getAPI().registerConfig(config);
        configManager.setMsgConfig("lang.yml");
        kitManager = new KitManager(configManager.getConfig("kits.yml"));
        Compulsory com = MinigamesCore.getRegistration().getCompulsory();
        com.registerVoteGUI(new InventoryBuilder(1, "&c地圖投票"), 0, 2, 4, 6, 8);
        com.registerArenaMechanic(new McInfArenaMechanic());
        com.registerGamePlayerHandler(new McInfPlayerMechanic());
        com.registerGameStatsHandler(new McInfGameStatsMechanic());
        com.registerArenaConfig(config);
        com.registerLobbyTask(new VotingTask());
        com.registerEndTask(new PreEndTask());
        com.registerArenaCommand(new InfArenaCommand(), this);
        Voluntary vol = MinigamesCore.getRegistration().getVoluntary();
        vol.addJoinItem(7, new ItemStackBuilder(Material.PLAYER_HEAD).displayName("&a選擇 人類 職業").onInteract(e -> {
            if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
            e.setCancelled(true);
            MinigamesCore.getApi().getPlayerManager().findPlayer(e.getPlayer()).ifPresent(p -> e.getPlayer().openInventory(getKitSelector(p, true)));
        }).build());
        vol.addJoinItem(8, new ItemStackBuilder(Material.SKELETON_SKULL).displayName("&c選擇 生化幽靈 職業").onInteract(e -> {
            if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
            e.setCancelled(true);
            MinigamesCore.getApi().getPlayerManager().findPlayer(e.getPlayer()).ifPresent(p -> e.getPlayer().openInventory(getKitSelector(p, false)));
        }).build());
        vol.registerGameTask(preStartState, new InfectingTask());
        vol.registerGameTask(new InGameState("gaming", null), new GameTask());
        vol.registerGameTask(gameEndState, new GameEndTask());
        skillManager.register("Speeder", new SpeederSkill());
        skillManager.register("Giant", new GiantSkill());
        skillManager.register("Leaper", new LeaperSkill());
        skillManager.register("Exploder", new ExploderSkill());
        skillManager.register("Blocker", new BlockerSkill());
        getServer().getPluginManager().registerEvents(new McInfListener(), this);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new SkillListener(skillManager), this);
    }


    @Override
    public void onDisable() {
        super.onDisable();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getCurrentItem() == null) return;
        Optional.ofNullable(clickMap.get(e.getCurrentItem())).ifPresent(ex -> ex.onEvent(e));
    }

    @EventHandler
    public void onCommandPreProcess(PlayerCommandPreprocessEvent e) {
        if (MinigamesCore.getApi().getGameManager().getGameState() != GameState.PRESTART) return;
        Optional<GamePlayer> gamePlayerOptional = MinigamesCore.getApi().getPlayerManager().findPlayer(e.getPlayer());
        if (gamePlayerOptional.isEmpty()) return;
        GamePlayer gamePlayer = gamePlayerOptional.get();
        if (e.getMessage().startsWith("/human")) {
            e.setCancelled(true);
            e.getPlayer().openInventory(getKitSelector(gamePlayer, true));
        } else if (e.getMessage().startsWith("/zombie")) {
            e.setCancelled(true);
            e.getPlayer().openInventory(getKitSelector(gamePlayer, false));
        }
    }
}
