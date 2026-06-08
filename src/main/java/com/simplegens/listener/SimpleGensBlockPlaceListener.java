package com.simplegens.listener;

import com.simplegens.SimpleGensPlugin;
import com.simplegens.data.SimpleGensGenerator;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class SimpleGensBlockPlaceListener implements Listener {

    private final SimpleGensPlugin plugin;

    public SimpleGensBlockPlaceListener(SimpleGensPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("simplegens.blockplace.bypass") || player.isOp()) {
            return; // Player has bypass permission
        }

        Block placedBlock = event.getBlockPlaced();
        int x = placedBlock.getX();
        int y = placedBlock.getY();
        int z = placedBlock.getZ();
        String worldName = placedBlock.getWorld().getName();

        for (SimpleGensGenerator generator : plugin.getGeneratorManager().getAllGenerators()) {
            if (generator.getWorldName().equals(worldName) && generator.contains(x, y, z)) {
                event.setCancelled(true);
                player.sendMessage(plugin.getMessageManager().getComponent("block_place_denied", "<red>You cannot place blocks inside an active generator region!</red>"));
                return;
            }
        }
    }
}
