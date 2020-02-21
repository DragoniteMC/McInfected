package com.ericlam.mc.mcinfected.implement.team;

import com.ericlam.mc.minigames.core.game.GameTeam;
import org.bukkit.ChatColor;

public class HumanTeam implements GameTeam {

    @Override
    public String getTeamName() {
        return "傭兵";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.GREEN;
    }

    @Override
    public boolean isEnabledFriendlyFire() {
        return false;
    }
}
