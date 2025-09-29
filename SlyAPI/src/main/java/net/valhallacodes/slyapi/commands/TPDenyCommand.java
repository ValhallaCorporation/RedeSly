package net.valhallacodes.slyapi.commands;

import net.valhallacodes.slyapi.SlyAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPDenyCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.only-players"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!SlyAPI.getInstance().getTPAManager().hasPendingRequest(player)) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.no-tpa-request"));
            return true;
        }
        
        if (SlyAPI.getInstance().getTPAManager().denyRequest(player)) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.tpa-denied"));
        } else {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.tpa-deny-failed"));
        }
        
        return true;
    }
}
