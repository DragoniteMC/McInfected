package com.ericlam.mc.mcinfected.tasks;

import com.ericlam.mc.mcinfected.main.McInfected;
import com.ericlam.mc.minigames.core.SectionTask;
import com.ericlam.mc.minigames.core.manager.PlayerManager;

public class VotingTask implements SectionTask {

    @Override
    public void initTimer(PlayerManager playerManager) {

    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onFinish() {

    }

    @Override
    public long run(long l) {

        return l;
    }

    @Override
    public long getTotalTime() {
        return McInfected.config().getData("votingTime", Long.class).orElse(30L);
    }

    @Override
    public boolean shouldCancel() {
        return false;
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public void setRunning(boolean b) {

    }
}
