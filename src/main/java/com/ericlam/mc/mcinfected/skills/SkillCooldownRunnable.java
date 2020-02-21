package com.ericlam.mc.mcinfected.skills;

import com.ericlam.mc.mcinfected.implement.team.ZombieTeam;
import com.ericlam.mc.minigames.core.character.GamePlayer;
import com.ericlam.mc.minigames.core.character.TeamPlayer;
import com.ericlam.mc.minigames.core.main.MinigamesCore;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.util.Map;

public class SkillCooldownRunnable extends BukkitRunnable {

    private Player player;
    private double timer;
    private Map<Player, Boolean> cooldown;

    public SkillCooldownRunnable(Player player, long timer, Map<Player, Boolean> cooldown) {
        this.player = player;
        this.timer = timer;
        this.cooldown = cooldown;
        cooldown.put(player, true);
    }

    @Override
    public void run() {
        if (this.timer < 0.1) {
            cancel();
            MinigamesCore.getApi().getPlayerManager().findPlayer(player).ifPresent(p -> {
                if (p.getStatus() != GamePlayer.Status.GAMING && !(p.castTo(TeamPlayer.class).getTeam() instanceof ZombieTeam)) {
                    return;
                }
                player.sendActionBar("§a技能冷卻完畢");
            });
        } else {
            MinigamesCore.getApi().getPlayerManager().findPlayer(player).ifPresentOrElse(p -> {
                if (p.getStatus() != GamePlayer.Status.GAMING || !(p.castTo(TeamPlayer.class).getTeam() instanceof ZombieTeam)) {
                    cancel();
                    return;
                }
                player.sendActionBar("§c技能冷卻中... §7(".concat(str(timer)).concat("s)"));
            }, this::cancel);
            this.timer -= 0.1;
        }
    }

    private String str(double count) {
        try {
            return new DecimalFormat().format(count);
        } catch (NumberFormatException e) {
            return Math.rint(count) + "";
        }
    }


    @Override
    public synchronized void cancel() throws IllegalStateException {
        super.cancel();
        cooldown.put(player, false);
    }
}
