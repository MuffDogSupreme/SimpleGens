package com.simplegens.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.simplegens.SimpleGensPlugin;
import com.simplegens.data.SimpleGensGenerator;
import com.simplegens.input.InputContext;
import com.simplegens.input.InputType;
import com.simplegens.input.PlayerInputManager;
import com.simplegens.manager.SimpleGensGeneratorManager;

import net.kyori.adventure.text.minimessage.MiniMessage;

public class GUIManager implements Listener {

    private final SimpleGensPlugin plugin;
    private final SimpleGensGeneratorManager generatorManager;
    private final PlayerInputManager playerInputManager;
    private final Map<UUID, AbstractGUI> openGUIs = new HashMap<>();
    private final MiniMessage miniMessage = MiniMessage.miniMessage(); // Keep for internal GUI component creation

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

        // Send specific prompts based on input type
        switch (type) {
            case DELAY_TIME -> {
                player.sendMessage(plugin.getMessageManager().getComponent("input_delay_prompt_header", "<dark_aqua>Please type your new generator delay input in chat:</dark_aqua>"));
                player.sendMessage(plugin.getMessageManager().getComponent("input_delay_prompt_suffixes", "<gray>Available suffixes: <yellow>t</yellow> (ticks), <yellow>s</yellow> (seconds), <yellow>m</yellow> (minutes), <yellow>h</yellow> (hours)</gray>"));
                player.sendMessage(plugin.getMessageManager().getComponent("input_delay_prompt_examples", "<gray>Examples:</gray> <gold>1t</gold> <gray>(every tick),</gray> <gold>5s</gold> <gray>(every 5 seconds),</gray> <gold>2m</gold> <gray>(every 2 minutes)</gray>"));
            }
            case BROADCAST_MESSAGE -> {
                player.sendMessage(plugin.getMessageManager().getComponent("input_broadcast_prompt_header", "<dark_aqua>Please type your new MiniMessage broadcast string in chat:</dark_aqua>"));
                player.sendMessage(plugin.getMessageManager().getComponent("input_broadcast_prompt_example_input", "<gray>Example Input Code: <yellow>\\<gold>\\<bold>Alert\\!</bold>\\</gold> \\<green>Mine reset!\\</green></yellow></gray>"));
                player.sendMessage(plugin.getMessageManager().getComponent("input_broadcast_prompt_example_output", "<gray>Rendered Preview Output: </gray><gold><bold>Alert!</bold></gold> <green>Mine reset!</green>"));
            }
            case ICON_MATERIAL ->
                player.sendMessage(plugin.getMessageManager().getComponent("input_icon_material_prompt", "Type a valid Bukkit Material name to set as this generator's display icon (e.g., DIAMOND_BLOCK):"));
            case BLUEPRINT_MATERIAL ->
                player.sendMessage(plugin.getMessageManager().getComponent("input_blueprint_material_prompt", "Type the exact Bukkit Material name to replace this position (e.g., COAL_ORE):"));
            default ->
                player.sendMessage(plugin.getMessageManager().getComponent("input_generic_prompt", "<gold>Please type your input in chat.</gold>"));
        }
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
