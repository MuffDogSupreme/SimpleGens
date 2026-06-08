package com.simplegens.gui;

import com.simplegens.SimpleGensPlugin;
import com.simplegens.manager.SimpleGensGeneratorManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public abstract class AbstractGUI {

    protected final SimpleGensPlugin plugin;
    protected final GUIManager guiManager;
    protected final SimpleGensGeneratorManager generatorManager;
    protected final Player player;
    protected Inventory inventory;
    protected final MiniMessage miniMessage; // Use plugin's message manager for messages

    public AbstractGUI(SimpleGensPlugin plugin, GUIManager guiManager, SimpleGensGeneratorManager generatorManager, Player player, int size, Component title) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.generatorManager = generatorManager;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, size, title);
        this.miniMessage = plugin.getMessageManager().getMiniMessage(); // Use the MiniMessage instance from MessageManager
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    protected abstract void setupItems();

    public abstract void handleClick(int slot, boolean isLeftClick, boolean isRightClick);

    public void update() {
        inventory.clear();
        setupItems();
    }

    protected ItemStack createGuiItem(Material material, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(name);
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    protected ItemStack createGuiItem(Material material, Component name) {
        return createGuiItem(material, name, List.of());
    }
}
