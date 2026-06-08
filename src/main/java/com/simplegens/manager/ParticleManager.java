package com.simplegens.manager;

import com.simplegens.SimpleGensPlugin;
import com.simplegens.data.SimpleGensGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ParticleManager implements Listener {

    private final SimpleGensPlugin plugin;
    // Player UUID -> (Generator ID -> Particle Task)
    private final Map<UUID, Map<String, BukkitTask>> activeParticleTasks;

    public ParticleManager(SimpleGensPlugin plugin) {
        this.plugin = plugin;
        this.activeParticleTasks = new HashMap<>();
    }

    /**
     * Toggles the particle boundary display for a specific generator for a player.
     * @param player The player to toggle the display for.
     * @param generator The generator to display particles for.
     * @return true if particles are now enabled, false if disabled.
     */
    public boolean toggleParticleDisplay(Player player, SimpleGensGenerator generator) {
        UUID playerUUID = player.getUniqueId();
        String generatorId = generator.getId();

        activeParticleTasks.computeIfAbsent(playerUUID, k -> new HashMap<>());

        if (activeParticleTasks.get(playerUUID).containsKey(generatorId)) {
            // Particles are active, cancel the task
            activeParticleTasks.get(playerUUID).remove(generatorId).cancel();
            if (activeParticleTasks.get(playerUUID).isEmpty()) {
                activeParticleTasks.remove(playerUUID);
            }
            return false; // Disabled
        } else {
            // Particles are not active, start a new task
            BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
                if (!player.isOnline() || !player.getWorld().getName().equals(generator.getWorldName())) {
                    // Player logged out or changed world, cancel task
                    cancelParticleDisplay(playerUUID, generatorId);
                    return;
                }
                drawGeneratorOutline(player, generator);
            }, 0L, 10L); // Every half second
            activeParticleTasks.get(playerUUID).put(generatorId, task);
            return true; // Enabled
        }
    }

    /**
     * Draws the 12 outer edge wireframe paths of the generator's cuboid field.
     * @param player The player to send particles to (client-side).
     * @param generator The generator to draw the outline for.
     */
    private void drawGeneratorOutline(Player player, SimpleGensGenerator generator) {
        World world = generator.getWorld();
        if (world == null) return;

        int minX = generator.getMinX();
        int minY = generator.getMinY();
        int minZ = generator.getMinZ();
        int maxX = generator.getMaxX();
        int maxY = generator.getMaxY();
        int maxZ = generator.getMaxZ();

        // Particle type and data
        Particle particle = Particle.DUST;
        Particle.DustOptions dustOptions = new Particle.DustOptions(org.bukkit.Color.AQUA, 1.0f);

        // Define the 8 corners of the cuboid
        // Note: WorldEdit regions are inclusive, so max coordinates need +1 for particle drawing to cover the full block face.
        Location p1 = new Location(world, minX, minY, minZ);
        Location p2 = new Location(world, maxX + 1, minY, minZ);
        Location p3 = new Location(world, minX, maxY + 1, minZ);
        Location p4 = new Location(world, minX, minY, maxZ + 1);
        Location p5 = new Location(world, maxX + 1, maxY + 1, minZ);
        Location p6 = new Location(world, maxX + 1, minY, maxZ + 1);
        Location p7 = new Location(world, minX, maxY + 1, maxZ + 1);
        Location p8 = new Location(world, maxX + 1, maxY + 1, maxZ + 1);

        // Draw 12 edges
        // Bottom square
        drawWireframeLine(player, p1, p2, particle, dustOptions); // minX,minY,minZ -> maxX,minY,minZ
        drawWireframeLine(player, p1, p4, particle, dustOptions); // minX,minY,minZ -> minX,minY,maxZ
        drawWireframeLine(player, p2, p6, particle, dustOptions); // maxX,minY,minZ -> maxX,minY,maxZ
        drawWireframeLine(player, p4, p6, particle, dustOptions); // minX,minY,maxZ -> maxX,minY,maxZ

        // Top square
        drawWireframeLine(player, p3, p5, particle, dustOptions); // minX,maxY,minZ -> maxX,maxY,minZ
        drawWireframeLine(player, p3, p7, particle, dustOptions); // minX,maxY,minZ -> minX,maxY,maxZ
        drawWireframeLine(player, p5, p8, particle, dustOptions); // maxX,maxY,minZ -> maxX,maxY,maxZ
        drawWireframeLine(player, p7, p8, particle, dustOptions); // minX,maxY,maxZ -> maxX,maxY,maxZ

        // Vertical edges
        drawWireframeLine(player, p1, p3, particle, dustOptions); // minX,minY,minZ -> minX,maxY,minZ
        drawWireframeLine(player, p2, p5, particle, dustOptions); // maxX,minY,minZ -> maxX,maxY,minZ
        drawWireframeLine(player, p4, p7, particle, dustOptions); // minX,minY,maxZ -> minX,maxY,maxZ
        drawWireframeLine(player, p6, p8, particle, dustOptions); // maxX,minY,maxZ -> maxX,maxY,maxZ
    }

    /**
     * Helper method to draw a line of particles between two locations for a specific player.
     * @param player The player to send particles to.
     * @param start The starting location.
     * @param end The ending location.
     * @param particle The particle type.
     * @param dustOptions Particle.DustOptions for DUST particle, null otherwise.
     */
    private void drawWireframeLine(Player player, Location start, Location end, Particle particle, Particle.DustOptions dustOptions) {
        double distance = start.distance(end);
        Vector direction = end.toVector().subtract(start.toVector()).normalize();

        for (double i = 0; i < distance; i += 0.5) { // Adjust step for particle density
            Location particleLoc = start.clone().add(direction.clone().multiply(i));
            if (particle == Particle.DUST) {
                player.spawnParticle(particle, particleLoc, 1, dustOptions);
            } else {
                player.spawnParticle(particle, particleLoc, 1);
            }
        }
    }

    /**
     * Cancels all particle tasks for a specific player.
     * @param playerUUID The UUID of the player.
     */
    public void cancelAllParticleDisplaysForPlayer(UUID playerUUID) {
        Map<String, BukkitTask> playerTasks = activeParticleTasks.remove(playerUUID);
        if (playerTasks != null) {
            playerTasks.values().forEach(BukkitTask::cancel);
        }
    }

    /**
     * Cancels particle display for a specific generator for a specific player.
     * @param playerUUID The UUID of the player.
     * @param generatorId The ID of the generator.
     */
    public void cancelParticleDisplay(UUID playerUUID, String generatorId) {
        Map<String, BukkitTask> playerTasks = activeParticleTasks.get(playerUUID);
        if (playerTasks != null) {
            BukkitTask task = playerTasks.remove(generatorId);
            if (task != null) {
                task.cancel();
            }
            if (playerTasks.isEmpty()) {
                activeParticleTasks.remove(playerUUID);
            }
        }
    }

    /**
     * Cancels all particle tasks associated with a given generator ID for all players.
     * This is called when a generator is removed.
     * @param generatorId The ID of the generator that was removed.
     */
    public void cancelParticleDisplayForGenerator(String generatorId) {
        activeParticleTasks.forEach((playerUUID, generatorTasks) -> {
            BukkitTask task = generatorTasks.remove(generatorId);
            if (task != null) {
                task.cancel();
            }
            if (generatorTasks.isEmpty()) {
                activeParticleTasks.remove(playerUUID);
            }
        });
    }

    /**
     * Cancels all active particle tasks for all players and all generators.
     */
    public void cancelAllParticleTasks() {
        activeParticleTasks.values().forEach(playerTasks -> playerTasks.values().forEach(BukkitTask::cancel));
        activeParticleTasks.clear();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        cancelAllParticleDisplaysForPlayer(event.getPlayer().getUniqueId());
    }
}
