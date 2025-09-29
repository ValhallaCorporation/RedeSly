package net.valhallacodes.slyapi.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class MessageUtils {
    
    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
    
    public static String format(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
