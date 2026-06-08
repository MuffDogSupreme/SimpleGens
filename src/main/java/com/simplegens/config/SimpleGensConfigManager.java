package com.simplegens.config;

import com.simplegens.SimpleGensPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class SimpleGensConfigManager {

    private final SimpleGensPlugin plugin;
    private FileConfiguration generatorsConfig;
    private File generatorsFile;

    public SimpleGensConfigManager(SimpleGensPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfigs() {
        plugin.saveDefaultConfig();

        generatorsFile = new File(plugin.getDataFolder(), "generators.yml");
        if (!generatorsFile.exists()) {
            try (InputStream in = plugin.getResource("generators.yml")) {
                if (in != null) {
                    Files.copy(in, generatorsFile.toPath());
                } else {
                    generatorsFile.createNewFile();
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create generators.yml: " + e.getMessage());
            }
        }
        generatorsConfig = YamlConfiguration.loadConfiguration(generatorsFile);
    }

    public FileConfiguration getGeneratorsConfig() {
        return generatorsConfig;
    }

    public void saveGeneratorsConfig() {
        try {
            generatorsConfig.save(generatorsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save generators.yml: " + e.getMessage());
        }
    }

    public void reloadGeneratorsConfig() {
        generatorsConfig = YamlConfiguration.loadConfiguration(generatorsFile);
    }
}
