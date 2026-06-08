package com.simplegens.gui;

import com.simplegens.SimpleGensPlugin;
import com.simplegens.data.SimpleGensGenerator;
import com.simplegens.manager.SimpleGensGeneratorManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public class ConfirmationGUI extends AbstractGUI {

    private final SimpleGensGenerator generator;
    private final AbstractGUI previousGUI;

    public ConfirmationGUI(SimpleGensPlugin plugin, GUIManager guiManager, SimpleGensGeneratorManager generatorManager, Player player, SimpleGensGenerator generator, AbstractGUI previousGUI) {
        super(plugin, guiManager, generatorManager, player, 9, plugin.getMessageManager().getComponent("gui_confirm_deletion_title", "<red>Confirm Deletion</red>"));
        this.generator = generator;
        this.previousGUI = previousGUI;
        setupItems();
    }

    @Override
    protected void setupItems() {
        // Confirm buttons
        for (int i = 0; i <= 3; i++) {
            inventory.setItem(i, createGuiItem(
                    Material.LIME_WOOL,
                    plugin.getMessageManager().getComponent("gui_confirm_deletion_confirm_name", "<green><bold>CONFIRM DELETION</bold></green>"),
                    plugin.getMessageManager().getComponentList("gui_confirm_deletion_lore_confirm",
                            List.of(miniMessage.deserialize("<gray>Click to permanently delete</gray>"), miniMessage.deserialize("<gray>generator <yellow>{id}</yellow>.</gray>")),
                            Placeholder.unparsed("id", generator.getId()))
            ));
        }

        // Abort buttons
        for (int i = 5; i <= 8; i++) {
            inventory.setItem(i, createGuiItem(
                    Material.RED_WOOL,
                    plugin.getMessageManager().getComponent("gui_confirm_deletion_abort_name", "<red><bold>ABORT</bold></red>"),
                    plugin.getMessageManager().getComponentList("gui_confirm_deletion_lore_abort",
                            List.of(miniMessage.deserialize("<gray>Click to cancel and go back.</gray>")))
            ));
        }
    }

    @Override
    public void handleClick(int slot, boolean isLeftClick, boolean isRightClick) {
        if (slot >= 0 && slot <= 3) { // Confirm
            generatorManager.removeGenerator(generator.getId());
            player.sendMessage(plugin.getMessageManager().getComponent("gui_generator_deleted", "<green>Generator <yellow>{id}</yellow> has been deleted.</green>", Placeholder.unparsed("id", generator.getId())));
            guiManager.openGeneratorIndex(player);
        } else if (slot >= 5 && slot <= 8) { // Abort
            guiManager.refreshGUI(player, previousGUI);
        }
    }
}
