package net.valhallacodes.slyapi.commands;

import net.valhallacodes.slyapi.SlyAPI;
import net.valhallacodes.slyapi.utils.MessageUtils;
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
            MessageUtils.sendMessage(sender, SlyAPI.getInstance().getConfig().getString("messages.only-players"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("slycore.warp.delete")) {
            MessageUtils.sendMessage(player, SlyAPI.getInstance().getConfig().getString("messages.no-permission"));
            return true;
        }
        
        if (args.length == 0) {
            MessageUtils.sendMessage(player, SlyAPI.getInstance().getConfig().getString("messages.delwarp-usage"));
            return true;
        }
        
        String warpName = args[0];
        
        try (Connection conn = SlyAPI.getInstance().getDatabaseManager().getConnection()) {
            String sql = "DELETE FROM warps WHERE name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, warpName);
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    MessageUtils.sendMessage(player, SlyAPI.getInstance().getConfig().getString("messages.warp-deleted")
                            .replace("%warp%", warpName));
                } else {
                    MessageUtils.sendMessage(player, SlyAPI.getInstance().getConfig().getString("messages.warp-not-found")
                            .replace("%warp%", warpName));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            MessageUtils.sendMessage(player, SlyAPI.getInstance().getConfig().getString("messages.warp-delete-failed"));
        }
        
        return true;
    }
}
