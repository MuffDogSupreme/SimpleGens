package com.simplegens.task;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;

import com.simplegens.SimpleGensPlugin;
import com.simplegens.data.SimpleGensBlockBlueprint;
import com.simplegens.data.SimpleGensGenerator;

public class SimpleGensRegenTask extends BukkitRunnable {

    private final SimpleGensPlugin plugin;
    private final SimpleGensGenerator generator;
    private Iterator<SimpleGensBlockBlueprint> blockIterator;
    private static final int MAX_BLOCKS_PER_TICK = 100; // Max blocks to process synchronously per tick

    public SimpleGensRegenTask(SimpleGensPlugin plugin, SimpleGensGenerator generator) {
        this.plugin = plugin;
        this.generator = generator;
        this.blockIterator = generator.getBlocks().iterator();
    }

    @Override
    public void run() {
        World world = generator.getWorld();
        if (world == null) {
            plugin.getLogger().log(java.util.logging.Level.WARNING,
                    "World not found for generator ''{0}'', cancelling task.", generator.getId());
            this.cancel();
            return;
        }

        int blocksProcessed = 0;
        while (blockIterator.hasNext() && blocksProcessed < MAX_BLOCKS_PER_TICK) {
            SimpleGensBlockBlueprint blueprint = blockIterator.next();
            Block block = world.getBlockAt(blueprint.getX(), blueprint.getY(), blueprint.getZ());

            if (block.getType() != blueprint.getMaterial()) {
                // Build the bounding box using a cloned location to prevent
                // Location.add() from mutating the base point before it is
                // passed to BoundingBox.of().
                Location blockLoc = block.getLocation();
                BoundingBox blockBoundingBox = BoundingBox.of(blockLoc, blockLoc.clone().add(1, 1, 1));

                for (Player p : world.getPlayers()) {
                    if (p.getBoundingBox().overlaps(blockBoundingBox)) {
                        Location playerLoc = p.getLocation();
                        if (playerLoc == null) continue;

                        // Fetch the absolute highest Y boundary of the generator region directly from our data models
                        int highestRegionY = generator.getBlocks().stream()
                                .mapToInt(com.simplegens.data.SimpleGensBlockBlueprint::getY)
                                .max()
                                .orElse(playerLoc.getBlockY());

                        Location safeLoc = new Location(
                                world,
                                playerLoc.getX(),
                                highestRegionY + 1.0, // Instantly teleports you safely directly on top of the ceiling layer
                                playerLoc.getZ(),
                                playerLoc.getYaw(),
                                playerLoc.getPitch());
                        p.teleport(safeLoc);
                    }
                }
                block.setType(blueprint.getMaterial(), false);
            }
            blocksProcessed++;
        }

        // When all blocks in this cycle are fully processed, broadcast and immediately
        // reset the iterator so the next scheduled run begins a fresh cycle.
        // This ensures the effective regen period equals getDelayTicks() exactly.
        if (!blockIterator.hasNext()) {
            blockIterator = generator.getBlocks().iterator();
            if (generator.isBroadcastEnabled()
                    && generator.getBroadcastMessage() != null
                    && !generator.getBroadcastMessage().isEmpty()) {
                net.kyori.adventure.text.Component prefix = plugin.getMessageManager()
                        .getComponent("prefix", "<gray>[<gold>SimpleGens</gold>]</gray> ");
                net.kyori.adventure.text.Component broadcast = plugin.getMessageManager()
                        .getMiniMessage().deserialize(generator.getBroadcastMessage());
                Bukkit.getServer().sendMessage(prefix.append(broadcast));
            }
        }
    }
}
