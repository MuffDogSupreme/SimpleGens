package com.simplegens.task;

import com.simplegens.SimpleGensPlugin;
import com.simplegens.data.SimpleGensBlockBlueprint;
import com.simplegens.data.SimpleGensGenerator;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Level;

public class SimpleGensRegenTask extends BukkitRunnable {

    private final SimpleGensPlugin plugin;
    private final SimpleGensGenerator generator;
    private final MiniMessage miniMessage;

    public SimpleGensRegenTask(SimpleGensPlugin plugin, SimpleGensGenerator generator) {
        this.plugin = plugin;
        this.generator = generator;
        this.miniMessage = MiniMessage.miniMessage();
    }

    @Override
    public void run() {
        World world = generator.getWorld();
        if (world == null) {
            plugin.getLogger().log(Level.WARNING, "Generator ''{0}'' world ''{1}'' not found. Cancelling task.",
                    new Object[]{generator.getId(), generator.getWorldName()});
            this.cancel();
            return;
        }

        for (SimpleGensBlockBlueprint blueprint : generator.getBlocks()) {
            Block block = world.getBlockAt(blueprint.getX(), blueprint.getY(), blueprint.getZ());
            if (block.getType() != blueprint.getMaterial()) {
                block.setType(blueprint.getMaterial(), false);
            }
        }

        if (generator.isBroadcastEnabled() && generator.getBroadcastMessage() != null && !generator.getBroadcastMessage().isEmpty()) {
            Bukkit.getServer().sendMessage(miniMessage.deserialize(generator.getBroadcastMessage()));
        }
    }
}
