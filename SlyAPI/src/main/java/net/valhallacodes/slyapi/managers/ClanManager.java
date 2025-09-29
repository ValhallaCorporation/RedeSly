package net.valhallacodes.slyapi.managers;

import net.valhallacodes.slyapi.SlyAPI;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClanManager {
    
    public boolean createClan(String playerName, String clanName, String tag) {
        try (Connection conn = SlyAPI.getInstance().getDatabaseManager().getConnection()) {
            String sql = "INSERT INTO clans (name, tag, leader) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, clanName);
                stmt.setString(2, tag);
                stmt.setString(3, playerName);
                stmt.executeUpdate();
            }
            
            int clanId = getClanIdByName(clanName);
            addMemberToClan(clanId, playerName, "LEADER");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deleteClan(String clanName) {
        try (Connection conn = SlyAPI.getInstance().getDatabaseManager().getConnection()) {
            String sql = "DELETE FROM clans WHERE name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, clanName);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean addMemberToClan(int clanId, String playerName, String role) {
        try (Connection conn = SlyAPI.getInstance().getDatabaseManager().getConnection()) {
            String sql = "INSERT INTO clan_members (clan_id, player, role) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, clanId);
                stmt.setString(2, playerName);
                stmt.setString(3, role);
                stmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean removeMemberFromClan(String playerName) {
        try (Connection conn = SlyAPI.getInstance().getDatabaseManager().getConnection()) {
            String sql = "DELETE FROM clan_members WHERE player = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, playerName);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public String getPlayerClan(String playerName) {
        try (Connection conn = SlyAPI.getInstance().getDatabaseManager().getConnection()) {
            String sql = "SELECT c.name FROM clans c JOIN clan_members cm ON c.id = cm.clan_id WHERE cm.player = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, playerName);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("name");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public String getPlayerClanRole(String playerName) {
        try (Connection conn = SlyAPI.getInstance().getDatabaseManager().getConnection()) {
            String sql = "SELECT cm.role FROM clan_members cm WHERE cm.player = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, playerName);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("role");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public int getClanIdByName(String clanName) {
        try (Connection conn = SlyAPI.getInstance().getDatabaseManager().getConnection()) {
            String sql = "SELECT id FROM clans WHERE name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, clanName);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("id");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    public boolean depositToClan(String playerName, double amount) {
        String clanName = getPlayerClan(playerName);
        if (clanName == null) return false;
        
        if (SlyAPI.getInstance().getEconomy().getBalance(Bukkit.getOfflinePlayer(playerName)) < amount) {
            return false;
        }
        
        SlyAPI.getInstance().getEconomy().withdrawPlayer(Bukkit.getOfflinePlayer(playerName), amount);
        
        try (Connection conn = SlyAPI.getInstance().getDatabaseManager().getConnection()) {
            String sql = "UPDATE clans SET balance = balance + ? WHERE name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDouble(1, amount);
                stmt.setString(2, clanName);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean withdrawFromClan(String playerName, double amount) {
        String clanName = getPlayerClan(playerName);
        if (clanName == null) return false;
        
        if (!hasClanPermission(playerName, "WITHDRAW")) return false;
        
        try (Connection conn = SlyAPI.getInstance().getDatabaseManager().getConnection()) {
            String sql = "SELECT balance FROM clans WHERE name = ?";
            double clanBalance = 0;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, clanName);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        clanBalance = rs.getDouble("balance");
                    }
                }
            }
            
            if (clanBalance < amount) return false;
            
            String updateSql = "UPDATE clans SET balance = balance - ? WHERE name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setDouble(1, amount);
                stmt.setString(2, clanName);
                if (stmt.executeUpdate() > 0) {
                    SlyAPI.getInstance().getEconomy().depositPlayer(Bukkit.getOfflinePlayer(playerName), amount);
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean hasClanPermission(String playerName, String permission) {
        String role = getPlayerClanRole(playerName);
        if (role == null) return false;
        
        switch (permission) {
            case "WITHDRAW":
            case "INVITE":
            case "KICK":
                return role.equals("LEADER") || role.equals("MODERATOR");
            case "DELETE":
            case "SETTAG":
                return role.equals("LEADER");
            default:
                return false;
        }
    }
    
    public List<String> getClanMembers(String clanName) {
        List<String> members = new ArrayList<>();
        try (Connection conn = SlyAPI.getInstance().getDatabaseManager().getConnection()) {
            String sql = "SELECT cm.player FROM clan_members cm JOIN clans c ON cm.clan_id = c.id WHERE c.name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, clanName);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        members.add(rs.getString("player"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }
    
    public void addKill(String playerName) {
        String clanName = getPlayerClan(playerName);
        if (clanName == null) return;
        
        try (Connection conn = SlyAPI.getInstance().getDatabaseManager().getConnection()) {
            String sql = "UPDATE clans SET kills = kills + 1 WHERE name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, clanName);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void addDeath(String playerName) {
        String clanName = getPlayerClan(playerName);
        if (clanName == null) return;
        
        try (Connection conn = SlyAPI.getInstance().getDatabaseManager().getConnection()) {
            String sql = "UPDATE clans SET deaths = deaths + 1 WHERE name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, clanName);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
