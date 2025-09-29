package net.valhallacodes.slyapi.commands;

import net.valhallacodes.slyapi.SlyAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DelWarpCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.only-players"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("slycore.warp.delete")) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.no-permission"));
            return true;
        }
        
        if (args.length == 0) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.delwarp-usage"));
            return true;
        }
        
        String warpName = args[0];
        
        try (Connection conn = SlyAPI.getInstance().getDatabaseManager().getConnection()) {
            String sql = "DELETE FROM warps WHERE name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, warpName);
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.warp-deleted")
                            .replace("%warp%", warpName));
                } else {
                    player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.warp-not-found")
                            .replace("%warp%", warpName));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.warp-delete-failed"));
        }
        
        return true;
    }
}
