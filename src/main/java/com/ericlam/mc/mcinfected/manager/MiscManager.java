package com.ericlam.mc.mcinfected.manager;

import com.ericlam.mc.minigamemiscs.MinigameMiscs;
import com.ericlam.mc.minigamemiscs.api.TintManager;
import org.bukkit.entity.Player;

public class MiscManager {
    private final TintManager tintManager;

    public MiscManager() {
        this.tintManager = MinigameMiscs.getApi().getTintManager();
    }

    public void setTint(Player player) {
        tintManager.setTint(player, 100);
    }

    public void removeTint(Player player) {
        if (tintManager.isTint(player)) {
            tintManager.removeTint(player);
            tintManager.fadeTint(player, 100, 3);
        }
    }
}
