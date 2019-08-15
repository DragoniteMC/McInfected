package com.ericlam.mc.mcinfected.implement.mechanic;

import com.ericlam.mc.mcinfected.implement.McInfGameStats;
import com.ericlam.mc.minigames.core.gamestats.GameStats;
import com.ericlam.mc.minigames.core.gamestats.GameStatsEditor;
import com.ericlam.mc.minigames.core.gamestats.GameStatsHandler;
import com.hypernite.mc.hnmc.core.main.HyperNiteMC;
import com.hypernite.mc.hnmc.core.managers.SQLDataSource;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class McInfGameStatsMechanic implements GameStatsHandler {

    private final SQLDataSource sqlDataSource;
    private final String createTableStatement =
            "CREATE TABLE IF NOT EXISTS `McInfected_stats` (`uuid` VARCHAR(40) PRIMARY KEY NOT NULL, `name`TINYTEXT NOT NULL , `kills` MEDIUMINT DEFAULT 0, `deaths` MEDIUMINT DEFAULT 0, `wins` MEDIUMINT DEFAULT  0, `played` MEDIUMINT DEFAULT 0, `infected` MEDIUMINT DEFAULT 0, `loses` MEDIUMINT DEFAULT 0, `scores` DOUBLE DEFAULT 0)";
    private final String selectStatement = "SELECT * FROM `McInfected_stats` WHERE `uuid`=? OR `name`=?";
    private final String saveStatement = "INSERT INTO `McInfected_stats` VALUES (?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE `name`=?, `kills`=?, `deaths`=?, `wins`=?, `played`=?, `infected`=?, `loses`=?, `scores`=?";
    public McInfGameStatsMechanic(){
        this.sqlDataSource = HyperNiteMC.getAPI().getSQLDataSource();
        CompletableFuture.runAsync(()->{
            try(Connection connection = sqlDataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(createTableStatement)) {
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Nonnull
    @Override
    public GameStatsEditor loadGameStatsData(@Nonnull Player player) {
        try(Connection connection = sqlDataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(selectStatement)){
            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, player.getName());
            ResultSet set = statement.executeQuery();
            if (set.next()){
                int kills = set.getInt("kills");
                int deaths = set.getInt("deaths");
                int wins = set.getInt("wins");
                int played = set.getInt("played");
                int infected = set.getInt("infected");
                int loses = set.getInt("loses");
                return new McInfGameStats(kills, deaths, played, wins, loses, infected);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new McInfGameStats();
    }

    @Override
    public CompletableFuture<Void> saveGameStatsData(OfflinePlayer offlinePlayer, GameStats gameStats) {
        McInfGameStats game = gameStats.castTo(McInfGameStats.class);
        return CompletableFuture.runAsync(()->{
            try(Connection connection = sqlDataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(saveStatement)){
                statement(statement, offlinePlayer, game);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public CompletableFuture<Void> saveGameStatsData(Map<OfflinePlayer, GameStats> map) {
        return CompletableFuture.runAsync(()->{
            try(Connection connection = sqlDataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(saveStatement)){
                for (Map.Entry<OfflinePlayer, GameStats> entry : map.entrySet()) {
                    OfflinePlayer offlinePlayer = entry.getKey();
                    McInfGameStats game = entry.getValue().castTo(McInfGameStats.class);
                    statement(statement, offlinePlayer, game);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        });
    }

    private void statement(PreparedStatement statement, OfflinePlayer offlinePlayer, McInfGameStats game) throws SQLException {
        statement.setString(1, offlinePlayer.getUniqueId().toString());
        statement.setString(2, offlinePlayer.getName());
        statement.setInt(3, game.getKills());
        statement.setInt(4, game.getDeaths());
        statement.setInt(5, game.getWins());
        statement.setInt(6, game.getPlayed());
        statement.setInt(7, game.getInfected());
        statement.setInt(8, game.getLoses());
        statement.setDouble(9, game.getScores());
        statement.setString(10, offlinePlayer.getName());
        statement.setInt(11, game.getKills());
        statement.setInt(12, game.getDeaths());
        statement.setInt(13, game.getWins());
        statement.setInt(14, game.getPlayed());
        statement.setInt(15, game.getInfected());
        statement.setInt(16, game.getLoses());
        statement.setDouble(17, game.getScores());
        statement.execute();
    }
}
