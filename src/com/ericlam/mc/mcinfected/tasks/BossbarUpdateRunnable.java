package com.ericlam.mc.mcinfected.tasks;

import com.ericlam.mc.mcinfected.main.McInfected;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BossBar;
import org.bukkit.scheduler.BukkitRunnable;

public class BossbarUpdateRunnable extends BukkitRunnable {

    private BossBar bossBar;
    private long humans;
    private long zombies;

    public BossbarUpdateRunnable(BossBar bossBar, long humans, long zombies) {
        this.bossBar = bossBar;
        this.humans = humans;
        this.zombies = zombies;
    }

    @Override
    public void run() {
        String tit = McInfected.getApi().getConfigManager().getPureMessage("Picture.Bar.Hunter");
        bossBar.setTitle(tit.replace("<h>", humans + "").replace("<z>", zombies + ""));
        double progress = humans == 0 ? 1.0 : zombies == 0 ? 0.0 : (double) humans / zombies;
        bossBar.setProgress(progress);
        bossBar.setColor(BarColor.WHITE);
    }
}
