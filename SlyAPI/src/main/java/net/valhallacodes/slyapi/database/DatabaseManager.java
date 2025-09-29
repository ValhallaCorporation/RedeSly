package net.valhallacodes.slyapi.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.valhallacodes.slyapi.SlyAPI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {
    
    private HikariDataSource dataSource;
    
    public boolean connect() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" + SlyAPI.getInstance().getConfig().getString("database.host") + 
                            ":" + SlyAPI.getInstance().getConfig().getString("database.port") + 
                            "/" + SlyAPI.getInstance().getConfig().getString("database.database"));
            config.setUsername(SlyAPI.getInstance().getConfig().getString("database.username"));
            config.setPassword(SlyAPI.getInstance().getConfig().getString("database.password"));
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            
            dataSource = new HikariDataSource(config);
            
            createTables();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
    
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    private void createTables() {
        try (Connection conn = getConnection()) {
            
            String clansTable = "CREATE TABLE IF NOT EXISTS clans (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(50) UNIQUE NOT NULL," +
                    "tag VARCHAR(4) UNIQUE NOT NULL," +
                    "tag_color VARCHAR(10) DEFAULT '&f'," +
                    "leader VARCHAR(16) NOT NULL," +
                    "balance DOUBLE DEFAULT 0," +
                    "kills INT DEFAULT 0," +
                    "deaths INT DEFAULT 0," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            
            String clanMembersTable = "CREATE TABLE IF NOT EXISTS clan_members (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "clan_id INT NOT NULL," +
                    "player VARCHAR(16) NOT NULL," +
                    "role ENUM('LEADER', 'MODERATOR', 'MEMBER') DEFAULT 'MEMBER'," +
                    "joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (clan_id) REFERENCES clans(id) ON DELETE CASCADE" +
                    ")";
            
            String clanWarsTable = "CREATE TABLE IF NOT EXISTS clan_wars (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "clan1_id INT NOT NULL," +
                    "clan2_id INT NOT NULL," +
                    "status ENUM('PENDING', 'ACTIVE', 'FINISHED') DEFAULT 'PENDING'," +
                    "winner_id INT NULL," +
                    "started_at TIMESTAMP NULL," +
                    "finished_at TIMESTAMP NULL," +
                    "FOREIGN KEY (clan1_id) REFERENCES clans(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (clan2_id) REFERENCES clans(id) ON DELETE CASCADE" +
                    ")";
            
            String homesTable = "CREATE TABLE IF NOT EXISTS homes (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "player VARCHAR(16) NOT NULL," +
                    "name VARCHAR(50) NOT NULL," +
                    "world VARCHAR(100) NOT NULL," +
                    "x DOUBLE NOT NULL," +
                    "y DOUBLE NOT NULL," +
                    "z DOUBLE NOT NULL," +
                    "yaw FLOAT NOT NULL," +
                    "pitch FLOAT NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "UNIQUE KEY unique_player_home (player, name)" +
                    ")";
            
            String warpsTable = "CREATE TABLE IF NOT EXISTS warps (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(50) UNIQUE NOT NULL," +
                    "world VARCHAR(100) NOT NULL," +
                    "x DOUBLE NOT NULL," +
                    "y DOUBLE NOT NULL," +
                    "z DOUBLE NOT NULL," +
                    "yaw FLOAT NOT NULL," +
                    "pitch FLOAT NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            
            String tpaRequestsTable = "CREATE TABLE IF NOT EXISTS tpa_requests (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "requester VARCHAR(16) NOT NULL," +
                    "target VARCHAR(16) NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            
            try (PreparedStatement stmt = conn.prepareStatement(clansTable)) {
                stmt.execute();
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(clanMembersTable)) {
                stmt.execute();
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(clanWarsTable)) {
                stmt.execute();
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(homesTable)) {
                stmt.execute();
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(warpsTable)) {
                stmt.execute();
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(tpaRequestsTable)) {
                stmt.execute();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
