package net.valhallacodes.slyapi.commands;

import net.valhallacodes.slyapi.SlyAPI;
import net.valhallacodes.slyapi.managers.HomeManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class HomesCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.only-players"));
            return true;
        }
        
        Player player = (Player) sender;
        
        var homes = SlyAPI.getInstance().getHomeManager().getPlayerHomes(player.getName());
        
        if (homes.isEmpty()) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.no-homes"));
            return true;
        }
        
        int size = ((homes.size() + 8) / 9) * 9;
        if (size > 54) size = 54;
        
        Inventory gui = Bukkit.createInventory(null, size, 
                SlyAPI.getInstance().getConfig().getString("messages.homes-gui-title"));
        
        for (int i = 0; i < homes.size() && i < 54; i++) {
            String homeName = homes.get(i);
            var homeInfo = SlyAPI.getInstance().getHomeManager().getHomeInfo(player.getName(), homeName);
            
            ItemStack item = new ItemStack(Material.BED);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(SlyAPI.getInstance().getConfig().getString("messages.home-item-name")
                    .replace("%home%", homeName));
            
            List<String> lore = new ArrayList<>();
            lore.add(SlyAPI.getInstance().getConfig().getString("messages.home-item-world")
                    .replace("%world%", homeInfo.getWorld()));
            lore.add(SlyAPI.getInstance().getConfig().getString("messages.home-item-coords")
                    .replace("%x%", String.format("%.1f", homeInfo.getX()))
                    .replace("%y%", String.format("%.1f", homeInfo.getY()))
                    .replace("%z%", String.format("%.1f", homeInfo.getZ())));
            lore.add(SlyAPI.getInstance().getConfig().getString("messages.home-item-created")
                    .replace("%date%", homeInfo.getCreatedAt().toString()));
            lore.add("");
            lore.add(SlyAPI.getInstance().getConfig().getString("messages.home-item-click"));
            
            meta.setLore(lore);
            item.setItemMeta(meta);
            
            gui.setItem(i, item);
        }
        
        player.openInventory(gui);
        return true;
    }
}
