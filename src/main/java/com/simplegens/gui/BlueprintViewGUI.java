package com.simplegens.gui;

import com.simplegens.SimpleGensPlugin;
import com.simplegens.data.SimpleGensBlockBlueprint;
import com.simplegens.data.SimpleGensGenerator;
import com.simplegens.input.InputType;
import com.simplegens.manager.SimpleGensGeneratorManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BlueprintViewGUI extends AbstractGUI {

    private final SimpleGensGenerator generator;
    private int currentPage;
    private static final int ITEMS_PER_PAGE = 45; // 5 rows for items, last row for navigation

    public BlueprintViewGUI(SimpleGensPlugin plugin, GUIManager guiManager, SimpleGensGeneratorManager generatorManager, Player player, SimpleGensGenerator generator, int page) {
        super(plugin, guiManager, generatorManager, player, 54, guiManager.getMiniMessage().deserialize("<dark_aqua>Blueprint: <yellow>" + generator.getId() + " (Page " + (page + 1) + ")</yellow></dark_aqua>"));
        this.generator = generator;
        this.currentPage = page;
    }

    @Override
    protected void setupItems() {
        List<SimpleGensBlockBlueprint> blueprints = generator.getBlocks();
        int totalPages = (int) Math.ceil((double) blueprints.size() / ITEMS_PER_PAGE);

        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, blueprints.size());

        for (int i = startIndex; i < endIndex; i++) {
            SimpleGensBlockBlueprint blueprint = blueprints.get(i);
            Component name = miniMessage.deserialize("<yellow>Location: X:" + blueprint.getX() + " Y:" + blueprint.getY() + " Z:" + blueprint.getZ() + "</yellow>");
            List<Component> lore = new ArrayList<>();
            lore.add(miniMessage.deserialize("<gray>Current Material: <white>" + blueprint.getMaterial().name() + "</white></gray>"));
            lore.add(Component.empty());
            lore.add(miniMessage.deserialize("<dark_gray>» Click to manually swap this block's material type.</dark_gray>"));

            inventory.setItem(i - startIndex, createGuiItem(blueprint.getMaterial(), name, lore));
        }

        // Navigation buttons (last row)
        if (currentPage > 0) {
            inventory.setItem(45, createGuiItem(Material.ARROW, miniMessage.deserialize("<green>Previous Page</green>")));
        }
        if (currentPage < totalPages - 1) {
            inventory.setItem(53, createGuiItem(Material.ARROW, miniMessage.deserialize("<green>Next Page</green>")));
        }

        // Back button
        inventory.setItem(49, createGuiItem(
                Material.BARRIER,
                miniMessage.deserialize("<gray>Back</gray>"),
                List.of(miniMessage.deserialize("<dark_gray>» Return to Generator Config.</dark_gray>"))
        ));
    }

    @Override
    public void handleClick(int slot, boolean isLeftClick, boolean isRightClick) {
        List<SimpleGensBlockBlueprint> blueprints = generator.getBlocks();
        int totalPages = (int) Math.ceil((double) blueprints.size() / ITEMS_PER_PAGE);

        if (slot == 45 && currentPage > 0) { // Previous Page
            guiManager.openBlueprintView(player, generator, currentPage - 1);
        } else if (slot == 53 && currentPage < totalPages - 1) { // Next Page
            guiManager.openBlueprintView(player, generator, currentPage + 1);
        } else if (slot == 49) { // Back button
            guiManager.openGeneratorConfig(player, generator);
        } else if (slot >= 0 && slot < ITEMS_PER_PAGE) { // Blueprint block click
            int blueprintIndex = currentPage * ITEMS_PER_PAGE + slot;
            if (blueprintIndex < blueprints.size()) {
                SimpleGensBlockBlueprint blueprint = blueprints.get(blueprintIndex);
                guiManager.requestChatInput(player, InputType.BLUEPRINT_MATERIAL, generator, blueprint, () -> guiManager.openBlueprintView(player, generator, currentPage));
            }
        }
    }
}
