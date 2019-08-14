package com.ericlam.mc.mcinfected.implement;

import com.ericlam.mc.mcinfected.implement.team.HumanTeam;
import com.ericlam.mc.mcinfected.implement.team.ZombieTeam;
import com.ericlam.mc.mcinfected.main.McInfected;
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
        this.gameTeam = gameTeam;
        this.status = status;
        this.humanKit = McInfected.config().getData("humanDefault", String.class).orElse("");
        this.zombieKit = McInfected.config().getData("zombieDefault", String.class).orElse("");
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
        if (gameTeam instanceof ZombieTeam){
            Optional.ofNullable(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).ifPresent(a->{
                a.setBaseValue(200);
                player.setHealth(a.getBaseValue());
            });
        }else if (gameTeam instanceof HumanTeam){
            Optional.ofNullable(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).ifPresent(a->{
                a.setBaseValue(20);
                player.setHealth(a.getBaseValue());
            });
        }
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
