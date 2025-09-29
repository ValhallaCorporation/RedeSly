package net.valhallacodes.slyapi.commands;

import net.valhallacodes.slyapi.SlyAPI;
import net.valhallacodes.slyapi.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPACommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, SlyAPI.getInstance().getConfig().getString("messages.only-players"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            MessageUtils.sendMessage(player, SlyAPI.getInstance().getConfig().getString("messages.tpa-usage"));
            return true;
        }
        
        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            MessageUtils.sendMessage(player, SlyAPI.getInstance().getConfig().getString("messages.player-not-found"));
            return true;
        }
        
        if (target == player) {
            MessageUtils.sendMessage(player, SlyAPI.getInstance().getConfig().getString("messages.cannot-tpa-self"));
            return true;
        }
        
        if (SlyAPI.getInstance().getTPAManager().hasPendingRequest(player)) {
            MessageUtils.sendMessage(player, SlyAPI.getInstance().getConfig().getString("messages.tpa-pending-request"));
            return true;
        }
        
        if (SlyAPI.getInstance().getTPAManager().sendRequest(player, target)) {
            MessageUtils.sendMessage(player, SlyAPI.getInstance().getConfig().getString("messages.tpa-sent")
                    .replace("%target%", target.getName()));
            
            MessageUtils.sendMessage(target, SlyAPI.getInstance().getConfig().getString("messages.tpa-received")
                    .replace("%requester%", player.getName()));
            MessageUtils.sendMessage(target, SlyAPI.getInstance().getConfig().getString("messages.tpa-commands"));
        } else {
            MessageUtils.sendMessage(player, SlyAPI.getInstance().getConfig().getString("messages.tpa-failed"));
        }
        
        return true;
    }
}
