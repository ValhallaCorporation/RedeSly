package net.valhallacodes.slycore.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.valhallacodes.slycore.SlycoreBungee;
import net.md_5.bungee.api.ChatColor;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GoCommand extends Command implements TabExecutor {
    
    public GoCommand() {
        super("go", "slycore.go");
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sendMessage(sender, SlycoreBungee.getInstance().getConfig().getString("messages.only-players"));
            return;
        }
        
        ProxiedPlayer player = (ProxiedPlayer) sender;
        
        if (args.length == 0) {
            sendMessage(player, SlycoreBungee.getInstance().getConfig().getString("messages.go-usage"));
            return;
        }
        
        String targetName = args[0];
        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(targetName);
        
        if (target == null) {
            sendMessage(player, SlycoreBungee.getInstance().getConfig().getString("messages.player-not-found")
                    .replace("%player%", targetName));
            return;
        }
        
        if (target == player) {
            sendMessage(player, SlycoreBungee.getInstance().getConfig().getString("messages.cannot-teleport-self"));
            return;
        }
        
        String targetServer = target.getServer().getInfo().getName();
        
        if (player.getServer().getInfo().getName().equals(targetServer)) {
            sendMessage(player, SlycoreBungee.getInstance().getConfig().getString("messages.teleporting-to-player")
                    .replace("%player%", target.getName()));
            
            sendTeleportMessage(player, target.getName());
            return;
        }
        
        player.connect(target.getServer().getInfo());
        
        sendMessage(player, SlycoreBungee.getInstance().getConfig().getString("messages.teleporting-to")
                .replace("%player%", target.getName())
                .replace("%server%", targetServer));
        
        ProxyServer.getInstance().getScheduler().schedule(SlycoreBungee.getInstance(), () -> {
            if (player.isConnected() && target.isConnected() && 
                player.getServer().getInfo().getName().equals(targetServer)) {
                
                sendTeleportMessage(player, target.getName());
            }
        }, 2L, java.util.concurrent.TimeUnit.SECONDS);
    }
    
    private void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', message)));
    }
    
    private void sendTeleportMessage(ProxiedPlayer player, String targetName) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            
            out.writeUTF(player.getName());
            out.writeUTF(targetName);
            
            player.getServer().sendData("slycore:go", b.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return ProxyServer.getInstance().getPlayers().stream()
                    .map(ProxiedPlayer::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
