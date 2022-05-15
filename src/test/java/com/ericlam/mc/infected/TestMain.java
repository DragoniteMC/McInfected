package com.ericlam.mc.infected;

public class TestMain {

    public static void main(String[] args) {
       var s1 = (Stats) new StatsImpl(1, 2, 3);
       var s2 = s1.clone();

       System.out.println(s1 == s2);
       System.out.println(s1.getDeaths() == s2.getDeaths());
       System.out.println(s1.getKills() == s2.getKills());
       System.out.println(s1.getWins() == s2.getWins());
    }


    public interface Stats extends Cloneable {

        int getDeaths();

        int getKills();

        int getWins();

        Stats clone();

    }
    public static class StatsImpl implements Stats {

        private final int kills;
        private final int deaths;
        private final int wins;


        public StatsImpl(int kills, int deaths, int wins) {
            this.kills = kills;
            this.deaths = deaths;
            this.wins = wins;
        }


        @Override
        public int getDeaths() {
            return deaths;
        }

        @Override
        public int getKills() {
            return kills;
        }

        @Override
        public int getWins() {
            return wins;
        }

        @Override
        public Stats clone() {
            try {
                return (Stats) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
