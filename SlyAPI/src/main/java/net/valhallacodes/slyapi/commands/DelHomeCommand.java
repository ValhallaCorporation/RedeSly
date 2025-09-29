package net.valhallacodes.slyapi.commands;

import net.valhallacodes.slyapi.SlyAPI;
import net.valhallacodes.slyapi.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DelHomeCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, SlyAPI.getInstance().getConfig().getString("messages.only-players"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            MessageUtils.sendMessage(player, SlyAPI.getInstance().getConfig().getString("messages.delhome-usage"));
            return true;
        }
        
        String homeName = args[0];
        
        if (SlyAPI.getInstance().getHomeManager().deleteHome(player.getName(), homeName)) {
            MessageUtils.sendMessage(player, SlyAPI.getInstance().getConfig().getString("messages.home-deleted")
                    .replace("%home%", homeName));
        } else {
            MessageUtils.sendMessage(player, SlyAPI.getInstance().getConfig().getString("messages.home-not-found")
                    .replace("%home%", homeName));
        }
        
        return true;
    }
}
