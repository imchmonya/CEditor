package org.cEditor.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.cEditor.CEditor;
import org.cEditor.utils.MessageUtilsSimple;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MessageManager {
    private final CEditor plugin;
    private FileConfiguration messagesConfig;
    private final Map<String, String> messages = new HashMap<>();
    
    public MessageManager(CEditor plugin) {
        this.plugin = plugin;
        loadMessages();
    }
    
    private void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        loadMessagesToMap("", messagesConfig);
    }
    
    private void loadMessagesToMap(String prefix, FileConfiguration section) {
        for (String key : section.getKeys(true)) {
            if (section.isString(key)) {
                String fullKey = prefix.isEmpty() ? key : prefix + "." + key;
                messages.put(fullKey, MessageUtilsSimple.format(section.getString(key)));
            }
        }
    }
    
    public String getMessage(String path) {
        return messages.getOrDefault(path, "§cСообщение не найдено: " + path);
    }
    
    public String getMessage(String path, String... placeholders) {
        String message = getMessage(path);
        
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                message = message.replace("%" + placeholders[i] + "%", placeholders[i + 1]);
            }
        }
        
        return message;
    }
    
    public void reloadMessages() {
        messages.clear();
        loadMessages();
    }
}
