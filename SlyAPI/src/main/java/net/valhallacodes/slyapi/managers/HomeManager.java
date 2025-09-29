package net.valhallacodes.slyapi.managers;

import net.valhallacodes.slyapi.SlyAPI;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HomeManager {
    
    public boolean setHome(Player player, String homeName) {
        if (getHomeCount(player.getName()) >= getMaxHomes(player)) {
            return false;
        }
        
        Location loc = player.getLocation();
        
        try (Connection conn = SlyAPI.getInstance().getDatabaseManager().getConnection()) {
            String sql = "INSERT INTO homes (player, name, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE world = VALUES(world), x = VALUES(x), y = VALUES(y), z = VALUES(z), yaw = VALUES(yaw), pitch = VALUES(pitch)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, player.getName());
                stmt.setString(2, homeName);
                stmt.setString(3, loc.getWorld().getName());
                stmt.setDouble(4, loc.getX());
                stmt.setDouble(5, loc.getY());
                stmt.setDouble(6, loc.getZ());
                stmt.setFloat(7, loc.getYaw());
                stmt.setFloat(8, loc.getPitch());
                stmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deleteHome(String playerName, String homeName) {
        try (Connection conn = SlyAPI.getInstance().getDatabaseManager().getConnection()) {
            String sql = "DELETE FROM homes WHERE player = ? AND name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, playerName);
                stmt.setString(2, homeName);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public Location getHome(String playerName, String homeName) {
        try (Connection conn = SlyAPI.getInstance().getDatabaseManager().getConnection()) {
            String sql = "SELECT * FROM homes WHERE player = ? AND name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, playerName);
                stmt.setString(2, homeName);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return new Location(
                                SlyAPI.getInstance().getServer().getWorld(rs.getString("world")),
                                rs.getDouble("x"),
                                rs.getDouble("y"),
                                rs.getDouble("z"),
                                rs.getFloat("yaw"),
                                rs.getFloat("pitch")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public List<String> getPlayerHomes(String playerName) {
        List<String> homes = new ArrayList<>();
        try (Connection conn = SlyAPI.getInstance().getDatabaseManager().getConnection()) {
            String sql = "SELECT name FROM homes WHERE player = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, playerName);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        homes.add(rs.getString("name"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return homes;
    }
    
    public int getHomeCount(String playerName) {
        try (Connection conn = SlyAPI.getInstance().getDatabaseManager().getConnection()) {
            String sql = "SELECT COUNT(*) FROM homes WHERE player = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, playerName);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    public int getMaxHomes(Player player) {
        for (int i = 10; i >= 1; i--) {
            if (player.hasPermission("slycore.home." + i)) {
                return i;
            }
        }
        return 1;
    }
    
    public HomeInfo getHomeInfo(String playerName, String homeName) {
        try (Connection conn = SlyAPI.getInstance().getDatabaseManager().getConnection()) {
            String sql = "SELECT * FROM homes WHERE player = ? AND name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, playerName);
                stmt.setString(2, homeName);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return new HomeInfo(
                                rs.getString("name"),
                                rs.getString("world"),
                                rs.getDouble("x"),
                                rs.getDouble("y"),
                                rs.getDouble("z"),
                                rs.getTimestamp("created_at")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static class HomeInfo {
        private final String name;
        private final String world;
        private final double x, y, z;
        private final java.sql.Timestamp createdAt;
        
        public HomeInfo(String name, String world, double x, double y, double z, java.sql.Timestamp createdAt) {
            this.name = name;
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.createdAt = createdAt;
        }
        
        public String getName() { return name; }
        public String getWorld() { return world; }
        public double getX() { return x; }
        public double getY() { return y; }
        public double getZ() { return z; }
        public java.sql.Timestamp getCreatedAt() { return createdAt; }
    }
}
