package net.valhallacodes.slyapi.listeners;

import net.valhallacodes.slyapi.SlyAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        SlyAPI.getInstance().getTPAManager().removeRequest(player);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        SlyAPI.getInstance().getTPAManager().removeRequest(player);
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        
        if (killer != null) {
            SlyAPI.getInstance().getClanManager().addKill(killer.getName());
        }
        
        SlyAPI.getInstance().getClanManager().addDeath(victim.getName());
    }
}
