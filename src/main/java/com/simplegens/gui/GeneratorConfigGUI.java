package com.simplegens.gui;

import com.simplegens.SimpleGensPlugin;
import com.simplegens.data.SimpleGensGenerator;
import com.simplegens.input.InputType;
import com.simplegens.manager.SimpleGensGeneratorManager;
import com.simplegens.util.TimeParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class GeneratorConfigGUI extends AbstractGUI {

    private final SimpleGensGenerator generator;

    public GeneratorConfigGUI(SimpleGensPlugin plugin, GUIManager guiManager, SimpleGensGeneratorManager generatorManager, Player player, SimpleGensGenerator generator) {
        super(plugin, guiManager, generatorManager, player, 27, guiManager.getMiniMessage().deserialize("<dark_aqua>Configure Generator: <yellow>" + generator.getId() + "</yellow></dark_aqua>"));
        this.generator = generator;
    }

    @Override
    protected void setupItems() {
        // Slot 10: Icon Selector
        inventory.setItem(10, createGuiItem(
                generator.getIcon() != null ? generator.getIcon() : Material.STONE,
                miniMessage.deserialize("<gold>Icon Selector</gold>"),
                List.of(
                miniMessage.deserialize("<gray>Current: <white>" + (generator.getIcon() != null ? generator.getIcon().name() : "STONE") + "</white></gray>"),
                        Component.empty(),
                        miniMessage.deserialize("<dark_gray>» Click to change icon.</dark_gray>")
                )
        ));

        // Slot 12: Timer Adjustment
        inventory.setItem(12, createGuiItem(
                Material.CLOCK,
                miniMessage.deserialize("<gold>Timer Adjustment</gold>"),
                List.of(
                        miniMessage.deserialize("<gray>Current Delay: <white>" + generator.getDelayTicks() + "t</white></gray>"),
                        miniMessage.deserialize("<gray>Mode: <white>" + generator.getMode().name() + "</white></gray>"),
                        Component.empty(),
                        miniMessage.deserialize("<dark_gray>» Click to change delay.</dark_gray>")
                )
        ));

        // Slot 14: Broadcast Message Modifier
        inventory.setItem(14, createGuiItem(
                Material.WRITABLE_BOOK,
                miniMessage.deserialize("<gold>Broadcast Message</gold>"),
                List.of(
                        miniMessage.deserialize("<gray>Current: <white>" + generator.getBroadcastMessage() + "</white></gray>"),
                        Component.empty(),
                        miniMessage.deserialize("<dark_gray>» Click to change message.</dark_gray>")
                )
        ));

        // Slot 16: Broadcast Toggle
        Material toggleMaterial = generator.isBroadcastEnabled() ? Material.LIME_WOOL : Material.RED_WOOL;
        Component toggleName = miniMessage.deserialize("<gold>Broadcast: <white>" + (generator.isBroadcastEnabled() ? "Enabled" : "Disabled") + "</white></gold>");
        inventory.setItem(16, createGuiItem(
                toggleMaterial,
                toggleName,
                List.of(
                        miniMessage.deserialize("<dark_gray>» Click to toggle.</dark_gray>")
                )
        ));

        // Slot 20: Structural Blueprint Editor
        inventory.setItem(20, createGuiItem(
                Material.CHEST,
                miniMessage.deserialize("<gold>Structural Blueprint Editor</gold>"),
                List.of(
                        miniMessage.deserialize("<dark_gray>» Click to view/edit blueprint.</dark_gray>")
                )
        ));

        // Slot 22: Destruct Sequence
        inventory.setItem(22, createGuiItem(
                Material.BARRIER,
                miniMessage.deserialize("<red><bold>DELETE GENERATOR</bold></red>"),
                List.of(
                        miniMessage.deserialize("<dark_gray>» Click to delete this generator.</dark_gray>")
                )
        ));

        // Back button
        inventory.setItem(26, createGuiItem(
                Material.ARROW,
                miniMessage.deserialize("<gray>Back</gray>"),
                List.of(miniMessage.deserialize("<dark_gray>» Return to Generator Index.</dark_gray>"))
        ));
    }

    @Override
    public void handleClick(int slot, boolean isLeftClick, boolean isRightClick) {
        switch (slot) {
            case 10: // Icon Selector
                guiManager.requestChatInput(player, InputType.ICON_MATERIAL, generator, null, () -> guiManager.openGeneratorConfig(player, generator));
                break;
            case 12: // Timer Adjustment
                guiManager.requestChatInput(player, InputType.DELAY_TIME, generator, null, () -> guiManager.openGeneratorConfig(player, generator));
                break;
            case 14: // Broadcast Message Modifier
                guiManager.requestChatInput(player, InputType.BROADCAST_MESSAGE, generator, null, () -> guiManager.openGeneratorConfig(player, generator));
                break;
            case 16: // Broadcast Toggle
                generator.setBroadcastEnabled(!generator.isBroadcastEnabled());
                generatorManager.updateGenerator(generator);
                guiManager.refreshGUI(player, this);
                player.sendMessage(miniMessage.deserialize("<green>Broadcast for " + generator.getId() + " is now " + (generator.isBroadcastEnabled() ? "enabled" : "disabled") + ".</green>"));
                break;
            case 20: // Structural Blueprint Editor
                guiManager.openBlueprintView(player, generator, 0);
                break;
            case 22: // Destruct Sequence
                guiManager.openConfirmationGUI(player, generator, this);
                break;
            case 26: // Back button
                guiManager.openGeneratorIndex(player);
                break;
        }
    }

    public SimpleGensGenerator getGenerator() {
        return generator;
    }
}
