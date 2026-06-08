package com.simplegens;

import com.simplegens.command.SimpleGensCommand;
import com.simplegens.config.SimpleGensConfigManager;
import com.simplegens.gui.GUIManager;
import com.simplegens.input.PlayerInputManager;
import com.simplegens.listener.SimpleGensBlockBreakListener;
import com.simplegens.manager.SimpleGensGeneratorManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleGensPlugin extends JavaPlugin {

    private static SimpleGensPlugin instance;
    private SimpleGensConfigManager configManager;
    private SimpleGensGeneratorManager generatorManager;
    private GUIManager guiManager;
    private PlayerInputManager playerInputManager;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("SimpleGens plugin enabling...");

        this.configManager = new SimpleGensConfigManager(this);
        this.configManager.loadConfigs();

        this.generatorManager = new SimpleGensGeneratorManager(this);
        this.generatorManager.loadGenerators();

        this.playerInputManager = new PlayerInputManager(this);
        this.guiManager = new GUIManager(this, generatorManager, playerInputManager);

        if (getCommand("gen") != null) {
            getCommand("gen").setExecutor(new SimpleGensCommand(this, guiManager));
            getCommand("gen").setTabCompleter(new SimpleGensCommand(this, guiManager));
        }

        getServer().getPluginManager().registerEvents(new SimpleGensBlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(playerInputManager, this);
        getServer().getPluginManager().registerEvents(guiManager, this);

        getLogger().info("SimpleGens plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("SimpleGens plugin disabling...");

        if (this.generatorManager != null) {
            this.generatorManager.saveGenerators();
            this.generatorManager.cancelAllTasks();
        }

        getLogger().info("SimpleGens plugin disabled!");
    }

    public static SimpleGensPlugin getInstance() {
        return instance;
    }

    public SimpleGensConfigManager getConfigManager() {
        return configManager;
    }

    public SimpleGensGeneratorManager getGeneratorManager() {
        return generatorManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public PlayerInputManager getPlayerInputManager() {
        return playerInputManager;
    }
}
