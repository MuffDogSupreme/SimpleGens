package com.simplegens.data;

import com.simplegens.SimpleGensPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.math.BlockVector3;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SimpleGensGenerator {

    private final String id;
    private SimpleGensGeneratorMode mode;
    private long delayTicks;
    private String worldName;
    private boolean broadcastEnabled;
    private String broadcastMessage;
    private Material icon;
    private final List<SimpleGensBlockBlueprint> blocks;
    private int minX, minY, minZ, maxX, maxY, maxZ; // Bounding box for quick checks

    public SimpleGensGenerator(String id, SimpleGensGeneratorMode mode, long delayTicks, String worldName, boolean broadcastEnabled, String broadcastMessage, Material icon, List<SimpleGensBlockBlueprint> blocks, Region region) {
        this.id = id;
        this.mode = mode;
        this.delayTicks = delayTicks;
        this.worldName = worldName;
        this.broadcastEnabled = broadcastEnabled;
        this.broadcastMessage = broadcastMessage;
        this.icon = icon;
        this.blocks = blocks;
        setRegionBounds(region);
    }

    public String getId() {
        return id;
    }

    public SimpleGensGeneratorMode getMode() {
        return mode;
    }

    public void setMode(SimpleGensGeneratorMode mode) {
        this.mode = mode;
    }

    public long getDelayTicks() {
        return delayTicks;
    }

    public void setDelayTicks(long delayTicks) {
        this.delayTicks = delayTicks;
        this.mode = (delayTicks < 20) ? SimpleGensGeneratorMode.STATIC : SimpleGensGeneratorMode.REGEN;
    }

    public String getWorldName() {
        return worldName;
    }

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public boolean isBroadcastEnabled() {
        return broadcastEnabled;
    }

    public void setBroadcastEnabled(boolean broadcastEnabled) {
        this.broadcastEnabled = broadcastEnabled;
    }

    public String getBroadcastMessage() {
        return broadcastMessage;
    }

    public void setBroadcastMessage(String broadcastMessage) {
        this.broadcastMessage = broadcastMessage;
    }

    public Material getIcon() {
        return icon;
    }

    public void setIcon(Material icon) {
        this.icon = icon;
    }

    public List<SimpleGensBlockBlueprint> getBlocks() {
        return blocks;
    }

    public int getMinX() { return minX; }
    public int getMinY() { return minY; }
    public int getMinZ() { return minZ; }
    public int getMaxX() { return maxX; }
    public int getMaxY() { return maxY; }
    public int getMaxZ() { return maxZ; }

    private void setRegionBounds(Region region) {
        if (region != null) {
            BlockVector3 min = region.getMinimumPoint();
            BlockVector3 max = region.getMaximumPoint();
            this.minX = min.getX();
            this.minY = min.getY();
            this.minZ = min.getZ();
            this.maxX = max.getX();
            this.maxY = max.getY();
            this.maxZ = max.getZ();
        } else {
            // Default to a safe, invalid region if no region is provided
            this.minX = this.minY = this.minZ = Integer.MAX_VALUE;
            this.maxX = this.maxY = this.maxZ = Integer.MIN_VALUE;
        }
    }

    public boolean contains(int x, int y, int z) {
        return x >= minX && x <= maxX &&
               y >= minY && y <= maxY &&
               z >= minZ && z <= maxZ;
    }

    public void save(ConfigurationSection section) {
        section.set("mode", mode.name());
        section.set("delay-ticks", delayTicks);
        section.set("world", worldName);
        section.set("broadcast.enabled", broadcastEnabled);
        section.set("broadcast.message", broadcastMessage);
        section.set("icon", icon.name());
        section.set("blocks", blocks.stream().map(SimpleGensBlockBlueprint::serialize).collect(Collectors.toList()));
        section.set("bounds.minX", minX);
        section.set("bounds.minY", minY);
        section.set("bounds.minZ", minZ);
        section.set("bounds.maxX", maxX);
        section.set("bounds.maxY", maxY);
        section.set("bounds.maxZ", maxZ);
    }

    public static SimpleGensGenerator load(String id, ConfigurationSection section) {
        try {
            SimpleGensGeneratorMode mode = SimpleGensGeneratorMode.valueOf(section.getString("mode", "REGEN"));
            long delayTicks = section.getLong("delay-ticks", 100);
            String worldName = section.getString("world");
            boolean broadcastEnabled = section.getBoolean("broadcast.enabled", false);
            String broadcastMessage = section.getString("broadcast.message", SimpleGensPlugin.getInstance().getMessageManager().getString("generator_reset_default_message", "<red>Generator reset!</red>"));
            Material icon = Material.valueOf(section.getString("icon", Material.STONE.name()));
            List<String> serializedBlocks = section.getStringList("blocks");

            if (worldName == null || serializedBlocks == null) {
                SimpleGensPlugin.getInstance().getLogger().warning("Generator '" + id + "' is missing world or blocks data.");
                return null;
            }

            List<SimpleGensBlockBlueprint> blocks = serializedBlocks.stream()
                    .map(SimpleGensBlockBlueprint::deserialize)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // Load bounds
            int minX = section.getInt("bounds.minX", Integer.MAX_VALUE);
            int minY = section.getInt("bounds.minY", Integer.MAX_VALUE);
            int minZ = section.getInt("bounds.minZ", Integer.MAX_VALUE);
            int maxX = section.getInt("bounds.maxX", Integer.MIN_VALUE);
            int maxY = section.getInt("bounds.maxY", Integer.MIN_VALUE);
            int maxZ = section.getInt("bounds.maxZ", Integer.MIN_VALUE);

            // Create a dummy region for the constructor, actual bounds are loaded separately
            // This is a workaround as WorldEdit Region cannot be easily deserialized without a World
            // The important part is that min/max coords are set directly.
            SimpleGensGenerator generator = new SimpleGensGenerator(id, mode, delayTicks, worldName, broadcastEnabled, broadcastMessage, icon, blocks, null);
            generator.minX = minX;
            generator.minY = minY;
            generator.minZ = minZ;
            generator.maxX = maxX;
            generator.maxY = maxY;
            generator.maxZ = maxZ;
            return generator;

        } catch (IllegalArgumentException e) {
            SimpleGensPlugin.getInstance().getLogger().warning("Failed to load generator '" + id + "': Invalid mode or other data. " + e.getMessage());
            return null;
        }
    }
}
