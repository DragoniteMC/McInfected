package com.ericlam.mc.mcinfected.implement;

import com.ericlam.mc.mcinfected.config.InfConfig;
import com.ericlam.mc.mcinfected.implement.team.HumanTeam;
import com.ericlam.mc.mcinfected.implement.team.ZombieTeam;
import com.ericlam.mc.mcinfected.main.McInfected;
import com.ericlam.mc.mcinfected.tasks.VotingTask;
import com.ericlam.mc.minigames.core.character.TeamPlayer;
import com.ericlam.mc.minigames.core.game.GameTeam;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.Optional;

public class McInfPlayer implements TeamPlayer {

    private Player player;
    private GameTeam gameTeam;
    private Status status;
    private String humanKit, zombieKit;

    public McInfPlayer(Player player, GameTeam gameTeam, Status status) {
        this.player = player;
        player.setFoodLevel(20);
        player.setWalkSpeed(0.2f);
        player.setGlowing(false);
        player.setLevel(0);
        Optional.ofNullable(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).ifPresent(a -> {
            a.setBaseValue(100);
            player.setHealth(a.getBaseValue());
            player.setHealthScale(20);
        });
        this.gameTeam = gameTeam;
        this.status = status;
        InfConfig infConfig = McInfected.getApi().getConfigManager().getConfigAs(InfConfig.class);
        this.humanKit = infConfig.defaultKit.get("human");
        this.zombieKit = infConfig.defaultKit.get("zombie");
    }

    public McInfPlayer(Player player){
        this(player, null, null);
    }

    public String getHumanKit() {
        return humanKit;
    }

    public void setHumanKit(String humanKit) {
        this.humanKit = humanKit;
    }

    public String getZombieKit() {
        return zombieKit;
    }

    public void setZombieKit(String zombieKit) {
        this.zombieKit = zombieKit;
    }

    @Override
    public GameTeam getTeam() {
        return gameTeam;
    }

    @Override
    public void setTeam(GameTeam gameTeam) {
        this.gameTeam = gameTeam;
        player.setInvulnerable(true);
        if (gameTeam instanceof ZombieTeam){
            Optional.ofNullable(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).ifPresent(a->{
                a.setBaseValue(2000);
                player.setHealthScale(20);
                player.setHealth(a.getBaseValue());
            });
            player.setWalkSpeed(0.25f);
        }else if (gameTeam instanceof HumanTeam){
            Optional.ofNullable(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).ifPresent(a->{
                a.setBaseValue(100);
                player.setHealthScale(20);
                player.setHealth(a.getBaseValue());
            });
            player.setWalkSpeed(0.2f);
        }
        player.setInvulnerable(false);
        VotingTask.switchTeam(this);
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public void setStatus(Status status) {
        this.status = status;
    }
}
