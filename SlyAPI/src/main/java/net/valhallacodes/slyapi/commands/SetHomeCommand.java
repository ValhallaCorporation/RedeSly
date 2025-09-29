package net.valhallacodes.slyapi.commands;

import net.valhallacodes.slyapi.SlyAPI;
import net.valhallacodes.slyapi.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetHomeCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, SlyAPI.getInstance().getConfig().getString("messages.only-players"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            MessageUtils.sendMessage(player, SlyAPI.getInstance().getConfig().getString("messages.sethome-usage"));
            return true;
        }
        
        String homeName = args[0];
        
        if (SlyAPI.getInstance().getHomeManager().getHomeCount(player.getName()) >= 
            SlyAPI.getInstance().getHomeManager().getMaxHomes(player)) {
            MessageUtils.sendMessage(player, SlyAPI.getInstance().getConfig().getString("messages.max-homes-reached")
                    .replace("%max%", String.valueOf(SlyAPI.getInstance().getHomeManager().getMaxHomes(player))));
            return true;
        }
        
        if (SlyAPI.getInstance().getHomeManager().setHome(player, homeName)) {
            MessageUtils.sendMessage(player, SlyAPI.getInstance().getConfig().getString("messages.home-set")
                    .replace("%home%", homeName));
        } else {
            MessageUtils.sendMessage(player, SlyAPI.getInstance().getConfig().getString("messages.home-set-failed"));
        }
        
        return true;
    }
}
