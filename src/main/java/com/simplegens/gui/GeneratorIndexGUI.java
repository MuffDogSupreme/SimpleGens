package com.simplegens.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.simplegens.SimpleGensPlugin;
import com.simplegens.data.SimpleGensGenerator;
import com.simplegens.manager.SimpleGensGeneratorManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public class GeneratorIndexGUI extends AbstractGUI {

    public GeneratorIndexGUI(SimpleGensPlugin plugin, GUIManager guiManager, SimpleGensGeneratorManager generatorManager, Player player) {
        super(plugin, guiManager, generatorManager, player, 27, plugin.getMessageManager().getComponent("gui_index_title", "<dark_aqua>SimpleGens Generators</dark_aqua>"));
        setupItems();
    }

    @Override
    protected void setupItems() {
        List<SimpleGensGenerator> generators = new ArrayList<>(generatorManager.getAllGenerators());
        for (int i = 0; i < generators.size() && i < 27; i++) {
            SimpleGensGenerator generator = generators.get(i);
            Material icon = generator.getIcon() != null ? generator.getIcon() : Material.STONE;

            // Compute the display-ready delay string once.
            // formatTicks() returns the raw tick count (no "t") for unsimplifiable intervals
            // so that messages.yml templates ending in a literal "t" (e.g. "<delay_ticks>t")
            // render as "72t" rather than "72tt". For whole-unit values the suffix is already
            // embedded ("5s", "2m", "1h"). The delayDisplay variant always includes the "t"
            // for raw tick counts, used in the Java-fallback lore string below.
            String delayFmt     = formatTicks(generator.getDelayTicks());
            String delayDisplay = delayFmt.chars().allMatch(Character::isDigit)
                    ? delayFmt + "t"
                    : delayFmt;

            Component name = plugin.getMessageManager().getComponent(
                    "gui_index_item_name",
                    "<gold>Generator: <yellow><id></yellow></gold>",
                    Placeholder.unparsed("id", generator.getId()));

            List<Component> lore = plugin.getMessageManager().getComponentList("gui_index_item_lore",
                    List.of(
                            miniMessage.deserialize("<gray>Mode: <white>" + generator.getMode().name() + "</white></gray>"),
                            miniMessage.deserialize("<gray>Delay: <white>" + delayDisplay + "</white></gray>"),
                            miniMessage.deserialize("<gray>Broadcast: <white>" + (generator.isBroadcastEnabled() ? "Enabled" : "Disabled") + "</white></gray>"),
                            Component.empty(),
                            miniMessage.deserialize("<dark_gray>» Left-Click to configure generator settings.</dark_gray>")
                    ),
                    Placeholder.unparsed("id", generator.getId()),
                    Placeholder.unparsed("mode", generator.getMode().name()),
                    Placeholder.unparsed("delay_ticks", delayFmt),
                    Placeholder.unparsed("broadcast_status", generator.isBroadcastEnabled() ? "ENABLED" : "DISABLED")
            );

            ItemStack item = createGuiItem(icon, name, lore);
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                // Inject the invisible ID to bypass text parsing issues
                NamespacedKey key = new NamespacedKey(plugin, "generator_id");
                meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, generator.getId());
                item.setItemMeta(meta);
            }

            inventory.setItem(i, item);
        }
    }

    @Override
    public void handleClick(int slot, boolean isLeftClick, boolean isRightClick) {
        if (slot < 0 || slot >= inventory.getSize()) return;

        ItemStack clickedItem = inventory.getItem(slot);
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null) return;

        NamespacedKey key = new NamespacedKey(plugin, "generator_id");

        // Ensure the item actually has our hidden generator ID tag
        if (!meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            return;
        }

        // Extract the exact ID string directly from the item's NBT data
        String generatorId = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        SimpleGensGenerator generator = generatorManager.getGenerator(generatorId);

        if (generator == null) {
            player.sendMessage(plugin.getMessageManager().getComponent(
                    "gui_error_generator_not_found",
                    "<red>Generator '<id>' could not be found.</red>",
                    Placeholder.unparsed("id", generatorId)));
            player.closeInventory();
            return;
        }

        // Transition to the next menu with the guaranteed valid object
        guiManager.openGeneratorConfig(player, generator);
    }

    /**
     * Converts a tick count to the cleanest whole-number time representation.
     * <ul>
     *   <li>Multiples of 72 000 ticks → hours  (e.g. 72000 → "1h")</li>
     *   <li>Multiples of 1 200 ticks  → minutes (e.g. 2400  → "2m")</li>
     *   <li>Multiples of 20 ticks     → seconds (e.g. 100   → "5s")</li>
     *   <li>Otherwise                 → raw numeric string, no suffix
     *       (callers/YAML templates append "t" as needed)</li>
     * </ul>
     */
    private static String formatTicks(long ticks) {
        if (ticks % 72000 == 0) return (ticks / 72000) + "h";
        if (ticks % 1200  == 0) return (ticks / 1200)  + "m";
        if (ticks % 20    == 0) return (ticks / 20)    + "s";
        return String.valueOf(ticks);
    }
}
