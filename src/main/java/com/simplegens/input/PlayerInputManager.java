package com.simplegens.input;

import com.simplegens.SimpleGensPlugin;
import com.simplegens.data.SimpleGensBlockBlueprint;
import com.simplegens.data.SimpleGensGenerator;
import com.simplegens.util.TimeParser;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
            InputContext context = awaitingInput.remove(playerUUID);
            String message = legacySerializer.serialize(event.message()); // Get raw message content

            Bukkit.getScheduler().runTask(plugin, () -> processInput(player, message, context));
        }
    }

    private void processInput(Player player, String input, InputContext context) {
        SimpleGensGenerator generator = plugin.getGeneratorManager().getGenerator(context.getGeneratorId());
        if (generator == null) {
            player.sendMessage(Component.text("Error: Generator not found. Aborting input.", NamedTextColor.RED));
            if (context.getOnComplete() != null) context.getOnComplete().run();
            return;
        }

        boolean success = false;
        switch (context.getType()) {
            case ICON_MATERIAL:
                try {
                    Material material = Material.valueOf(input.toUpperCase());
                    generator.setIcon(material);
                    player.sendMessage(plugin.getGuiManager().getMiniMessage().deserialize("<green>Generator icon updated to <white>" + material.name() + "</white>.</green>"));
                    success = true;
                } catch (IllegalArgumentException e) {
                    player.sendMessage(Component.text("Invalid material name: " + input, NamedTextColor.RED));
                }
                break;
            case DELAY_TIME:
                long delayTicks = TimeParser.parseTime(input);
                if (delayTicks > 0) {
                    generator.setDelayTicks(delayTicks);
                    player.sendMessage(plugin.getGuiManager().getMiniMessage().deserialize("<green>Generator delay updated to <white>" + delayTicks + "t</white>.</green>"));
                    success = true;
                } else {
                    player.sendMessage(Component.text("Invalid time format. Use formats like 10t, 5s, 2m, 1h.", NamedTextColor.RED));
                }
                break;
            case BROADCAST_MESSAGE:
                generator.setBroadcastMessage(input);
                player.sendMessage(plugin.getGuiManager().getMiniMessage().deserialize("<green>Broadcast message updated.</green>"));
                success = true;
                break;
            case BLUEPRINT_MATERIAL:
                if (context.getContextObject() instanceof SimpleGensBlockBlueprint blueprint) {
                    try {
                        Material material = Material.valueOf(input.toUpperCase());
                        blueprint.setMaterial(material);
                        player.sendMessage(plugin.getGuiManager().getMiniMessage().deserialize("<green>Blueprint block material updated to <white>" + material.name() + "</white>.</green>"));
                        success = true;
                    } catch (IllegalArgumentException e) {
                        player.sendMessage(Component.text("Invalid material name: " + input, NamedTextColor.RED));
                    }
                } else {
                    player.sendMessage(Component.text("Error: Invalid blueprint context.", NamedTextColor.RED));
                }
                break;
        }

        if (success) {
            plugin.getGeneratorManager().updateGenerator(generator);
        }

        if (context.getOnComplete() != null) {
            context.getOnComplete().run();
        }
    }
}
