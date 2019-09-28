package com.ericlam.mc.mcinfected.implement;

import com.ericlam.mc.minigames.core.gamestats.GameStatsEditor;

public class McInfGameStats implements GameStatsEditor {

    private int kills;
    private int deaths;
    private int played;
    private int wins;
    private int loses;
    private int infected;

    public McInfGameStats(int kills, int deaths, int played, int wins, int loses, int infected) {
        this.kills = kills;
        this.deaths = deaths;
        this.played = played;
        this.wins = wins;
        this.loses = loses;
        this.infected = infected;
    }

    public McInfGameStats() {
        this(0, 0, 0, 0, 0, 0);
    }

    public int getLoses() {
        return loses;
    }

    public void setLoses(int loses) {
        this.loses = loses;
    }

    public int getInfected() {
        return infected;
    }

    public void setInfected(int infected) {
        this.infected = infected;
    }

    @Override
    public void setKills(int i) {
        this.kills = i;
    }

    @Override
    public void setDeaths(int i) {
        this.deaths = i;
    }

    @Override
    public void setPlayed(int i) {
        this.played = i;
    }

    @Override
    public void setWins(int i) {
        this.wins = i;
    }

    @Override
    public int getPlayed() {
        return played;
    }

    @Override
    public int getKills() {
        return kills;
    }

    @Override
    public int getDeaths() {
        return deaths;
    }

    @Override
    public int getWins() {
        return wins;
    }

    @Override
    public double getScores() {
        return kills + wins * 5;
    }

    @Override
    public String[] getInfo() {
        return new String[]{
                "§e殺敵數: §f" + kills,
                "§e勝利數: §f" + wins,
                "§e遊玩數: §f" + played,
                "§e死亡數: §f" + deaths,
                "§e失敗數: §f" + loses,
                "§e感染數: §f" + infected
        };
    }
}
