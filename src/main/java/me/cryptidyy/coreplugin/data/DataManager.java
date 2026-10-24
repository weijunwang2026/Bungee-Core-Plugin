package me.cryptidyy.coreplugin.data;

import me.cryptidyy.coreplugin.Main;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;

public class DataManager {

    private Main plugin;
    private File configFile;
    private String filename;
    private Configuration config;

    public DataManager(Main plugin, String filename)
    {
        if(!plugin.getDataFolder().exists())
            plugin.getDataFolder().mkdirs();

        this.plugin = plugin;
        this.filename = filename;
        saveDefaultConfig();
    }

    public Configuration getConfig()
    {
        return config;
    }

    public void saveConfig()
    {
        if(config == null)
            return;

        try
        {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, new File(plugin.getDataFolder(), filename));
        }
        catch (IOException e)
        {
            plugin.getLogger().log(Level.SEVERE, "Error saving to " + configFile, e);
        }
    }

    public void saveDefaultConfig()
    {
        File configFile = new File(plugin.getDataFolder(), filename);
        // Copy the default config.yml from the jar resources if it doesn't already exist
        if (!configFile.exists()) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream(filename)) {
                Files.copy(in, configFile.toPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
