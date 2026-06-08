package com.simplegens.input;

import com.simplegens.SimpleGensPlugin;
import com.simplegens.data.SimpleGensBlockBlueprint;
import com.simplegens.data.SimpleGensGenerator;
import com.simplegens.util.TimeParser;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerInputManager implements Listener {

    private final SimpleGensPlugin plugin;
    private final Map<UUID, InputContext> awaitingInput = new HashMap<>();
    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder().character('&').hexColors().build();

    public PlayerInputManager(SimpleGensPlugin plugin) {
        this.plugin = plugin;
    }

    public void awaitInput(Player player, InputContext context) {
        awaitingInput.put(player.getUniqueId(), context);
    }

    public boolean isAwaitingInput(Player player) {
        return awaitingInput.containsKey(player.getUniqueId());
    }

    @EventHandler
    public void onAsyncChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (awaitingInput.containsKey(playerUUID)) {
            event.setCancelled(true);
            InputContext context = awaitingInput.get(playerUUID);
            String message = legacySerializer.serialize(event.message());

            // CANCEL override: typing the exact keyword "CANCEL" aborts the active
            // input session, clears the capture map, and reopens the config GUI.
            if ("CANCEL".equals(message)) {
                awaitingInput.remove(playerUUID);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.sendMessage(plugin.getMessageManager().getComponent(
                            "input_cancelled", "<yellow>Input cancelled. Returning to menu.</yellow>"));
                    SimpleGensGenerator generator = plugin.getGeneratorManager().getGenerator(context.getGeneratorId());
                    if (generator != null && plugin.getGuiManager() != null) {
                        plugin.getGuiManager().openGeneratorConfig(player, generator);
                    }
                });
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> processInput(player, message, context));
        }
    }

    private void processInput(Player player, String input, InputContext context) {
        SimpleGensGenerator generator = plugin.getGeneratorManager().getGenerator(context.getGeneratorId());
        if (generator == null) {
            player.sendMessage(plugin.getMessageManager().getComponent(
                    "gui_error_generator_not_found", "<red>Error: Generator not found. Aborting input.</red>"));
            awaitingInput.remove(player.getUniqueId());
            if (context.getOnComplete() != null) context.getOnComplete().run();
            return;
        }

        boolean success = false;
        switch (context.getType()) {
            case ICON_MATERIAL -> {
                try {
                    Material material = Material.valueOf(input.toUpperCase());
                    generator.setIcon(material);
                    player.sendMessage(plugin.getMessageManager().getComponent(
                            "gui_icon_updated",
                            "<green>Generator icon updated to <white>{material}</white>.</green>",
                            Placeholder.unparsed("material", material.name())));
                    success = true;
                } catch (IllegalArgumentException e) {
                    player.sendMessage(plugin.getMessageManager().getComponent(
                            "gui_invalid_material",
                            "<red>Invalid material name: <white>{input}</white>.</red>",
                            Placeholder.unparsed("input", input)));
                    // Re-prompt for input
                    player.sendMessage(plugin.getMessageManager().getComponent(
                            "input_icon_material_prompt",
                            "Type a valid Bukkit Material name to set as this generator's display icon (e.g., DIAMOND_BLOCK):"));
                }
            }
            case DELAY_TIME -> {
                long delayTicks = TimeParser.parseTime(input);
                if (delayTicks > 0) {
                    generator.setDelayTicks(delayTicks);
                    player.sendMessage(plugin.getMessageManager().getComponent(
                            "gui_delay_updated",
                            "<green>Generator delay updated to <white>{delay_ticks}t</white>.</green>",
                            Placeholder.unparsed("delay_ticks", String.valueOf(delayTicks))));
                    success = true;
                } else {
                    player.sendMessage(plugin.getMessageManager().getComponent(
                            "gui_invalid_time_format",
                            "<red>Invalid time format. Use formats like 10t, 5s, 2m, 1h.</red>"));
                    // Re-prompt for input
                    player.sendMessage(plugin.getMessageManager().getComponent(
                            "input_delay_prompt_header",
                            "<dark_aqua>Please type your new generator delay input in chat:</dark_aqua>"));
                    player.sendMessage(plugin.getMessageManager().getComponent(
                            "input_delay_prompt_suffixes",
                            "<gray>Available suffixes: <yellow>t</yellow> (ticks), <yellow>s</yellow> (seconds), <yellow>m</yellow> (minutes), <yellow>h</yellow> (hours)</gray>"));
                    player.sendMessage(plugin.getMessageManager().getComponent(
                            "input_delay_prompt_examples",
                            "<dark_gray>Examples: 1t, 5s, 2m</dark_gray>"));
                }
            }
            case BROADCAST_MESSAGE -> {
                generator.setBroadcastMessage(input);
                player.sendMessage(plugin.getMessageManager().getComponent(
                        "gui_broadcast_message_updated", "<green>Broadcast message updated.</green>"));
                success = true;
            }
            case BLUEPRINT_MATERIAL -> {
                if (context.getContextObject() instanceof SimpleGensBlockBlueprint blueprint) {
                    try {
                        Material material = Material.valueOf(input.toUpperCase());
                        if (!material.isBlock()) {
                            player.sendMessage(plugin.getMessageManager().getComponent(
                                    "gui_invalid_block_material",
                                    "<red>Material <white>{input}</white> is not a placeable block.</red>",
                                    Placeholder.unparsed("input", input)));
                            // Re-prompt
                            player.sendMessage(plugin.getMessageManager().getComponent(
                                    "input_blueprint_material_prompt",
                                    "Type the exact Bukkit Material name to replace this position (e.g., COAL_ORE):"));
                        } else {
                            blueprint.setMaterial(material);
                            player.sendMessage(plugin.getMessageManager().getComponent(
                                    "gui_blueprint_material_updated",
                                    "<green>Blueprint block material updated to <white>{material}</white>.</green>",
                                    Placeholder.unparsed("material", material.name())));
                            success = true;
                        }
                    } catch (IllegalArgumentException e) {
                        player.sendMessage(plugin.getMessageManager().getComponent(
                                "gui_invalid_material",
                                "<red>Invalid material name: <white>{input}</white>.</red>",
                                Placeholder.unparsed("input", input)));
                        // Re-prompt
                        player.sendMessage(plugin.getMessageManager().getComponent(
                                "input_blueprint_material_prompt",
                                "Type the exact Bukkit Material name to replace this position (e.g., COAL_ORE):"));
                    }
                } else {
                    player.sendMessage(plugin.getMessageManager().getComponent(
                            "gui_invalid_blueprint_context", "<red>Error: Invalid blueprint context.</red>"));
                    awaitingInput.remove(player.getUniqueId());
                }
            }
        }

        if (success) {
            plugin.getGeneratorManager().updateGenerator(generator);
            awaitingInput.remove(player.getUniqueId());
            if (context.getOnComplete() != null) {
                context.getOnComplete().run();
            }
        }
    }
}
