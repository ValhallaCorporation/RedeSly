package net.valhallacodes.slycore.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.valhallacodes.slycore.SlycoreBungee;

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
            sender.sendMessage(new TextComponent(SlycoreBungee.getInstance().getConfig().getString("messages.only-players")));
            return;
        }
        
        ProxiedPlayer player = (ProxiedPlayer) sender;
        
        if (args.length == 0) {
            player.sendMessage(new TextComponent(SlycoreBungee.getInstance().getConfig().getString("messages.go-usage")));
            return;
        }
        
        String targetName = args[0];
        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(targetName);
        
        if (target == null) {
            player.sendMessage(new TextComponent(SlycoreBungee.getInstance().getConfig().getString("messages.player-not-found")
                    .replace("%player%", targetName)));
            return;
        }
        
        if (target == player) {
            player.sendMessage(new TextComponent(SlycoreBungee.getInstance().getConfig().getString("messages.cannot-teleport-self")));
            return;
        }
        
        String targetServer = target.getServer().getInfo().getName();
        
        if (player.getServer().getInfo().getName().equals(targetServer)) {
            player.sendMessage(new TextComponent(SlycoreBungee.getInstance().getConfig().getString("messages.already-on-server")));
            return;
        }
        
        player.connect(target.getServer().getInfo());
        
        player.sendMessage(new TextComponent(SlycoreBungee.getInstance().getConfig().getString("messages.teleporting-to")
                .replace("%player%", target.getName())
                .replace("%server%", targetServer)));
        
        target.sendMessage(new TextComponent(SlycoreBungee.getInstance().getConfig().getString("messages.staff-teleporting")
                .replace("%staff%", player.getName())));
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
