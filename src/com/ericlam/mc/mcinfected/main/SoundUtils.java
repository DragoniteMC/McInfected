package com.ericlam.mc.mcinfected.main;

import com.ericlam.mc.minigames.core.main.MinigamesCore;
import com.hypernite.mc.hnmc.core.managers.ConfigManager;
import org.bukkit.Bukkit;

import javax.annotation.Nullable;

public class SoundUtils {

    public static void playVoteSound(boolean final_) {
        ConfigManager cf = McInfected.getApi().getConfigManager();
        String[] voteCount = cf.getData("vote".concat(final_ ? "Final" : "Count"), String[].class).orElse(null);
        play(voteCount);
    }

    public static void playGameSound(boolean final_) {
        ConfigManager cf = McInfected.getApi().getConfigManager();
        String[] gameCount = cf.getData("game".concat(final_ ? "Final" : "Count"), String[].class).orElse(null);
        play(gameCount);
    }

    public static void playInfectSound(boolean final_) {
        ConfigManager cf = McInfected.getApi().getConfigManager();
        String[] infectCount = cf.getData("infect".concat(final_ ? "Final" : "Count"), String[].class).orElse(null);
        play(infectCount);
    }

    private static void play(@Nullable String[] sound) {
        if (sound == null) {
            Bukkit.getLogger().warning("the Sound is null");
            return;
        }
        Bukkit.getOnlinePlayers().forEach(p -> MinigamesCore.getApi().getGameUtils().playSound(p, sound));
    }
}
