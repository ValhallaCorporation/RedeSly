package net.valhallacodes.slyapi;

import net.milkbowl.vault.economy.Economy;
import net.valhallacodes.slyapi.commands.*;
import net.valhallacodes.slyapi.database.DatabaseManager;
import net.valhallacodes.slyapi.listeners.PlayerListener;
import net.valhallacodes.slyapi.listeners.TeleportListener;
import net.valhallacodes.slyapi.managers.*;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.SQLException;

public class SlyAPI extends JavaPlugin {
    
    private static SlyAPI instance;
    private DatabaseManager databaseManager;
    private Economy economy;
    private ClanManager clanManager;
    private HomeManager homeManager;
    private TPAManager tpaManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        if (!setupEconomy()) {
            getLogger().severe("Vault não encontrado! Desabilitando plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        createConfig();
        loadConfig();
        
        databaseManager = new DatabaseManager();
        if (!databaseManager.connect()) {
            getLogger().severe("Falha ao conectar com o banco de dados! Desabilitando plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        clanManager = new ClanManager();
        homeManager = new HomeManager();
        tpaManager = new TPAManager();
        
        registerCommands();
        registerEvents();
        
        getLogger().info("SlyAPI habilitado com sucesso!");
    }
    
    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        getLogger().info("SlyAPI desabilitado!");
    }
    
    private void createConfig() {
        saveDefaultConfig();
    }
    
    private void loadConfig() {
        reloadConfig();
    }
    
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }
    
    private void registerCommands() {
        getCommand("clan").setExecutor(new ClanCommand());
        getCommand("homes").setExecutor(new HomesCommand());
        getCommand("home").setExecutor(new HomeCommand());
        getCommand("sethome").setExecutor(new SetHomeCommand());
        getCommand("delhome").setExecutor(new DelHomeCommand());
        getCommand("warp").setExecutor(new WarpCommand());
        getCommand("setwarp").setExecutor(new SetWarpCommand());
        getCommand("delwarp").setExecutor(new DelWarpCommand());
        getCommand("tpa").setExecutor(new TPACommand());
        getCommand("tpaccept").setExecutor(new TPAcceptCommand());
        getCommand("tpdeny").setExecutor(new TPDenyCommand());
    }
    
    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new TeleportListener(), this);
    }
    
    public static SlyAPI getInstance() {
        return instance;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public Economy getEconomy() {
        return economy;
    }
    
    public ClanManager getClanManager() {
        return clanManager;
    }
    
    public HomeManager getHomeManager() {
        return homeManager;
    }
    
    public TPAManager getTPAManager() {
        return tpaManager;
    }
}
