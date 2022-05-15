package com.ericlam.mc.mcinfected.implement.mechanic;

import com.dragonite.mc.dnmc.core.main.DragoniteMC;
import com.dragonite.mc.dnmc.core.managers.SQLDataSource;
import com.ericlam.mc.mcinfected.implement.McInfGameStats;
import com.ericlam.mc.minigames.core.gamestats.GameStats;
import com.ericlam.mc.minigames.core.gamestats.GameStatsEditor;
import com.ericlam.mc.minigames.core.gamestats.GameStatsHandler;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.sql.*;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class McInfGameStatsMechanic implements GameStatsHandler {

    private final SQLDataSource sqlDataSource;
    private static final String createTableStatement =
            """
                    CREATE TABLE IF NOT EXISTS `McInfected_stats` 
                    (`uuid` VARCHAR(40) PRIMARY KEY NOT NULL, 
                    `name`TINYTEXT NOT NULL , 
                    `kills` MEDIUMINT DEFAULT 0, 
                    `deaths` MEDIUMINT DEFAULT 0, 
                    `wins` MEDIUMINT DEFAULT  0, 
                    `played` MEDIUMINT DEFAULT 0, 
                    `infected` MEDIUMINT DEFAULT 0, 
                    `loses` MEDIUMINT DEFAULT 0,
                     `scores` DOUBLE DEFAULT 0)
                    """;
    private static final String selectStatement = "SELECT * FROM `McInfected_stats` WHERE `uuid`=? OR `name`=?";
    private static final String saveStatement = """
            INSERT INTO `McInfected_stats` VALUES (?,?,?,?,?,?,?,?,?) 
            ON DUPLICATE KEY UPDATE `name`=?, `kills`=?, `deaths`=?, `wins`=?, `played`=?, `infected`=?, `loses`=?, `scores`=?
            """;

    private static final String createRecordStatement = """
                CREATE TABLE IF NOT EXISTS `McInfected_Log` 
                (`id` int primary key auto_increment, 
                `uuid` VARCHAR(40) NOT NULL, 
                `time` LONG NOT NULL, 
                `kills` MEDIUMINT DEFAULT 0, 
                `deaths` MEDIUMINT DEFAULT 0, 
                `wins` MEDIUMINT DEFAULT  0, 
                `infected` MEDIUMINT DEFAULT 0, 
                `loses` MEDIUMINT DEFAULT 0,
                `scores` DOUBLE DEFAULT 0)
            """;
    private static final String saveRecordStatement = """
            INSERT INTO `McInfected_Log` VALUES (NULL,?,?,?,?,?,?,?,?)
            """;

    public McInfGameStatsMechanic() {
        this.sqlDataSource = DragoniteMC.getAPI().getSQLDataSource();
        CompletableFuture.runAsync(() -> {
            try (Connection connection = sqlDataSource.getConnection();
                 PreparedStatement createTable = connection.prepareStatement(createTableStatement);
                 PreparedStatement createLogTable = connection.prepareStatement(createRecordStatement)) {
                createTable.execute();
                createLogTable.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Nonnull
    @Override
    public GameStatsEditor loadGameStatsData(@Nonnull Player player) {
        try (Connection connection = sqlDataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectStatement)) {
            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, player.getName());
            ResultSet set = statement.executeQuery();
            if (set.next()) {
                int kills = set.getInt("kills");
                int deaths = set.getInt("deaths");
                int wins = set.getInt("wins");
                int played = set.getInt("played");
                int infected = set.getInt("infected");
                int loses = set.getInt("loses");
                double scores = set.getDouble("scores");
                return new McInfGameStats(kills, deaths, played, wins, loses, infected, scores);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new McInfGameStats();
    }

    @Override
    public CompletableFuture<Void> saveGameStatsData(OfflinePlayer offlinePlayer, GameStats gameStats) {
        McInfGameStats game = gameStats.castTo(McInfGameStats.class);
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = sqlDataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(saveStatement)) {
                saveStatsStatement(statement, offlinePlayer, game);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public CompletableFuture<Void> saveGameStatsData(Map<OfflinePlayer, GameStats> map) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = sqlDataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(saveStatement)) {
                for (Map.Entry<OfflinePlayer, GameStats> entry : map.entrySet()) {
                    OfflinePlayer offlinePlayer = entry.getKey();
                    McInfGameStats game = entry.getValue().castTo(McInfGameStats.class);
                    saveStatsStatement(statement, offlinePlayer, game);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        });
    }

    @Override
    public CompletableFuture<Void> saveGameStatsRecord(OfflinePlayer offlinePlayer, GameStats gameStats, Timestamp timestamp) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = sqlDataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(saveRecordStatement)) {
                var mcinfStats = gameStats.castTo(McInfGameStats.class);
                saveRecordStatement(statement, offlinePlayer, mcinfStats, timestamp);
            }catch (SQLException e){
                e.printStackTrace();
            }
        });
    }

    @Override
    public CompletableFuture<Void> saveGameStatsRecord(Map<OfflinePlayer, GameStats> map, Map<OfflinePlayer, Timestamp> map1) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = sqlDataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(saveRecordStatement)) {
                for (Map.Entry<OfflinePlayer, GameStats> entry : map.entrySet()) {
                    var player = entry.getKey();
                    var mcinfStats = entry.getValue().castTo(McInfGameStats.class);
                    var ts = map1.getOrDefault(player, Timestamp.from(Instant.now()));
                    saveRecordStatement(statement, player, mcinfStats, ts);
                }
            }catch (SQLException e){
                e.printStackTrace();
            }
        });
    }

    private void saveRecordStatement(PreparedStatement statement, OfflinePlayer player, McInfGameStats mcinfStats, Timestamp ts) throws SQLException {
        statement.setString(1, player.getUniqueId().toString());
        statement.setLong(2, ts.getTime());
        statement.setInt(3, mcinfStats.getKills());
        statement.setInt(4, mcinfStats.getDeaths());
        statement.setInt(5, mcinfStats.getWins());
        statement.setInt(6, mcinfStats.getInfected());
        statement.setInt(7, mcinfStats.getLoses());
        statement.setDouble(8, mcinfStats.getScores());
        statement.executeUpdate();
    }

    private void saveStatsStatement(PreparedStatement statement, OfflinePlayer offlinePlayer, McInfGameStats game) throws SQLException {
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
