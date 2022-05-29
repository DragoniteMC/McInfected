package com.ericlam.mc.mcinfected.implement.team;

import com.ericlam.mc.minigames.core.game.GameTeam;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Team;

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


    @Override
    public void onTeamCreate(Team team) {
        team.setCanSeeFriendlyInvisibles(false);
    }
}
