package com.simplegens.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.simplegens.SimpleGensPlugin;
import com.simplegens.data.SimpleGensGenerator;
import com.simplegens.gui.GUIManager;
import com.simplegens.util.TimeParser;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class SimpleGensCommand implements CommandExecutor, TabCompleter {

    private final SimpleGensPlugin plugin;
    private final GUIManager guiManager;
    private final MiniMessage miniMessage;

    public SimpleGensCommand(SimpleGensPlugin plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.miniMessage = MiniMessage.miniMessage();
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            return List.of("create", "remove", "list", "reload", "setmessage", "togglebroadcast", "gui", "help").stream()
                    .filter(c -> c.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("remove") || sub.equals("setmessage") || sub.equals("togglebroadcast")) {
                return plugin.getGeneratorManager().getAllGenerators().stream()
                        .map(SimpleGensGenerator::getId)
                        .filter(id -> id.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (sub.equals("create")) {
                completions.add("<id>");
            }
            return completions.stream()
                    .filter(c -> c.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            return List.of("10t", "1s", "5s", "10s", "1m", "5m", "1h").stream()
                    .filter(t -> t.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("gen.admin")) {
            sender.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> handleCreateCommand(sender, args);
            case "remove" -> handleRemoveCommand(sender, args);
            case "list" -> handleListCommand(sender);
            case "reload" -> handleReloadCommand(sender);
            case "setmessage" -> handleSetMessageCommand(sender, args);
            case "togglebroadcast" -> handleToggleBroadcastCommand(sender, args);
            case "gui" -> handleGuiCommand(sender);
            case "help" -> sendHelpMessage(sender);
            default -> sendHelpMessage(sender);
        }
        return true;
    }

    private void handleCreateCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by a player.", NamedTextColor.RED));
            return;
        }

        if (args.length < 3) {
            player.sendMessage(Component.text("Usage: /gen create <id> <time>", NamedTextColor.RED));
            return;
        }

        String id = args[1];
        long delayTicks = TimeParser.parseTime(args[2]);

        if (delayTicks < 1) {
            player.sendMessage(Component.text("Invalid time format or delay too short.", NamedTextColor.RED));
            return;
        }

        try {
            LocalSession session = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player));
            Region selection = session.getSelection(BukkitAdapter.adapt(player.getWorld()));
            
            if (plugin.getGeneratorManager().createGenerator(id, delayTicks, selection, player.getWorld())) {
                player.sendMessage(Component.text("Generator '" + id + "' created successfully.", NamedTextColor.GREEN));
            } else {
                player.sendMessage(Component.text("Failed to create generator. It might already exist.", NamedTextColor.RED));
            }
        } catch (IncompleteRegionException e) {
            player.sendMessage(Component.text("You must make a WorldEdit selection first.", NamedTextColor.RED));
        }
    }

    private void handleRemoveCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /gen remove <id>", NamedTextColor.RED));
            return;
        }
        if (plugin.getGeneratorManager().removeGenerator(args[1])) {
            sender.sendMessage(Component.text("Generator '" + args[1] + "' removed.", NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("Generator not found.", NamedTextColor.RED));
        }
    }

    private void handleListCommand(CommandSender sender) {
        Collection<SimpleGensGenerator> generators = plugin.getGeneratorManager().getAllGenerators();
        if (generators.isEmpty()) {
            sender.sendMessage(Component.text("No generators active.", NamedTextColor.YELLOW));
            return;
        }
        sender.sendMessage(miniMessage.deserialize("<gold>--- Generator Commands ---</gold>"));
        for (SimpleGensGenerator gen : generators) {
            sender.sendMessage(miniMessage.deserialize("<yellow>ID: " + gen.getId() + " <white>- <gray>Mode: <white>" + gen.getMode() + " <white>- <gray>Delay: <white>" + gen.getDelayTicks() + "t</white></gray>"));
        }
        sender.sendMessage(miniMessage.deserialize("<gold>--------------------------</gold>"));
    }

    private void handleReloadCommand(CommandSender sender) {
        plugin.getGeneratorManager().reloadGenerators();
        sender.sendMessage(Component.text("Generators reloaded.", NamedTextColor.GREEN));
    }

    private void handleSetMessageCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /gen setmessage <id> <message>", NamedTextColor.RED));
            return;
        }
        SimpleGensGenerator generator = plugin.getGeneratorManager().getGenerator(args[1]);
        if (generator == null) {
            sender.sendMessage(Component.text("Generator not found.", NamedTextColor.RED));
            return;
        }
        String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        generator.setBroadcastMessage(message);
        plugin.getGeneratorManager().updateGenerator(generator);
        sender.sendMessage(Component.text("Message updated for " + args[1], NamedTextColor.GREEN));
    }

    private void handleToggleBroadcastCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /gen togglebroadcast <id>", NamedTextColor.RED));
            return;
        }
        SimpleGensGenerator generator = plugin.getGeneratorManager().getGenerator(args[1]);
        if (generator == null) {
            sender.sendMessage(Component.text("Generator not found.", NamedTextColor.RED));
            return;
        }
        generator.setBroadcastEnabled(!generator.isBroadcastEnabled());
        plugin.getGeneratorManager().updateGenerator(generator);
        sender.sendMessage(Component.text("Broadcast is now " + (generator.isBroadcastEnabled() ? "ENABLED" : "DISABLED"), NamedTextColor.GREEN));
    }

    private void handleGuiCommand(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by a player.", NamedTextColor.RED));
            return;
        }
        guiManager.openGeneratorIndex(player);
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(miniMessage.deserialize("<gold>--- Generator Commands ---</gold>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/gen create <id> <time> <white>- <gray>Create a new generator from your WorldEdit selection.</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/gen remove <id> <white>- <gray>Remove an existing generator.</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/gen list <white>- <gray>List all active generators.</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/gen reload <white>- <gray>Reload generator configurations.</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/gen setmessage <id> <message> <white>- <gray>Set the broadcast message for a generator (MiniMessage).</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/gen togglebroadcast <id> <white>- <gray>Toggle broadcast for a generator.</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/gen gui <white>- <gray>Opens the interactive visual generator management interface.</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/gen help <white>- <gray>Opens this command help menu.</gray>"));
        sender.sendMessage(miniMessage.deserialize("<gold>--------------------------</gold>"));
    }
}
