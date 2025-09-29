package net.valhallacodes.slyapi.commands;

import net.valhallacodes.slyapi.SlyAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPACommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.only-players"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.tpa-usage"));
            return true;
        }
        
        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.player-not-found"));
            return true;
        }
        
        if (target == player) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.cannot-tpa-self"));
            return true;
        }
        
        if (SlyAPI.getInstance().getTPAManager().hasPendingRequest(player)) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.tpa-pending-request"));
            return true;
        }
        
        if (SlyAPI.getInstance().getTPAManager().sendRequest(player, target)) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.tpa-sent")
                    .replace("%target%", target.getName()));
            
            target.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.tpa-received")
                    .replace("%requester%", player.getName()));
            target.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.tpa-commands"));
        } else {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.tpa-failed"));
        }
        
        return true;
    }
}
