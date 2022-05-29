package com.ericlam.mc.mcinfected.implement.team;

import com.ericlam.mc.minigames.core.game.GameTeam;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Team;

public class ZombieTeam implements GameTeam {
    @Override
    public String getTeamName() {
        return "殭屍";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.RED;
    }

    @Override
    public boolean isEnabledFriendlyFire() {
        return false;
    }


    @Override
    public void onTeamCreate(Team team) {
        team.setCanSeeFriendlyInvisibles(false);
    }
}
