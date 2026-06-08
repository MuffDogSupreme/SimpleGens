package com.simplegens.data;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.simplegens.SimpleGensPlugin;

public class SimpleGensBlockBlueprint {
    private final int x;
    private final int y;
    private final int z;
    private Material material; // Made non-final to allow editing

    public SimpleGensBlockBlueprint(int x, int y, int z, Material material) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.material = material;
    }

    public SimpleGensBlockBlueprint(Block block) {
        this(block.getX(), block.getY(), block.getZ(), block.getType());
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public String serialize() {
        return x + "," + y + "," + z + ":" + material.name();
    }

    public static SimpleGensBlockBlueprint deserialize(String serialized) {
        try {
            String[] parts = serialized.split(":");
            if (parts.length != 2) return null;

            Material material = Material.valueOf(parts[1].toUpperCase());

            String[] coords = parts[0].split(",");
            if (coords.length != 3) return null;

            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            int z = Integer.parseInt(coords[2]);

            return new SimpleGensBlockBlueprint(x, y, z, material);
        } catch (IllegalArgumentException e) {
            SimpleGensPlugin.getInstance().getLogger().warning("Failed to deserialize BlockBlueprint: " + serialized + " - " + e.getMessage());
            return null;
        }
    }
}
