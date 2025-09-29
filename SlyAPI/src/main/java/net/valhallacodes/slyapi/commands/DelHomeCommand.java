package net.valhallacodes.slyapi.commands;

import net.valhallacodes.slyapi.SlyAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DelHomeCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.only-players"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.delhome-usage"));
            return true;
        }
        
        String homeName = args[0];
        
        if (SlyAPI.getInstance().getHomeManager().deleteHome(player.getName(), homeName)) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.home-deleted")
                    .replace("%home%", homeName));
        } else {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.home-not-found")
                    .replace("%home%", homeName));
        }
        
        return true;
    }
}
