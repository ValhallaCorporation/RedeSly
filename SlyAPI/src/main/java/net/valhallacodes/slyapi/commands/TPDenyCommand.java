package net.valhallacodes.slyapi.commands;

import net.valhallacodes.slyapi.SlyAPI;
import net.valhallacodes.slyapi.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPDenyCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, SlyAPI.getInstance().getConfig().getString("messages.only-players"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!SlyAPI.getInstance().getTPAManager().hasPendingRequest(player)) {
            MessageUtils.sendMessage(player, SlyAPI.getInstance().getConfig().getString("messages.no-tpa-request"));
            return true;
        }
        
        if (SlyAPI.getInstance().getTPAManager().denyRequest(player)) {
            MessageUtils.sendMessage(player, SlyAPI.getInstance().getConfig().getString("messages.tpa-denied"));
        } else {
            MessageUtils.sendMessage(player, SlyAPI.getInstance().getConfig().getString("messages.tpa-deny-failed"));
        }
        
        return true;
    }
}
