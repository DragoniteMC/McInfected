package com.ericlam.mc.mcinfected.main;

import com.ericlam.mc.mcinfected.config.InfConfig;
import com.ericlam.mc.minigames.core.main.MinigamesCore;
import org.bukkit.Bukkit;

import javax.annotation.Nullable;

public class SoundUtils {

    public static void playVoteSound(boolean final_) {
        InfConfig cf = McInfected.getApi().getConfigManager().getConfigAs(InfConfig.class);
        String[] voteCount = cf.sounds.voting.get(final_ ? "Final" : "Countdown").split(":");
        play(voteCount);
    }

    public static void playGameSound(boolean final_) {
        InfConfig cf = McInfected.getApi().getConfigManager().getConfigAs(InfConfig.class);
        String[] gameCount = cf.sounds.game.get(final_ ? "Final" : "Countdown").split(":");
        play(gameCount);
    }

    public static void playInfectSound(boolean final_) {
        InfConfig cf = McInfected.getApi().getConfigManager().getConfigAs(InfConfig.class);
        String[] infectCount = cf.sounds.infecting.get(final_ ? "Final" : "Countdown").split(":");
        play(infectCount);
    }

    private static void play(@Nullable String[] sound) {
        if (sound == null) {
            return;
        }
        Bukkit.getOnlinePlayers().forEach(p -> MinigamesCore.getApi().getGameUtils().playSound(p, sound));
    }
}
