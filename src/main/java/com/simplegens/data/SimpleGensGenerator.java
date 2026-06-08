package com.simplegens.data;

import com.simplegens.SimpleGensPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

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

    public SimpleGensGenerator(String id, SimpleGensGeneratorMode mode, long delayTicks, String worldName, boolean broadcastEnabled, String broadcastMessage, Material icon, List<SimpleGensBlockBlueprint> blocks) {
        this.id = id;
        this.mode = mode;
        this.delayTicks = delayTicks;
        this.worldName = worldName;
        this.broadcastEnabled = broadcastEnabled;
        this.broadcastMessage = broadcastMessage;
        this.icon = icon;
        this.blocks = blocks;
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

    public void save(ConfigurationSection section) {
        section.set("mode", mode.name());
        section.set("delay-ticks", delayTicks);
        section.set("world", worldName);
        section.set("broadcast.enabled", broadcastEnabled);
        section.set("broadcast.message", broadcastMessage);
        section.set("icon", icon.name());
        section.set("blocks", blocks.stream().map(SimpleGensBlockBlueprint::serialize).collect(Collectors.toList()));
    }

    public static SimpleGensGenerator load(String id, ConfigurationSection section) {
        try {
            SimpleGensGeneratorMode mode = SimpleGensGeneratorMode.valueOf(section.getString("mode", "REGEN"));
            long delayTicks = section.getLong("delay-ticks", 100);
            String worldName = section.getString("world");
            boolean broadcastEnabled = section.getBoolean("broadcast.enabled", false);
            String broadcastMessage = section.getString("broadcast.message", "<red>Generator reset!</red>");
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

            return new SimpleGensGenerator(id, mode, delayTicks, worldName, broadcastEnabled, broadcastMessage, icon, blocks);
        } catch (IllegalArgumentException e) {
            SimpleGensPlugin.getInstance().getLogger().warning("Failed to load generator '" + id + "': Invalid mode or other data. " + e.getMessage());
            return null;
        }
    }
}
