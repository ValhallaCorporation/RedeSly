package net.valhallacodes.slycore;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class SlycoreBungee extends Plugin {
    
    private static SlycoreBungee instance;
    private Configuration config;
    
    @Override
    public void onEnable() {
        instance = this;
        
        createConfig();
        loadConfig();
        
        PluginManager pm = getProxy().getPluginManager();
        pm.registerCommand(this, new net.valhallacodes.slycore.commands.GoCommand());
        
        getLogger().info("SlyCore habilitado com sucesso!");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("SlyCore desabilitado!");
    }
    
    private void createConfig() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, configFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void loadConfig() {
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class)
                    .load(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void reloadConfig() {
        loadConfig();
    }
    
    public static SlycoreBungee getInstance() {
        return instance;
    }
    
    public Configuration getConfig() {
        return config;
    }
}
