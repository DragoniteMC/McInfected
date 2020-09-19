package com.ericlam.mc.mcinfected.manager;

import com.ericlam.mc.mcinfected.implement.McInfPlayer;
import com.ericlam.mc.mcinfected.implement.team.HumanTeam;
import com.ericlam.mc.minigames.core.character.GamePlayer;
import com.ericlam.mc.minigames.core.main.MinigamesCore;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class AirDropManager {

    private final List<Consumer<McInfPlayer>> airdropHandlers = new LinkedList<>();
    private final Random random = new Random();
    private StorageMinecart airdrop;

    public void spawnAirDrop(List<Location> warp) {
        this.spawnAirDrop(warp.get(random.nextInt(warp.size())));
    }

    public void spawnAirDrop(Location location) {
        this.airdrop = (StorageMinecart) location.getWorld().spawnEntity(location, EntityType.MINECART_CHEST);
        airdrop.setCustomName("§e補救箱");
        airdrop.setInvulnerable(true);
        airdrop.setGlowing(true);
        airdrop.setSlowWhenEmpty(true);
        airdrop.setCustomNameVisible(true);
    }

    public void notifyAirDrop(List<GamePlayer> gamePlayers) {
        if (airdrop == null) return;
        var loc = airdrop.getLocation();
        gamePlayers.forEach(p -> {
            p.getPlayer().sendTitle("", "§a補救箱已送達。", 0, 60, 20);
            p.getPlayer().playSound(loc, Sound.ENTITY_ENDERMAN_STARE, 50, 3);
        });
        MinigamesCore.getApi().getFireWorkManager().spawnFireWork(List.of(loc));
    }

    public void addAirDropHandler(Consumer<McInfPlayer> playerHandler) {
        this.airdropHandlers.add(playerHandler);
    }

    public void removeAirDrop() {
        if (this.airdrop != null) {
            this.airdrop.remove();
            this.airdrop = null;
        }
    }

    public void onInteractAirDrop(PlayerInteractEntityEvent e) {
        if (airdrop == null) return;
        StorageMinecart minecart = airdrop;
        if (e.getRightClicked() != minecart) return;
        MinigamesCore.getApi().getPlayerManager().findPlayer(e.getPlayer()).ifPresent(g -> {
            McInfPlayer player = g.castTo(McInfPlayer.class);
            if (player.getStatus() != GamePlayer.Status.GAMING) return;
            e.setCancelled(true);
            if (!(player.getTeam() instanceof HumanTeam)) return;
            this.removeAirDrop();
            this.airdropHandlers.get(random.nextInt(this.airdropHandlers.size())).accept(player);
        });
    }
}
