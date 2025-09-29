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

public class SetWarpCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, SlyAPI.getInstance().getConfig().getString("messages.only-players"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("slycore.warp.set")) {
            MessageUtils.sendMessage(player, SlyAPI.getInstance().getConfig().getString("messages.no-permission"));
            return true;
        }
        
        if (args.length == 0) {
            MessageUtils.sendMessage(player, SlyAPI.getInstance().getConfig().getString("messages.setwarp-usage"));
            return true;
        }
        
        String warpName = args[0];
        org.bukkit.Location location = player.getLocation();
        
        try (Connection conn = SlyAPI.getInstance().getDatabaseManager().getConnection()) {
            String sql = "INSERT INTO warps (name, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE world = VALUES(world), x = VALUES(x), y = VALUES(y), z = VALUES(z), yaw = VALUES(yaw), pitch = VALUES(pitch)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, warpName);
                stmt.setString(2, location.getWorld().getName());
                stmt.setDouble(3, location.getX());
                stmt.setDouble(4, location.getY());
                stmt.setDouble(5, location.getZ());
                stmt.setFloat(6, location.getYaw());
                stmt.setFloat(7, location.getPitch());
                stmt.executeUpdate();
                
                MessageUtils.sendMessage(player, SlyAPI.getInstance().getConfig().getString("messages.warp-set")
                        .replace("%warp%", warpName));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            MessageUtils.sendMessage(player, SlyAPI.getInstance().getConfig().getString("messages.warp-set-failed"));
        }
        
        return true;
    }
}
