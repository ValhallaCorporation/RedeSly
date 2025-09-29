package net.valhallacodes.slyapi.listeners;

import net.valhallacodes.slyapi.SlyAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportListener implements Listener {
    
    private final Map<UUID, String> pendingTeleports = new HashMap<>();
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        if (pendingTeleports.containsKey(player.getUniqueId())) {
            String targetName = pendingTeleports.remove(player.getUniqueId());
            Player target = SlyAPI.getInstance().getServer().getPlayer(targetName);
            
            if (target != null && target.isOnline()) {
                SlyAPI.getInstance().getServer().getScheduler().runTaskLater(SlyAPI.getInstance(), () -> {
                    if (player.isOnline() && target.isOnline()) {
                        player.teleport(target.getLocation());
                        player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.teleported-to-player")
                                .replace("%player%", targetName));
                    }
                }, 20L);
            }
        }
    }
    
    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        
        if (pendingTeleports.containsKey(player.getUniqueId())) {
            String targetName = pendingTeleports.remove(player.getUniqueId());
            Player target = SlyAPI.getInstance().getServer().getPlayer(targetName);
            
            if (target != null && target.isOnline()) {
                player.teleport(target.getLocation());
                player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.teleported-to-player")
                        .replace("%player%", targetName));
            }
        }
    }
    
    public void addPendingTeleport(Player player, String targetName) {
        pendingTeleports.put(player.getUniqueId(), targetName);
    }
    
    public void removePendingTeleport(Player player) {
        pendingTeleports.remove(player.getUniqueId());
    }
}
