package net.valhallacodes.slyapi.listeners;

import net.valhallacodes.slyapi.SlyAPI;
import net.valhallacodes.slyapi.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class GoTeleportListener implements PluginMessageListener {
    
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("slycore:go")) {
            return;
        }
        
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
            String staffName = in.readUTF();
            String targetName = in.readUTF();
            
            Player staff = Bukkit.getPlayer(staffName);
            Player target = Bukkit.getPlayer(targetName);
            
            if (staff != null && target != null && staff.isOnline() && target.isOnline()) {
                staff.teleport(target.getLocation());
                MessageUtils.sendMessage(staff, SlyAPI.getInstance().getConfig().getString("messages.teleported-to-player")
                        .replace("%player%", targetName));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
