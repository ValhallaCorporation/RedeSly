package net.valhallacodes.slyapi.commands;

import net.valhallacodes.slyapi.SlyAPI;
import net.valhallacodes.slyapi.managers.ClanManager;
import net.valhallacodes.slyapi.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClanCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            MessageUtils.sendMessage(sender, SlyAPI.getInstance().getConfig().getString("messages.only-players"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            showHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "criar":
                if (args.length < 3) {
                    player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.clan-create-usage"));
                    return true;
                }
                createClan(player, args[1], args[2]);
                break;
                
            case "deletar":
                deleteClan(player);
                break;
                
            case "convidar":
                if (args.length < 2) {
                    player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.clan-invite-usage"));
                    return true;
                }
                invitePlayer(player, args[1]);
                break;
                
            case "sair":
                leaveClan(player);
                break;
                
            case "expulsar":
                if (args.length < 2) {
                    player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.clan-kick-usage"));
                    return true;
                }
                kickPlayer(player, args[1]);
                break;
                
            case "info":
                showClanInfo(player);
                break;
                
            case "membros":
                showClanMembers(player);
                break;
                
            case "depositar":
                if (SlyAPI.getInstance().getEconomy() == null) {
                    MessageUtils.sendMessage(player, "&c&l[CLAN] &cSistema de economia não disponível! Instale um plugin de economia como EssentialsX.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.clan-deposit-usage"));
                    return true;
                }
                try {
                    double amount = Double.parseDouble(args[1]);
                    depositToClan(player, amount);
                } catch (NumberFormatException e) {
                    player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.invalid-amount"));
                }
                break;
                
            case "sacar":
                if (SlyAPI.getInstance().getEconomy() == null) {
                    MessageUtils.sendMessage(player, "&c&l[CLAN] &cSistema de economia não disponível! Instale um plugin de economia como EssentialsX.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.clan-withdraw-usage"));
                    return true;
                }
                try {
                    double amount = Double.parseDouble(args[1]);
                    withdrawFromClan(player, amount);
                } catch (NumberFormatException e) {
                    player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.invalid-amount"));
                }
                break;
                
            case "settag":
                if (args.length < 2) {
                    player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.clan-settag-usage"));
                    return true;
                }
                setClanTag(player, args[1]);
                break;
                
            case "guerra":
                if (args.length < 2) {
                    player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.clan-war-usage"));
                    return true;
                }
                declareWar(player, args[1]);
                break;
                
            case "top":
                showTopClans(player);
                break;
                
            default:
                showHelp(player);
                break;
        }
        
        return true;
    }
    
    private void showHelp(Player player) {
        String[] help = SlyAPI.getInstance().getConfig().getStringList("messages.clan-help").toArray(new String[0]);
        for (String line : help) {
            player.sendMessage(line);
        }
    }
    
    private void createClan(Player player, String clanName, String tag) {
        if (SlyAPI.getInstance().getClanManager().getPlayerClan(player.getName()) != null) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.already-in-clan"));
            return;
        }
        
        if (tag.length() > 4) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.tag-too-long"));
            return;
        }
        
        if (SlyAPI.getInstance().getClanManager().createClan(player.getName(), clanName, tag)) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.clan-created")
                    .replace("%clan%", clanName)
                    .replace("%tag%", tag));
        } else {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.clan-create-failed"));
        }
    }
    
    private void deleteClan(Player player) {
        String clanName = SlyAPI.getInstance().getClanManager().getPlayerClan(player.getName());
        if (clanName == null) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.not-in-clan"));
            return;
        }
        
        if (!SlyAPI.getInstance().getClanManager().hasClanPermission(player.getName(), "DELETE")) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.no-permission"));
            return;
        }
        
        if (SlyAPI.getInstance().getClanManager().deleteClan(clanName)) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.clan-deleted")
                    .replace("%clan%", clanName));
        } else {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.clan-delete-failed"));
        }
    }
    
    private void invitePlayer(Player player, String targetName) {
        String clanName = SlyAPI.getInstance().getClanManager().getPlayerClan(player.getName());
        if (clanName == null) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.not-in-clan"));
            return;
        }
        
        if (!SlyAPI.getInstance().getClanManager().hasClanPermission(player.getName(), "INVITE")) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.no-permission"));
            return;
        }
        
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.player-not-found"));
            return;
        }
        
        if (SlyAPI.getInstance().getClanManager().getPlayerClan(targetName) != null) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.player-already-in-clan"));
            return;
        }
        
        SlyAPI.getInstance().getClanManager().addMemberToClan(
                SlyAPI.getInstance().getClanManager().getClanIdByName(clanName), targetName, "MEMBER");
        
        player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.player-invited")
                .replace("%player%", targetName));
        target.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.clan-invited")
                .replace("%clan%", clanName));
    }
    
    private void leaveClan(Player player) {
        String clanName = SlyAPI.getInstance().getClanManager().getPlayerClan(player.getName());
        if (clanName == null) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.not-in-clan"));
            return;
        }
        
        if (SlyAPI.getInstance().getClanManager().getPlayerClanRole(player.getName()).equals("LEADER")) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.leader-cannot-leave"));
            return;
        }
        
        if (SlyAPI.getInstance().getClanManager().removeMemberFromClan(player.getName())) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.left-clan")
                    .replace("%clan%", clanName));
        }
    }
    
    private void kickPlayer(Player player, String targetName) {
        String clanName = SlyAPI.getInstance().getClanManager().getPlayerClan(player.getName());
        if (clanName == null) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.not-in-clan"));
            return;
        }
        
        if (!SlyAPI.getInstance().getClanManager().hasClanPermission(player.getName(), "KICK")) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.no-permission"));
            return;
        }
        
        if (!SlyAPI.getInstance().getClanManager().getClanMembers(clanName).contains(targetName)) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.player-not-in-clan"));
            return;
        }
        
        if (SlyAPI.getInstance().getClanManager().removeMemberFromClan(targetName)) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.player-kicked")
                    .replace("%player%", targetName));
            
            Player target = Bukkit.getPlayer(targetName);
            if (target != null) {
                target.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.kicked-from-clan")
                        .replace("%clan%", clanName));
            }
        }
    }
    
    private void showClanInfo(Player player) {
        String clanName = SlyAPI.getInstance().getClanManager().getPlayerClan(player.getName());
        if (clanName == null) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.not-in-clan"));
            return;
        }
        
        try (Connection conn = SlyAPI.getInstance().getDatabaseManager().getConnection()) {
            String sql = "SELECT * FROM clans WHERE name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, clanName);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String tag = rs.getString("tag");
                        String tagColor = rs.getString("tag_color");
                        String leader = rs.getString("leader");
                        double balance = rs.getDouble("balance");
                        int kills = rs.getInt("kills");
                        int deaths = rs.getInt("deaths");
                        
                        player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.clan-info-header")
                                .replace("%clan%", clanName));
                        player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.clan-info-tag")
                                .replace("%tag%", tagColor + tag));
                        player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.clan-info-leader")
                                .replace("%leader%", leader));
                        player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.clan-info-balance")
                                .replace("%balance%", String.format("%.2f", balance)));
                        player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.clan-info-kdr")
                                .replace("%kills%", String.valueOf(kills))
                                .replace("%deaths%", String.valueOf(deaths)));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void showClanMembers(Player player) {
        String clanName = SlyAPI.getInstance().getClanManager().getPlayerClan(player.getName());
        if (clanName == null) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.not-in-clan"));
            return;
        }
        
        List<String> members = SlyAPI.getInstance().getClanManager().getClanMembers(clanName);
        player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.clan-members-header")
                .replace("%clan%", clanName));
        
        for (String member : members) {
            String role = SlyAPI.getInstance().getClanManager().getPlayerClanRole(member);
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.clan-member")
                    .replace("%player%", member)
                    .replace("%role%", role));
        }
    }
    
    private void depositToClan(Player player, double amount) {
        String clanName = SlyAPI.getInstance().getClanManager().getPlayerClan(player.getName());
        if (clanName == null) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.not-in-clan"));
            return;
        }
        
        if (amount <= 0) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.invalid-amount"));
            return;
        }
        
        if (SlyAPI.getInstance().getEconomy().getBalance(player) < amount) {
            MessageUtils.sendMessage(player, SlyAPI.getInstance().getConfig().getString("messages.insufficient-funds"));
            return;
        }
        
        if (SlyAPI.getInstance().getClanManager().depositToClan(player.getName(), amount)) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.deposited-to-clan")
                    .replace("%amount%", String.format("%.2f", amount)));
        } else {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.deposit-failed"));
        }
    }
    
    private void withdrawFromClan(Player player, double amount) {
        String clanName = SlyAPI.getInstance().getClanManager().getPlayerClan(player.getName());
        if (clanName == null) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.not-in-clan"));
            return;
        }
        
        if (amount <= 0) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.invalid-amount"));
            return;
        }
        
        if (!SlyAPI.getInstance().getClanManager().hasClanPermission(player.getName(), "WITHDRAW")) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.no-permission"));
            return;
        }
        
        if (SlyAPI.getInstance().getClanManager().withdrawFromClan(player.getName(), amount)) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.withdrawn-from-clan")
                    .replace("%amount%", String.format("%.2f", amount)));
        } else {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.withdraw-failed"));
        }
    }
    
    private void setClanTag(Player player, String newTag) {
        String clanName = SlyAPI.getInstance().getClanManager().getPlayerClan(player.getName());
        if (clanName == null) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.not-in-clan"));
            return;
        }
        
        if (!SlyAPI.getInstance().getClanManager().hasClanPermission(player.getName(), "SETTAG")) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.no-permission"));
            return;
        }
        
        if (newTag.length() > 4) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.tag-too-long"));
            return;
        }
        
        try (Connection conn = SlyAPI.getInstance().getDatabaseManager().getConnection()) {
            String sql = "UPDATE clans SET tag = ? WHERE name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, newTag);
                stmt.setString(2, clanName);
                if (stmt.executeUpdate() > 0) {
                    player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.clan-tag-updated")
                            .replace("%tag%", newTag));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void declareWar(Player player, String targetClan) {
        String clanName = SlyAPI.getInstance().getClanManager().getPlayerClan(player.getName());
        if (clanName == null) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.not-in-clan"));
            return;
        }
        
        if (!SlyAPI.getInstance().getClanManager().hasClanPermission(player.getName(), "WAR")) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.no-permission"));
            return;
        }
        
        if (clanName.equals(targetClan)) {
            player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.cannot-war-self"));
            return;
        }
        
        try (Connection conn = SlyAPI.getInstance().getDatabaseManager().getConnection()) {
            String sql = "INSERT INTO clan_wars (clan1_id, clan2_id) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, SlyAPI.getInstance().getClanManager().getClanIdByName(clanName));
                stmt.setInt(2, SlyAPI.getInstance().getClanManager().getClanIdByName(targetClan));
                stmt.executeUpdate();
                
                player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.war-declared")
                        .replace("%clan%", targetClan));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void showTopClans(Player player) {
        try (Connection conn = SlyAPI.getInstance().getDatabaseManager().getConnection()) {
            String sql = "SELECT name, kills, deaths FROM clans ORDER BY kills DESC LIMIT 10";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.top-clans-header"));
                    int position = 1;
                    while (rs.next()) {
                        String clanName = rs.getString("name");
                        int kills = rs.getInt("kills");
                        int deaths = rs.getInt("deaths");
                        double kdr = deaths > 0 ? (double) kills / deaths : kills;
                        
                        player.sendMessage(SlyAPI.getInstance().getConfig().getString("messages.top-clan-entry")
                                .replace("%position%", String.valueOf(position))
                                .replace("%clan%", clanName)
                                .replace("%kills%", String.valueOf(kills))
                                .replace("%kdr%", String.format("%.2f", kdr)));
                        position++;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
