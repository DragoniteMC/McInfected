package com.ericlam.mc.mcinfected.config;

import com.hypernite.mc.hnmc.core.config.Prop;
import com.hypernite.mc.hnmc.core.config.yaml.Configuration;
import com.hypernite.mc.hnmc.core.config.yaml.Resource;

import java.util.Map;

@Resource(locate = "config.yml")
public class InfConfig extends Configuration {

    @Prop(path = "time.game")
    public long gameTime;

    @Prop
    public Map<String, Object> lobby;

    @Prop(path = "time.infecting")
    public long infectTime;

    @Prop(path = "time.voting")
    public long votingTime;

    @Prop(path = "game.alpha-percent")
    public float alphaPercent;

    @Prop(path = "game.hunter-percent")
    public float hunterPercent;

    @Prop(path = "game.auto-start")
    public int autoStart;

    @Prop(path = "game.max-round")
    public int maxRound;

    @Prop(path = "default-kit")
    public Map<String, String> defaultKit;

    @Prop(path = "damage.multiplier")
    public double damageMultiplier;

    @Prop(path = "fallback-server")
    public String fallbackServer;

    @Prop(path = "compass-give-in-sec")
    public long compassGiven;

    @Prop(path = "Sounds.Skill")
    public Map<String, String> soundSkill;

    @Prop(path = "Sounds.Voting")
    public Map<String, String> soundVote;

    @Prop(path = "Sounds.Infecting")
    public Map<String, String> soundInfect;

    @Prop(path = "Sounds.Hunter")
    public Map<String, String> soundHunter;

    @Prop(path = "Sounds.Game")
    public Map<String, String> soundGame;

    @Prop(path = "Sounds")
    public Map<String, String> sounds;

}
