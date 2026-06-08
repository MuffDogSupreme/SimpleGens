package com.simplegens.gui;

import com.simplegens.SimpleGensPlugin;
import com.simplegens.data.SimpleGensGenerator;
import com.simplegens.manager.SimpleGensGeneratorManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GeneratorIndexGUI extends AbstractGUI {

    public GeneratorIndexGUI(SimpleGensPlugin plugin, GUIManager guiManager, SimpleGensGeneratorManager generatorManager, Player player) {
        super(plugin, guiManager, generatorManager, player, 27, guiManager.getMiniMessage().deserialize("<dark_aqua>Generator Index</dark_aqua>"));
    }

    @Override
    protected void setupItems() {
        List<SimpleGensGenerator> generators = new ArrayList<>(generatorManager.getAllGenerators());
        for (int i = 0; i < generators.size() && i < 27; i++) {
            SimpleGensGenerator generator = generators.get(i);
            Material icon = generator.getIcon() != null ? generator.getIcon() : Material.STONE;

            Component name = miniMessage.deserialize("<gold>Generator: <yellow>" + generator.getId() + "</yellow></gold>");
            List<Component> lore = new ArrayList<>();
            lore.add(miniMessage.deserialize("<gray>Mode: <white>" + generator.getMode().name() + "</white></gray>"));
            lore.add(miniMessage.deserialize("<gray>Delay: <white>" + generator.getDelayTicks() + "t</white></gray>"));
            lore.add(miniMessage.deserialize("<gray>Broadcast: <white>" + (generator.isBroadcastEnabled() ? "Enabled" : "Disabled") + "</white></gray>"));
            lore.add(Component.empty());
            lore.add(miniMessage.deserialize("<dark_gray>» Left-Click to configure generator settings.</dark_gray>"));

            inventory.setItem(i, createGuiItem(icon, name, lore));
        }
    }

    @Override
    public void handleClick(int slot, boolean isLeftClick, boolean isRightClick) {
        if (slot < 0 || slot >= inventory.getSize()) return;

        ItemStack clickedItem = inventory.getItem(slot);
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        // Strip all formatting/MiniMessage tags to get the raw plain-text name
        String plainName = PlainTextComponentSerializer.plainText().serialize(meta.displayName());
        // Name format is "Generator: <id>" — extract the ID after the prefix
        String generatorId = plainName.startsWith("Generator: ") ? plainName.substring("Generator: ".length()) : plainName;

        SimpleGensGenerator generator = generatorManager.getGenerator(generatorId);
        if (generator == null) {
            player.sendMessage(net.kyori.adventure.text.Component.text(
                    "Generator '" + generatorId + "' could not be found.", NamedTextColor.RED));
            return;
        }

        guiManager.openGeneratorConfig(player, generator);
    }
}
