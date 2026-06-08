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

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public class SimpleGensCommand implements CommandExecutor, TabCompleter {

    private final SimpleGensPlugin plugin;
    private final GUIManager guiManager;

    public SimpleGensCommand(SimpleGensPlugin plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("simplegens.gens.create") || sender.isOp()) completions.add("create");
            if (sender.hasPermission("simplegens.gens.remove") || sender.isOp()) completions.add("remove");
            if (sender.hasPermission("simplegens.gens.list") || sender.isOp()) completions.add("list");
            if (sender.hasPermission("simplegens.reload") || sender.isOp()) completions.add("reload");
            if (sender.hasPermission("simplegens.gens.setmessage") || sender.isOp()) completions.add("setmessage");
            if (sender.hasPermission("simplegens.gens.togglebroadcast") || sender.isOp()) completions.add("togglebroadcast");
            if (sender.hasPermission("simplegens.gens.gui") || sender.isOp()) completions.add("gui");
            if (sender.hasPermission("simplegens.particle") || sender.isOp()) completions.add("particle");
            if (sender.hasPermission("simplegens.help") || sender.isOp()) completions.add("help");

            return completions.stream()
                    .filter(c -> c.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("remove") && (sender.hasPermission("simplegens.gens.remove") || sender.isOp())) {
                return this.plugin.getGeneratorManager().getAllGenerators().stream()
                        .map(SimpleGensGenerator::getId)
                        .filter(id -> id.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (sub.equals("setmessage") && (sender.hasPermission("simplegens.gens.setmessage") || sender.isOp())) {
                return this.plugin.getGeneratorManager().getAllGenerators().stream()
                        .map(SimpleGensGenerator::getId)
                        .filter(id -> id.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (sub.equals("togglebroadcast") && (sender.hasPermission("simplegens.gens.togglebroadcast") || sender.isOp())) {
                return this.plugin.getGeneratorManager().getAllGenerators().stream()
                        .map(SimpleGensGenerator::getId)
                        .filter(id -> id.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (sub.equals("particle") && (sender.hasPermission("simplegens.particle") || sender.isOp())) {
                return this.plugin.getGeneratorManager().getAllGenerators().stream()
                        .map(SimpleGensGenerator::getId)
                        .filter(id -> id.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (sub.equals("create") && (sender.hasPermission("simplegens.gens.create") || sender.isOp())) {
                completions.add("<id>");
            }
            return completions.stream()
                    .filter(c -> c.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("create") && (sender.hasPermission("simplegens.gens.create") || sender.isOp())) {
            return List.of("10t", "1s", "5s", "10s", "1m", "5m", "1h").stream()
                    .filter(t -> t.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelpMessage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        // Permission checks
        boolean hasPermission = switch (subCommand) {
            case "create" -> sender.hasPermission("simplegens.gens.create");
            case "remove" -> sender.hasPermission("simplegens.gens.remove");
            case "list" -> sender.hasPermission("simplegens.gens.list");
            case "reload" -> sender.hasPermission("simplegens.reload");
            case "setmessage" -> sender.hasPermission("simplegens.gens.setmessage");
            case "togglebroadcast" -> sender.hasPermission("simplegens.gens.togglebroadcast");
            case "gui" -> sender.hasPermission("simplegens.gens.gui");
            case "particle" -> sender.hasPermission("simplegens.particle");
            default -> false;
        };

        if (!hasPermission && !sender.isOp()) {
            sender.sendMessage(plugin.getMessageManager().getComponent("no_permission", "<red>You do not have permission to use this command.</red>"));
            return true;
        }

        switch (subCommand) {
            case "create" -> this.handleCreateCommand(sender, args);
            case "remove" -> this.handleRemoveCommand(sender, args);
            case "list" -> this.handleListCommand(sender);
            case "reload" -> this.handleReloadCommand(sender);
            case "setmessage" -> this.handleSetMessageCommand(sender, args);
            case "togglebroadcast" -> this.handleToggleBroadcastCommand(sender, args);
            case "gui" -> this.handleGuiCommand(sender);
            case "particle" -> this.handleParticleCommand(sender, args);
            default -> this.sendHelpMessage(sender);
        }
        return true;
    }

    private void handleCreateCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().getComponent("player_only_command", "<red>This command can only be run by a player.</red>"));
            return;
        }

        if (args.length < 3) {
            player.sendMessage(plugin.getMessageManager().getComponent("create_usage", "<red>Usage: /gen create <id> <time></red>"));
            return;
        }

        String id = args[1];
        long delayTicks = TimeParser.parseTime(args[2]);

        if (delayTicks < 1) {
            player.sendMessage(plugin.getMessageManager().getComponent("create_invalid_time", "<red>Invalid time format or delay too short.</red>"));
            return;
        }

        try {
            LocalSession session = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player));
            if (session == null) {
                player.sendMessage(plugin.getMessageManager().getComponent("create_no_selection", "<red>You must make a WorldEdit selection first.</red>"));
                return;
            }
            Region selection = session.getSelection(BukkitAdapter.adapt(player.getWorld()));
            if (selection == null) {
                player.sendMessage(plugin.getMessageManager().getComponent("create_no_selection", "<red>You must make a WorldEdit selection first.</red>"));
                return;
            }

            if (plugin.getGeneratorManager().createGenerator(id, delayTicks, selection, player.getWorld())) {
                player.sendMessage(plugin.getMessageManager().getComponent("create_success", "<green>Generator '<yellow><id></yellow>' created successfully.</green>", Placeholder.unparsed("id", id)));
            } else {
                player.sendMessage(plugin.getMessageManager().getComponent("create_already_exists", "<red>Failed to create generator. It might already exist.</red>"));
            }
        } catch (IncompleteRegionException e) {
            player.sendMessage(plugin.getMessageManager().getComponent("create_no_selection", "<red>You must make a WorldEdit selection first.</red>"));
        }
    }

    private void handleRemoveCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getMessageManager().getComponent("remove_usage", "<red>Usage: /gen remove <id></red>"));
            return;
        }
        String id = args[1];
        if (plugin.getGeneratorManager().removeGenerator(id)) {
            sender.sendMessage(plugin.getMessageManager().getComponent("remove_success", "<green>Generator '<yellow><id></yellow>' removed.</green>", Placeholder.unparsed("id", id)));
        } else {
            sender.sendMessage(plugin.getMessageManager().getComponent("remove_not_found", "<red>Generator '<yellow><id></yellow>' not found.</red>", Placeholder.unparsed("id", id)));
        }
    }

    private void handleListCommand(CommandSender sender) {
        Collection<SimpleGensGenerator> generators = plugin.getGeneratorManager().getAllGenerators();
        if (generators == null || generators.isEmpty()) {
            sender.sendMessage(plugin.getMessageManager().getComponent("list_empty", "<yellow>No generators active.</yellow>"));
            return;
        }
        sender.sendMessage(plugin.getMessageManager().getComponent("list_header", "<gold>--- Generator List ---</gold>"));
        for (SimpleGensGenerator gen : generators) {
            sender.sendMessage(plugin.getMessageManager().getComponent("list_entry", "<yellow>ID: <id> <white>- <gray>Mode: <white><mode> <white>- <gray>Delay: <white><delay_ticks>t</white></gray>",
                    Placeholder.unparsed("id", gen.getId()),
                    Placeholder.unparsed("mode", gen.getMode() != null ? gen.getMode().name() : "UNKNOWN"),
                    Placeholder.unparsed("delay_ticks", String.valueOf(gen.getDelayTicks()))
            ));
        }
        sender.sendMessage(plugin.getMessageManager().getComponent("list_footer", "<gold>--------------------------</gold>"));
    }

    private void handleReloadCommand(CommandSender sender) {
        plugin.getConfigManager().reloadGeneratorsConfig();
        plugin.getMessageManager().reloadMessages();
        plugin.getGeneratorManager().reloadGenerators();
        sender.sendMessage(plugin.getMessageManager().getComponent("reload_success", "<green>Generators reloaded.</green>"));
    }

    private void handleSetMessageCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(plugin.getMessageManager().getComponent("setmessage_usage", "<red>Usage: /gen setmessage <id> <message></red>"));
            return;
        }
        String id = args[1];
        SimpleGensGenerator generator = plugin.getGeneratorManager().getGenerator(id);
        if (generator == null) {
            sender.sendMessage(plugin.getMessageManager().getComponent("remove_not_found", "<red>Generator '<yellow><id></yellow>' not found.</red>", Placeholder.unparsed("id", id)));
            return;
        }
        String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        generator.setBroadcastMessage(message);
        plugin.getGeneratorManager().updateGenerator(generator);
        sender.sendMessage(plugin.getMessageManager().getComponent("setmessage_success", "<green>Broadcast message updated for <yellow><id></yellow>.</green>", Placeholder.unparsed("id", id)));
    }

    private void handleToggleBroadcastCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getMessageManager().getComponent("togglebroadcast_usage", "<red>Usage: /gen togglebroadcast <id></red>"));
            return;
        }
        String id = args[1];
        SimpleGensGenerator generator = plugin.getGeneratorManager().getGenerator(id);
        if (generator == null) {
            sender.sendMessage(plugin.getMessageManager().getComponent("remove_not_found", "<red>Generator '<yellow><id></yellow>' not found.</red>", Placeholder.unparsed("id", id)));
            return;
        }
        generator.setBroadcastEnabled(!generator.isBroadcastEnabled());
        plugin.getGeneratorManager().updateGenerator(generator);
        sender.sendMessage(plugin.getMessageManager().getComponent("togglebroadcast_status", "<green>Broadcast for <yellow><id></yellow> is now <white><status></white>.</green>",
                Placeholder.unparsed("id", id),
                Placeholder.unparsed("status", generator.isBroadcastEnabled() ? "ENABLED" : "DISABLED")
        ));
    }

    private void handleGuiCommand(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().getComponent("player_only_command", "<red>This command can only be run by a player.</red>"));
            return;
        }
        if (guiManager == null) {
            player.sendMessage(plugin.getMessageManager().getComponent("gui_error", "<red>GUI manager is currently unavailable.</red>"));
            return;
        }
        guiManager.openGeneratorIndex(player);
    }

    private void handleParticleCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().getComponent("player_only_command", "<red>This command can only be run by a player.</red>"));
            return;
        }
        if (plugin.getParticleManager() == null) {
            player.sendMessage(plugin.getMessageManager().getComponent("particle_error", "<red>Particle manager is currently unavailable.</red>"));
            return;
        }
        if (args.length < 2) {
            player.sendMessage(plugin.getMessageManager().getComponent("particle_usage", "<red>Usage: /gen particle <id></red>"));
            return;
        }
        String id = args[1];
        SimpleGensGenerator generator = plugin.getGeneratorManager().getGenerator(id);
        if (generator == null) {
            player.sendMessage(plugin.getMessageManager().getComponent("particle_not_found", "<red>Generator '<yellow><id></yellow>' not found.</red>", Placeholder.unparsed("id", id)));
            return;
        }

        boolean enabled = plugin.getParticleManager().toggleParticleDisplay(player, generator);
        if (enabled) {
            player.sendMessage(plugin.getMessageManager().getComponent("particle_toggle_on", "<green>Particle border for generator '<yellow><id></yellow>' <white>ENABLED</white>.</green>", Placeholder.unparsed("id", id)));
        } else {
            player.sendMessage(plugin.getMessageManager().getComponent("particle_toggle_off", "<red>Particle border for generator '<yellow><id></yellow>' <white>DISABLED</white>.</red>", Placeholder.unparsed("id", id)));
        }
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(plugin.getMessageManager().getComponent("help_header", "<gold>--- Generator Commands ---</gold>"));
        if (sender.hasPermission("simplegens.gens.create") || sender.isOp()) {
            sender.sendMessage(plugin.getMessageManager().getComponent("help_create", "<yellow>/gen create <id> <time> <white>- <gray>Create a new generator from your WorldEdit selection.</gray>"));
        }
        if (sender.hasPermission("simplegens.gens.remove") || sender.isOp()) {
            sender.sendMessage(plugin.getMessageManager().getComponent("help_remove", "<yellow>/gen remove <id> <white>- <gray>Remove an existing generator.</gray>"));
        }
        if (sender.hasPermission("simplegens.gens.list") || sender.isOp()) {
            sender.sendMessage(plugin.getMessageManager().getComponent("help_list", "<yellow>/gen list <white>- <gray>List all active generators.</gray>"));
        }
        if (sender.hasPermission("simplegens.reload") || sender.isOp()) {
            sender.sendMessage(plugin.getMessageManager().getComponent("help_reload", "<yellow>/gen reload <white>- <gray>Reload generator configurations.</gray>"));
        }
        if (sender.hasPermission("simplegens.gens.setmessage") || sender.isOp()) {
            sender.sendMessage(plugin.getMessageManager().getComponent("help_setmessage", "<yellow>/gen setmessage <id> <message> <white>- <gray>Set the broadcast message for a generator (MiniMessage).</gray>"));
        }
        if (sender.hasPermission("simplegens.gens.togglebroadcast") || sender.isOp()) {
            sender.sendMessage(plugin.getMessageManager().getComponent("help_togglebroadcast", "<yellow>/gen togglebroadcast <id> <white>- <gray>Toggle broadcast for a generator.</gray>"));
        }
        if (sender.hasPermission("simplegens.gens.gui") || sender.isOp()) {
            sender.sendMessage(plugin.getMessageManager().getComponent("help_gui", "<yellow>/gen gui <white>- <gray>Opens the interactive visual generator management interface.</gray>"));
        }
        if (sender.hasPermission("simplegens.particle") || sender.isOp()) {
            sender.sendMessage(plugin.getMessageManager().getComponent("help_particle", "<yellow>/gen particle <id> <white>- <gray>Toggles particle boundary display for a generator.</gray>"));
        }
        sender.sendMessage(plugin.getMessageManager().getComponent("help_help", "<yellow>/gen help <white>- <gray>Opens this command help menu.</gray>"));
        sender.sendMessage(plugin.getMessageManager().getComponent("help_footer", "<gold>--------------------------</gold>"));
    }
}
