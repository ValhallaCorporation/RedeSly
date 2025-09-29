package net.valhallacodes.slyapi.commands;

import net.valhallacodes.slyapi.SlyAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomeCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.only-players"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.home-usage"));
            return true;
        }
        
        String homeName = args[0];
        org.bukkit.Location homeLocation = SlyAPI.getInstance().getHomeManager().getHome(player.getName(), homeName);
        
        if (homeLocation == null) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.home-not-found")
                    .replace("%home%", homeName));
            return true;
        }
        
        if (homeLocation.getWorld() == null) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.home-world-not-found"));
            return true;
        }
        
        player.teleport(homeLocation);
        player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.teleported-to-home")
                .replace("%home%", homeName));
        
        return true;
    }
}
