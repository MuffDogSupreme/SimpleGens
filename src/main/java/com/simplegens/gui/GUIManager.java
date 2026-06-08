package com.simplegens.gui;

import com.simplegens.SimpleGensPlugin;
import com.simplegens.data.SimpleGensGenerator;
import com.simplegens.input.InputContext;
import com.simplegens.input.InputType;
import com.simplegens.input.PlayerInputManager;
import com.simplegens.manager.SimpleGensGeneratorManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GUIManager implements Listener {

    private final SimpleGensPlugin plugin;
    private final SimpleGensGeneratorManager generatorManager;
    private final PlayerInputManager playerInputManager;
    private final Map<UUID, AbstractGUI> openGUIs = new HashMap<>();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public GUIManager(SimpleGensPlugin plugin, SimpleGensGeneratorManager generatorManager, PlayerInputManager playerInputManager) {
        this.plugin = plugin;
        this.generatorManager = generatorManager;
        this.playerInputManager = playerInputManager;
    }

    public void openGeneratorIndex(Player player) {
        GeneratorIndexGUI gui = new GeneratorIndexGUI(plugin, this, generatorManager, player);
        gui.open(player);
        openGUIs.put(player.getUniqueId(), gui);
    }

    public void openGeneratorConfig(Player player, SimpleGensGenerator generator) {
        GeneratorConfigGUI gui = new GeneratorConfigGUI(plugin, this, generatorManager, player, generator);
        gui.open(player);
        openGUIs.put(player.getUniqueId(), gui);
    }

    public void openBlueprintView(Player player, SimpleGensGenerator generator, int page) {
        BlueprintViewGUI gui = new BlueprintViewGUI(plugin, this, generatorManager, player, generator, page);
        gui.open(player);
        openGUIs.put(player.getUniqueId(), gui);
    }

    public void openConfirmationGUI(Player player, SimpleGensGenerator generator, AbstractGUI previousGUI) {
        ConfirmationGUI gui = new ConfirmationGUI(plugin, this, generatorManager, player, generator, previousGUI);
        gui.open(player);
        openGUIs.put(player.getUniqueId(), gui);
    }

    public void closeGUI(Player player) {
        openGUIs.remove(player.getUniqueId());
        player.closeInventory();
    }

    public void requestChatInput(Player player, InputType type, SimpleGensGenerator generator, Object context, Runnable onComplete) {
        playerInputManager.awaitInput(player, new InputContext(type, generator.getId(), context, onComplete));
        player.closeInventory();
        player.sendMessage(miniMessage.deserialize("<gold>Please type your input in chat.</gold>"));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        AbstractGUI gui = openGUIs.get(player.getUniqueId());

        if (gui != null && event.getInventory().equals(gui.getInventory())) {
            event.setCancelled(true);
            gui.handleClick(event.getSlot(), event.isLeftClick(), event.isRightClick());
        }
    }

    public void refreshGUI(Player player) {
        AbstractGUI gui = openGUIs.get(player.getUniqueId());
        if (gui != null) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                gui.update();
                player.openInventory(gui.getInventory());
            });
        }
    }

    public void refreshGUI(Player player, AbstractGUI gui) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            gui.update();
            player.openInventory(gui.getInventory());
        });
    }

    public MiniMessage getMiniMessage() {
        return miniMessage;
    }
}
