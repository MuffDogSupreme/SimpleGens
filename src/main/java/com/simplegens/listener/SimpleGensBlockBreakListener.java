package com.simplegens.listener;

import java.util.Collection;
import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.simplegens.SimpleGensPlugin;
import com.simplegens.data.SimpleGensGenerator;
import com.simplegens.data.SimpleGensGeneratorMode;

public class SimpleGensBlockBreakListener implements Listener {

    private final SimpleGensPlugin plugin;

    public SimpleGensBlockBreakListener(SimpleGensPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block brokenBlock = event.getBlock();
        Player player = event.getPlayer();
        Location blockLocation = brokenBlock.getLocation();

        Optional<SimpleGensGenerator> staticGenerator = plugin.getGeneratorManager().getAllGenerators().stream()
                .filter(gen -> gen.getMode() == SimpleGensGeneratorMode.STATIC &&
                               gen.getWorldName().equals(brokenBlock.getWorld().getName()) &&
                               gen.getBlocks().stream().anyMatch(bp ->
                                       bp.getX() == brokenBlock.getX() &&
                                       bp.getY() == brokenBlock.getY() &&
                                       bp.getZ() == brokenBlock.getZ()))
                .findFirst();

        if (staticGenerator.isPresent()) {
            event.setCancelled(true);

            player.playSound(blockLocation, brokenBlock.getBlockData().getSoundGroup().getBreakSound(), 1.0f, 1.0f);
            player.spawnParticle(Particle.BLOCK, blockLocation.add(0.5, 0.5, 0.5), 10, 0.2, 0.2, 0.2, 0.1, brokenBlock.getBlockData());

            ItemStack tool = player.getInventory().getItemInMainHand();
            Collection<ItemStack> drops = brokenBlock.getDrops(tool, player);

            PlayerInventory inventory = player.getInventory();
            for (ItemStack drop : drops) {
                if (inventory.addItem(drop).size() > 0) {
                    player.getWorld().dropItemNaturally(player.getLocation(), drop);
                }
            }
        }
    }
}
