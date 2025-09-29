package net.valhallacodes.slyapi.commands;

import net.valhallacodes.slyapi.SlyAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WarpCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.only-players"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.warp-usage"));
            return true;
        }
        
        String warpName = args[0];
        
        try (Connection conn = SlyAPI.getInstance().getDatabaseManager().getConnection()) {
            String sql = "SELECT * FROM warps WHERE name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, warpName);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        var world = SlyAPI.getInstance().getServer().getWorld(rs.getString("world"));
                        if (world == null) {
                            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.warp-world-not-found"));
                            return true;
                        }
                        
                        var location = new org.bukkit.Location(
                                world,
                                rs.getDouble("x"),
                                rs.getDouble("y"),
                                rs.getDouble("z"),
                                rs.getFloat("yaw"),
                                rs.getFloat("pitch")
                        );
                        
                        player.teleport(location);
                        player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.teleported-to-warp")
                                .replace("%warp%", warpName));
                    } else {
                        player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.warp-not-found")
                                .replace("%warp%", warpName));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return true;
    }
}
