package net.valhallacodes.slyapi.managers;

import net.valhallacodes.slyapi.SlyAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TPAManager {
    
    private final Map<UUID, UUID> pendingRequests = new HashMap<>();
    
    public boolean sendRequest(Player requester, Player target) {
        if (pendingRequests.containsKey(requester.getUniqueId())) {
            return false;
        }
        
        pendingRequests.put(requester.getUniqueId(), target.getUniqueId());
        
        try (Connection conn = SlyAPI.getInstance().getDatabaseManager().getConnection()) {
            String sql = "INSERT INTO tpa_requests (requester, target) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, requester.getName());
                stmt.setString(2, target.getName());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return true;
    }
    
    public boolean acceptRequest(Player target) {
        UUID requesterUUID = null;
        for (Map.Entry<UUID, UUID> entry : pendingRequests.entrySet()) {
            if (entry.getValue().equals(target.getUniqueId())) {
                requesterUUID = entry.getKey();
                break;
            }
        }
        
        if (requesterUUID == null) {
            return false;
        }
        
        Player requester = Bukkit.getPlayer(requesterUUID);
        if (requester == null) {
            pendingRequests.remove(requesterUUID);
            return false;
        }
        
        pendingRequests.remove(requesterUUID);
        
        try (Connection conn = SlyAPI.getInstance().getDatabaseManager().getConnection()) {
            String sql = "DELETE FROM tpa_requests WHERE requester = ? AND target = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, requester.getName());
                stmt.setString(2, target.getName());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        Bukkit.getScheduler().runTaskLater(SlyAPI.getInstance(), () -> {
            if (requester.isOnline() && target.isOnline()) {
                requester.teleport(target.getLocation());
                requester.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.tpa-success")
                        .replace("%target%", target.getName()));
                target.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.tpa-accepted")
                        .replace("%requester%", requester.getName()));
            }
        }, 60L);
        
        return true;
    }
    
    public boolean denyRequest(Player target) {
        UUID requesterUUID = null;
        for (Map.Entry<UUID, UUID> entry : pendingRequests.entrySet()) {
            if (entry.getValue().equals(target.getUniqueId())) {
                requesterUUID = entry.getKey();
                break;
            }
        }
        
        if (requesterUUID == null) {
            return false;
        }
        
        Player requester = Bukkit.getPlayer(requesterUUID);
        pendingRequests.remove(requesterUUID);
        
        try (Connection conn = SlyAPI.getInstance().getDatabaseManager().getConnection()) {
            String sql = "DELETE FROM tpa_requests WHERE requester = ? AND target = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, requester.getName());
                stmt.setString(2, target.getName());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        if (requester != null && requester.isOnline()) {
            requester.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.tpa-denied")
                    .replace("%target%", target.getName()));
        }
        
        return true;
    }
    
    public boolean hasPendingRequest(Player player) {
        return pendingRequests.containsValue(player.getUniqueId());
    }
    
    public Player getRequester(Player target) {
        for (Map.Entry<UUID, UUID> entry : pendingRequests.entrySet()) {
            if (entry.getValue().equals(target.getUniqueId())) {
                return Bukkit.getPlayer(entry.getKey());
            }
        }
        return null;
    }
    
    public void removeRequest(Player player) {
        pendingRequests.remove(player.getUniqueId());
    }
}
