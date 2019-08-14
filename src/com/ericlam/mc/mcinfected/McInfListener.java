package com.ericlam.mc.mcinfected;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTeleportEvent;

public class McInfListener implements Listener {

    @EventHandler
    public void onEnderManTeleport(EntityTeleportEvent e){
        if (e.getEntityType() != EntityType.ENDERMAN) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onDrop(EntityDropItemEvent e){
        e.setCancelled(true);
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent e){
        e.setCancelled(true);
    }
}
