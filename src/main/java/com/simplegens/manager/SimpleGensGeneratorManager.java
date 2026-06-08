package com.simplegens.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitTask;

import com.simplegens.SimpleGensPlugin;
import com.simplegens.data.SimpleGensBlockBlueprint;
import com.simplegens.data.SimpleGensGenerator;
import com.simplegens.data.SimpleGensGeneratorMode;
import com.simplegens.task.SimpleGensRegenTask;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;

public class SimpleGensGeneratorManager {

    private final SimpleGensPlugin plugin;
    private final Map<String, SimpleGensGenerator> generators;
    private final Map<String, BukkitTask> activeTasks;

    public SimpleGensGeneratorManager(SimpleGensPlugin plugin) {
        this.plugin = plugin;
        this.generators = new HashMap<>();
        this.activeTasks = new HashMap<>();
    }

    public void loadGenerators() {
        generators.clear();
        activeTasks.values().forEach(BukkitTask::cancel);
        activeTasks.clear();

        ConfigurationSection generatorsSection = plugin.getConfigManager().getGeneratorsConfig().getConfigurationSection("generators");
        if (generatorsSection == null) {
            plugin.getLogger().info("No generators found in generators.yml.");
            return;
        }

        for (String id : generatorsSection.getKeys(false)) {
            ConfigurationSection genSection = generatorsSection.getConfigurationSection(id);
            if (genSection != null) {
                SimpleGensGenerator generator = SimpleGensGenerator.load(id, genSection);
                if (generator != null) {
                    generators.put(id, generator);
                    if (generator.getMode() == SimpleGensGeneratorMode.REGEN) {
                        startRegenTask(generator);
                    }
                }
            }
        }
        plugin.getLogger().info("Loaded " + generators.size() + " generators.");
    }

    public void saveGenerators() {
        ConfigurationSection generatorsSection = plugin.getConfigManager().getGeneratorsConfig().createSection("generators");
        for (SimpleGensGenerator generator : generators.values()) {
            generator.save(generatorsSection.createSection(generator.getId()));
        }
        plugin.getConfigManager().saveGeneratorsConfig();
        plugin.getLogger().info("Saved " + generators.size() + " generators.");
    }

    public void reloadGenerators() {
        plugin.getConfigManager().reloadGeneratorsConfig();
        loadGenerators();
        plugin.getLogger().info("Generators reloaded.");
    }

    public SimpleGensGenerator getGenerator(String id) {
        return generators.get(id);
    }

    public Collection<SimpleGensGenerator> getAllGenerators() {
        return generators.values().stream()
                .sorted(Comparator.comparing(SimpleGensGenerator::getId))
                .collect(Collectors.toList());
    }

    public boolean createGenerator(String id, long delayTicks, Region selection, World world) {
        if (generators.containsKey(id)) {
            return false;
        }

        List<SimpleGensBlockBlueprint> blueprints = new ArrayList<>();
        for (BlockVector3 blockVector : selection) {
        Block block = BukkitAdapter.adapt(world, blockVector).getBlock();
            if (block.getType().isAir()) {
                continue;
            }
            blueprints.add(new SimpleGensBlockBlueprint(block));
        }

        if (blueprints.isEmpty()) {
            return false;
        }

        SimpleGensGeneratorMode mode = (delayTicks < 20) ? SimpleGensGeneratorMode.STATIC : SimpleGensGeneratorMode.REGEN;
        SimpleGensGenerator generator = new SimpleGensGenerator(id, mode, delayTicks, world.getName(), false, "<red>Generator reset!</red>", Material.STONE, blueprints);
        generators.put(id, generator);

        if (mode == SimpleGensGeneratorMode.REGEN) {
            startRegenTask(generator);
        }

        saveGenerators();
        return true;
    }

    public boolean removeGenerator(String id) {
        SimpleGensGenerator generator = generators.remove(id);
        if (generator != null) {
            // Safely remove and cancel the task
            BukkitTask task = activeTasks.remove(id);
            if (task != null) {
                try {
                    task.cancel();
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to cancel task for " + id + ": " + e.getMessage());
                }
            }
            saveGenerators();
            return true;
        }
        return false;
    }

    public void updateGenerator(SimpleGensGenerator generator) {
        if (generator.getMode() == SimpleGensGeneratorMode.REGEN) {
            if (!activeTasks.containsKey(generator.getId())) {
                startRegenTask(generator);
            } else {
                BukkitTask task = activeTasks.remove(generator.getId());
                if (task != null) task.cancel();
                startRegenTask(generator);
            }
        } else {
            BukkitTask task = activeTasks.remove(generator.getId());
            if (task != null) {
                task.cancel();
            }
        }
        saveGenerators();
    }

    private void startRegenTask(SimpleGensGenerator generator) {
        if (generator.getMode() != SimpleGensGeneratorMode.REGEN) return;

        BukkitTask existingTask = activeTasks.remove(generator.getId());
        if (existingTask != null) {
            existingTask.cancel();
        }

        SimpleGensRegenTask regenTask = new SimpleGensRegenTask(plugin, generator);
        BukkitTask task = regenTask.runTaskTimer(plugin, generator.getDelayTicks(), generator.getDelayTicks());
        activeTasks.put(generator.getId(), task);
        plugin.getLogger().info("Started REGEN task for generator '" + generator.getId() + "' with delay " + generator.getDelayTicks() + " ticks.");
    }

    public void cancelAllTasks() {
        activeTasks.values().forEach(BukkitTask::cancel);
        activeTasks.clear();
    }
}
