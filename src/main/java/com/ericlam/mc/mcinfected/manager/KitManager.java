package com.ericlam.mc.mcinfected.manager;

import com.ericlam.mc.mcinfected.Kit;
import com.ericlam.mc.mcinfected.config.InfConfig;
import com.ericlam.mc.mcinfected.config.KitConfig;
import com.ericlam.mc.mcinfected.config.LangConfig;
import com.ericlam.mc.mcinfected.implement.McInfPlayer;
import com.ericlam.mc.mcinfected.implement.team.HumanTeam;
import com.ericlam.mc.mcinfected.main.McInfected;
import com.ericlam.mc.minigames.core.main.MinigamesCore;
import com.dragonite.mc.dnmc.core.builders.InventoryBuilder;
import com.dragonite.mc.dnmc.core.builders.ItemStackBuilder;
import com.dragonite.mc.dnmc.core.managers.YamlManager;
import com.shampaggon.crackshot.CSUtility;
import me.DeeCaaD.CrackShotPlus.API;
import me.DeeCaaD.CrackShotPlus.CSPapi;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class KitManager {
    private final Map<String, Kit> humanKitMap = new HashMap<>();
    private final Map<String, Kit> zombieKitMap = new HashMap<>();
    private final Map<OfflinePlayer, String> currentKit = new ConcurrentHashMap<>();
    private final YamlManager configManager;
    private Inventory humanKitSelector;
    private final CSUtility csUtility = API.getCSUtility();
    private Inventory zombieKitSelector;

    public KitManager(YamlManager configManager) {
        this.configManager = configManager;
        var kitConfig = configManager.getConfigAs(KitConfig.class);
        this.loadKit(humanKitMap, kitConfig.Human);
        this.loadKit(zombieKitMap, kitConfig.Infected);
    }

    private <T> List<T> nonNull(List<T> t) {
        return Optional.ofNullable(t).orElse(List.of());
    }

    public Inventory getKitSelector(boolean human) {
        if (human && humanKitSelector != null) return humanKitSelector;
        else if (!human && zombieKitSelector != null) return zombieKitSelector;
        var kits = human ? humanKitMap : zombieKitMap;
        var msg = configManager.getConfigAs(LangConfig.class);
        int row = (int) Math.ceil((double) kits.size() / 9);
        InventoryBuilder builder = new InventoryBuilder(row == 0 ? 1 : row, "&9選擇職業");
        kits.forEach((k, v) -> {
            if (k.equals(configManager.getConfigAs(InfConfig.class).defaultKit.get("hunter"))) return;
            var desc = new LinkedList<>(v.getDescription());
            desc.add(0, msg.getPure("Command.Kit.Choose").replace("<kit>", v.getDisplayName()));
            desc.add(1, " ");
            ItemStack stack = new ItemStackBuilder(v.getIcon()).displayName(v.getDisplayName()).lore(desc)
                    .onClick(e -> {
                        e.setCancelled(true);
                        Player clicked = (Player) e.getWhoClicked();
                        if (v.getPermission() != null && !clicked.hasPermission(v.getPermission())) {
                            clicked.sendMessage(msg.get("Command.Kit.Locked").replace("<kit>", v.getDisplayName()));
                            return;
                        }
                        MinigamesCore.getApi().getPlayerManager().findPlayer(clicked).ifPresent(player1 -> {
                            McInfPlayer infPlayer = player1.castTo(McInfPlayer.class);
                            if (human) infPlayer.setHumanKit(k);
                            else infPlayer.setZombieKit(k);
                            clicked.sendMessage(msg.get("Command.Kit.Chosen").replace("<team>", human ? "人類" : "生化幽靈").replace("<kit>", v.getDisplayName()));
                        });

                    }).build();
            builder.item(stack);
        });
        Inventory inv = builder.build();
        if (human) {
            this.humanKitSelector = inv;
        } else {
            this.zombieKitSelector = inv;
        }
        return getKitSelector(human);
    }

    private void loadKit(Map<String, Kit> toInsert, Map<String, KitConfig.Kit> kitMap) {
        if (kitMap.isEmpty()) return;
        for (var en : kitMap.entrySet()) {
            String kitName = en.getKey();
            KitConfig.Kit kit = en.getValue();
            List<Material> armor = nonNull(kit.Armor);
            String display = kit.Display;
            List<String> csItems = nonNull(kit.Inventory.Crackshot);
            List<String> items = nonNull(kit.Inventory.Normal);
            List<String> description = nonNull(kit.Description).stream().map(e -> ChatColor.translateAlternateColorCodes('&', e)).collect(Collectors.toList());
            String icon = kit.Icon;
            EntityType disguise = kit.Disguise;
            List<String> potions = nonNull(kit.Potions);
            List<ItemStack> stacks = csItems.stream().map(s -> s.split(":")).map(arr -> {
                ItemStack stack = csUtility.generateWeapon(arr[0]);
                stack = CSPapi.updateItemStackFeaturesNonPlayer(arr[0], stack);
                if (stack != null && arr.length > 1) {
                    stack.setAmount(Integer.parseInt(arr[1]));
                }
                return stack;
            }).filter(Objects::nonNull).collect(Collectors.toList());
            stacks.addAll(items.stream().map(s -> s.split(":")).map(arr -> {
                Material material = Material.getMaterial(arr[0]);
                if (material == null) return null;
                ItemStack stack = new ItemStack(material);
                if (arr.length > 1) {
                    stack.setAmount(Integer.parseInt(arr[1]));
                }
                return stack;
            }).filter(Objects::nonNull).collect(Collectors.toList()));
            ItemStack[] armors = armor.size() == 0 ? null : armor.stream().map(s -> s == null ? Material.AIR : s).map(ItemStack::new).toArray(ItemStack[]::new);
            ItemStack iconItem = Optional.ofNullable(csUtility.generateWeapon(icon)).map(w -> CSPapi.updateItemStackFeaturesNonPlayer(icon, w)).orElseGet(() -> new ItemStack(Material.valueOf(icon)));
            List<PotionEffect> potionsEffect = potions.stream().map(s -> s.split(":")).filter(s -> s.length >= 3).map(s -> {
                PotionEffectType type = PotionEffectType.getByName(s[0]);
                if (type == null) return null;
                int dur = Integer.parseInt(s[1]);
                int amp = Integer.parseInt(s[2]);
                return new PotionEffect(type, dur * 20, amp, false, false);
            }).filter(Objects::nonNull).collect(Collectors.toList());
            Kit kits = new Kit(display == null ? kitName : ChatColor.translateAlternateColorCodes('&', display),
                    armors,
                    stacks.toArray(ItemStack[]::new),
                    description,
                    iconItem,
                    potionsEffect,
                    kit.permission
            );
            if (disguise != null) {
                try {
                    kits.setDisguise(disguise);
                } catch (IllegalArgumentException ignored) {
                }
            }
            toInsert.put(kitName, kits);
        }
    }

    public void removeLastKit(Player target, boolean invClear) {
        if (invClear) target.getInventory().clear();
        target.getActivePotionEffects().forEach(e -> target.removePotionEffect(e.getType()));
        target.setFireTicks(0);
        target.setGlowing(false);
        target.setFoodLevel(20);
        DisguiseAPI.undisguiseToAll(target);
        this.currentKit.remove(target);
    }

    public void gainKit(Player target, String kit) {
        var kitMap = new HashMap<>(humanKitMap);
        kitMap.putAll(zombieKitMap);
        Kit mcinfKit = kitMap.get(kit);
        if (mcinfKit == null) {
            var warning = "There are no ".concat(kit).concat(" in kitMap");
            target.sendMessage(warning);
            McInfected.getProvidingPlugin(McInfected.class).getLogger().warning(warning);
            return;
        }
        PlayerInventory playerInventory = target.getInventory();
        //clear previous
        removeLastKit(target, true);
        //insert current
        playerInventory.setArmorContents(mcinfKit.getArmors());
        playerInventory.addItem(mcinfKit.getInventory());
        target.addPotionEffects(mcinfKit.getPotionEffects());
        this.currentKit.put(target, kit);
        if (mcinfKit.getDisguise() == EntityType.PLAYER) return;
        Disguise disguise = new MobDisguise(DisguiseType.getType(mcinfKit.getDisguise()));
        DisguiseAPI.setViewDisguiseToggled(target, false);
        DisguiseAPI.disguiseToAll(target, disguise);
    }

    public void gainKit(McInfPlayer player) {
        String kit = player.getTeam() instanceof HumanTeam ? player.getHumanKit() : player.getZombieKit();
        this.gainKit(player.getPlayer(), kit);
        var msg = McInfected.getApi().getConfigManager().getConfigAs(LangConfig.class);
        if (player.getTeam() instanceof HumanTeam) {
            player.getPlayer().sendMessage(msg.get("Picture.Human.To Win"));
            Title.Times time = Title.Times.times(Duration.ofSeconds(0), Duration.ofSeconds(3), Duration.ofSeconds(1));
            Title t = Title.title(Component.empty(), Component.text(msg.getPure("Picture.Human.You")), time);
            player.getPlayer().showTitle(t);
        } else {
            player.getPlayer().sendMessage(msg.get("Picture.Infected.To Win"));
            Title.Times time = Title.Times.times(Duration.ofSeconds(0), Duration.ofSeconds(3), Duration.ofSeconds(1));
            Title t = Title.title(Component.empty(), Component.text(msg.getPure("Picture.Infected.You")), time);
            player.getPlayer().showTitle(t);
        }
    }

    @Nullable
    public String getCurrentUsing(Player player) {
        return currentKit.get(player);
    }
}
