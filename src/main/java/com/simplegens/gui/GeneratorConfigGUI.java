package com.simplegens.gui;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.simplegens.SimpleGensPlugin;
import com.simplegens.data.SimpleGensGenerator;
import com.simplegens.input.InputType;
import com.simplegens.manager.SimpleGensGeneratorManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public class GeneratorConfigGUI extends AbstractGUI {

    private final SimpleGensGenerator generator;

    public GeneratorConfigGUI(SimpleGensPlugin plugin, GUIManager guiManager, SimpleGensGeneratorManager generatorManager, Player player, SimpleGensGenerator generator) {
        super(plugin, guiManager, generatorManager, player, 27, plugin.getMessageManager().getComponent("gui_config_title", "<dark_aqua>Configure Generator: <yellow><id></yellow></dark_aqua>", Placeholder.unparsed("id", generator.getId())));
        this.generator = generator;
        setupItems();
    }

    @Override
    protected void setupItems() {
        // Pre-compute the formatted delay strings once for this render pass.
        // formatTicks() returns the raw tick count (no "t") for unsimplifiable intervals
        // so that messages.yml templates using "<delay_ticks>t" render as "72t" and not
        // "72tt". For whole-unit values the suffix is embedded ("5s", "2m", "1h").
        // delayDisplay always includes the unit, used in Java-fallback lore strings.
        String delayFmt     = formatTicks(generator.getDelayTicks());
        String delayDisplay = delayFmt.chars().allMatch(Character::isDigit)
                ? delayFmt + "t"
                : delayFmt;

        // Slot 10: Icon Selector
        inventory.setItem(10, createGuiItem(
                generator.getIcon() != null ? generator.getIcon() : Material.STONE,
                plugin.getMessageManager().getComponent("gui_config_icon_name", "<gold>Icon Selector</gold>",
                        Placeholder.unparsed("id", generator.getId()),
                        Placeholder.unparsed("mode", generator.getMode().name()),
                        Placeholder.unparsed("delay_ticks", delayFmt),
                        Placeholder.unparsed("status", generator.isBroadcastEnabled() ? "ENABLED" : "DISABLED")
                ),
                plugin.getMessageManager().getComponentList("gui_config_icon_lore",
                        List.of(
                                miniMessage.deserialize("<gray>Current: <white>" + (generator.getIcon() != null ? generator.getIcon().name() : "STONE") + "</white></gray>"),
                                Component.empty(),
                                miniMessage.deserialize("<dark_gray>» Click to change icon.</dark_gray>")
                        ),
                        Placeholder.unparsed("id", generator.getId()),
                        Placeholder.unparsed("mode", generator.getMode().name()),
                        Placeholder.unparsed("delay_ticks", delayFmt),
                        Placeholder.unparsed("status", generator.isBroadcastEnabled() ? "ENABLED" : "DISABLED"),
                        Placeholder.unparsed("current_icon", generator.getIcon() != null ? generator.getIcon().name() : "STONE")
                )
        ));

        // Slot 12: Timer Adjustment
        inventory.setItem(12, createGuiItem(
                Material.CLOCK,
                plugin.getMessageManager().getComponent("gui_config_timer_name", "<gold>Timer Adjustment</gold>",
                        Placeholder.unparsed("id", generator.getId()),
                        Placeholder.unparsed("mode", generator.getMode().name()),
                        Placeholder.unparsed("delay_ticks", delayFmt),
                        Placeholder.unparsed("status", generator.isBroadcastEnabled() ? "ENABLED" : "DISABLED")
                ),
                plugin.getMessageManager().getComponentList("gui_config_timer_lore",
                        List.of(
                                miniMessage.deserialize("<gray>Current Delay: <white>" + delayDisplay + "</white></gray>"),
                                miniMessage.deserialize("<gray>Mode: <white>" + generator.getMode().name() + "</white></gray>"),
                                Component.empty(),
                                miniMessage.deserialize("<dark_gray>» Click to change delay.</dark_gray>")
                        ),
                        Placeholder.unparsed("id", generator.getId()),
                        Placeholder.unparsed("mode", generator.getMode().name()),
                        Placeholder.unparsed("delay_ticks", delayFmt),
                        Placeholder.unparsed("status", generator.isBroadcastEnabled() ? "ENABLED" : "DISABLED")
                )
        ));

        // Slot 14: Broadcast Message Modifier
        inventory.setItem(14, createGuiItem(
                Material.WRITABLE_BOOK,
                plugin.getMessageManager().getComponent("gui_config_broadcast_message_name", "<gold>Broadcast Message</gold>",
                        Placeholder.unparsed("id", generator.getId()),
                        Placeholder.unparsed("mode", generator.getMode().name()),
                        Placeholder.unparsed("delay_ticks", delayFmt),
                        Placeholder.unparsed("status", generator.isBroadcastEnabled() ? "ENABLED" : "DISABLED")
                ),
                plugin.getMessageManager().getComponentList("gui_config_broadcast_message_lore",
                        List.of(
                                miniMessage.deserialize("<gray>Current: <white>" + generator.getBroadcastMessage() + "</white></gray>"),
                                Component.empty(),
                                miniMessage.deserialize("<dark_gray>» Click to change message.</dark_gray>")
                        ),
                        Placeholder.unparsed("id", generator.getId()),
                        Placeholder.unparsed("mode", generator.getMode().name()),
                        Placeholder.unparsed("delay_ticks", delayFmt),
                        Placeholder.unparsed("status", generator.isBroadcastEnabled() ? "ENABLED" : "DISABLED"),
                        Placeholder.parsed("current_message", generator.getBroadcastMessage() != null ? generator.getBroadcastMessage() : "")
                )
        ));

        // Slot 16: Broadcast Toggle
        Material toggleMaterial = generator.isBroadcastEnabled() ? Material.LIME_WOOL : Material.RED_WOOL;
        Component toggleName = plugin.getMessageManager().getComponent("gui_config_broadcast_toggle_name",
                "<gold>Broadcast: <white><status></white></gold>",
                Placeholder.unparsed("id", generator.getId()),
                Placeholder.unparsed("mode", generator.getMode().name()),
                Placeholder.unparsed("delay_ticks", delayFmt),
                Placeholder.unparsed("status", generator.isBroadcastEnabled() ? "ENABLED" : "DISABLED")
        );
        inventory.setItem(16, createGuiItem(
                toggleMaterial,
                toggleName,
                plugin.getMessageManager().getComponentList("gui_config_broadcast_toggle_lore",
                        List.of(
                                miniMessage.deserialize("<dark_gray>» Click to toggle.</dark_gray>")
                        ),
                        Placeholder.unparsed("id", generator.getId()),
                        Placeholder.unparsed("mode", generator.getMode().name()),
                        Placeholder.unparsed("delay_ticks", delayFmt),
                        Placeholder.unparsed("status", generator.isBroadcastEnabled() ? "ENABLED" : "DISABLED")
                )
        ));

        // Slot 20: Structural Blueprint Editor
        inventory.setItem(20, createGuiItem(
                Material.CHEST,
                plugin.getMessageManager().getComponent("gui_config_blueprint_editor_name",
                        "<gold>Structural Blueprint Editor</gold>",
                        Placeholder.unparsed("id", generator.getId()),
                        Placeholder.unparsed("mode", generator.getMode().name()),
                        Placeholder.unparsed("delay_ticks", delayFmt),
                        Placeholder.unparsed("status", generator.isBroadcastEnabled() ? "ENABLED" : "DISABLED")
                ),
                plugin.getMessageManager().getComponentList("gui_config_blueprint_editor_lore",
                        List.of(
                                miniMessage.deserialize("<dark_gray>» Click to view/edit blueprint.</dark_gray>")
                        ),
                        Placeholder.unparsed("id", generator.getId()),
                        Placeholder.unparsed("mode", generator.getMode().name()),
                        Placeholder.unparsed("delay_ticks", delayFmt),
                        Placeholder.unparsed("status", generator.isBroadcastEnabled() ? "ENABLED" : "DISABLED")
                )
        ));

        // Slot 22: Destruct Sequence
        inventory.setItem(22, createGuiItem(
                Material.BARRIER,
                plugin.getMessageManager().getComponent("gui_config_delete_generator_name",
                        "<red><bold>DELETE GENERATOR</bold></red>",
                        Placeholder.unparsed("id", generator.getId()),
                        Placeholder.unparsed("mode", generator.getMode().name()),
                        Placeholder.unparsed("delay_ticks", delayFmt),
                        Placeholder.unparsed("status", generator.isBroadcastEnabled() ? "ENABLED" : "DISABLED")
                ),
                plugin.getMessageManager().getComponentList("gui_config_delete_generator_lore",
                        List.of(
                                miniMessage.deserialize("<dark_gray>» Click to delete this generator.</dark_gray>")
                        ),
                        Placeholder.unparsed("id", generator.getId()),
                        Placeholder.unparsed("mode", generator.getMode().name()),
                        Placeholder.unparsed("delay_ticks", delayFmt),
                        Placeholder.unparsed("status", generator.isBroadcastEnabled() ? "ENABLED" : "DISABLED")
                )
        ));

        // Back button
        inventory.setItem(26, createGuiItem(
                Material.ARROW,
                plugin.getMessageManager().getComponent("gui_config_back_name", "<gray>Back</gray>",
                        Placeholder.unparsed("id", generator.getId()),
                        Placeholder.unparsed("mode", generator.getMode().name()),
                        Placeholder.unparsed("delay_ticks", delayFmt),
                        Placeholder.unparsed("status", generator.isBroadcastEnabled() ? "ENABLED" : "DISABLED")
                ),
                plugin.getMessageManager().getComponentList("gui_config_back_lore",
                        List.of(miniMessage.deserialize("<dark_gray>» Return to Generator Index.</dark_gray>")),
                        Placeholder.unparsed("id", generator.getId()),
                        Placeholder.unparsed("mode", generator.getMode().name()),
                        Placeholder.unparsed("delay_ticks", delayFmt),
                        Placeholder.unparsed("status", generator.isBroadcastEnabled() ? "ENABLED" : "DISABLED")
                )
        ));
    }

    @Override
    public void handleClick(int slot, boolean isLeftClick, boolean isRightClick) {
        switch (slot) {
            case 10 -> // Icon Selector
                guiManager.requestChatInput(player, InputType.ICON_MATERIAL, generator, null,
                        () -> guiManager.openGeneratorConfig(player, generator));
            case 12 -> // Timer Adjustment
                guiManager.requestChatInput(player, InputType.DELAY_TIME, generator, null,
                        () -> guiManager.openGeneratorConfig(player, generator));
            case 14 -> // Broadcast Message Modifier
                guiManager.requestChatInput(player, InputType.BROADCAST_MESSAGE, generator, null,
                        () -> guiManager.openGeneratorConfig(player, generator));
            case 16 -> { // Broadcast Toggle
                generator.setBroadcastEnabled(!generator.isBroadcastEnabled());
                generatorManager.updateGenerator(generator);
                guiManager.refreshGUI(player, this);
                player.sendMessage(plugin.getMessageManager().getComponent(
                        "togglebroadcast_status",
                        "<green>Broadcast for <yellow><id></yellow> is now <white><status></white>.</green>",
                        Placeholder.unparsed("id", generator.getId()),
                        Placeholder.unparsed("status", generator.isBroadcastEnabled() ? "ENABLED" : "DISABLED")
                ));
            }
            case 20 -> // Structural Blueprint Editor
                guiManager.openBlueprintView(player, generator, 0);
            case 22 -> // Destruct Sequence
                guiManager.openConfirmationGUI(player, generator, this);
            case 26 -> // Back button
                guiManager.openGeneratorIndex(player);
        }
    }

    public SimpleGensGenerator getGenerator() {
        return generator;
    }

    /**
     * Converts a tick count to the cleanest whole-number time representation.
     * <ul>
     *   <li>Multiples of 72 000 ticks → hours  (e.g. 72000 → "1h")</li>
     *   <li>Multiples of 1 200 ticks  → minutes (e.g. 2400  → "2m")</li>
     *   <li>Multiples of 20 ticks     → seconds (e.g. 100   → "5s")</li>
     *   <li>Otherwise                 → raw numeric string, no "t" suffix
     *       (callers or messages.yml templates append the "t" as needed)</li>
     * </ul>
     */
    private static String formatTicks(long ticks) {
        if (ticks % 72000 == 0) return (ticks / 72000) + "h";
        if (ticks % 1200  == 0) return (ticks / 1200)  + "m";
        if (ticks % 20    == 0) return (ticks / 20)    + "s";
        return String.valueOf(ticks);
    }
}
