package com.simplegens.config;

import com.simplegens.SimpleGensPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class MessageManager {

    private final SimpleGensPlugin plugin;
    private final MiniMessage miniMessage;
    private Map<String, String> messages;
    private File messagesFile;
    private FileConfiguration messagesConfig;

    public MessageManager(SimpleGensPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.messages = new HashMap<>();
    }

    public void loadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            try (InputStream in = plugin.getResource("messages.yml")) {
                if (in != null) {
                    Files.copy(in, messagesFile.toPath());
                } else {
                    messagesFile.createNewFile();
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create messages.yml: " + e.getMessage());
            }
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        loadMessagesFromConfig();
    }

    private void loadMessagesFromConfig() {
        messages.clear();
        for (String key : messagesConfig.getKeys(true)) {
            messages.put(key, messagesConfig.getString(key));
        }
        plugin.getLogger().info("Loaded " + messages.size() + " messages from messages.yml.");
    }

    public Component getComponent(String key, String fallback, TagResolver... resolvers) {
        String message = messages.getOrDefault(key, fallback);
        return miniMessage.deserialize(message, resolvers);
    }

    public Component getComponent(String key, String fallback) {
        String message = messages.getOrDefault(key, fallback);
        return miniMessage.deserialize(message);
    }

    public String getString(String key, String fallback) {
        return messages.getOrDefault(key, fallback);
    }

    public List<Component> getComponentList(String key, List<Component> fallback, TagResolver... resolvers) {
        List<String> messagesList = messagesConfig.getStringList(key);
        if (messagesList.isEmpty()) {
            return fallback;
        }
        return messagesList.stream()
                .map(s -> miniMessage.deserialize(s, resolvers))
                .collect(Collectors.toList());
    }

    public List<Component> getComponentList(String key, List<Component> fallback) {
        List<String> messagesList = messagesConfig.getStringList(key);
        if (messagesList.isEmpty()) {
            return fallback;
        }
        return messagesList.stream()
                .map(miniMessage::deserialize)
                .collect(Collectors.toList());
    }

    public void reloadMessages() {
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        loadMessagesFromConfig();
    }

    public MiniMessage getMiniMessage() {
        return miniMessage;
    }
}
