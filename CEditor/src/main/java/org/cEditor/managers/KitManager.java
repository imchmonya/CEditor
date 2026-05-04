package org.cEditor.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.cEditor.CEditor;
import org.cEditor.models.Kit;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.String;
import java.util.UUID;

public class KitManager {
    private final CEditor plugin;
    private final Map<String, Kit> kits;
    private final Map<UUID, Map<String, Kit>> editingKits;
    private final Map<UUID, Map<String, Kit>> playerKits;
    private final File kitsFile;

    public KitManager(CEditor plugin) {
        this.plugin = plugin;
        this.kits = new ConcurrentHashMap<>();
        this.editingKits = new ConcurrentHashMap<>();
        this.playerKits = new ConcurrentHashMap<>();
        this.kitsFile = new File(plugin.getDataFolder(), "kits.yml");
        
        loadKits();
    }

    public boolean createKit(String name, org.bukkit.inventory.PlayerInventory inventory, UUID playerId) {
        if (kits.containsKey(name)) {
            return false;
        }
        
        Kit kit = new Kit(name, inventory);
        kits.put(name, kit);
        saveKits();
        return true;
    }

    public boolean deleteKit(String name, UUID playerId) {
        if (!kits.containsKey(name)) {
            return false;
        }
        
        kits.remove(name);
        saveKits();
        return true;
    }

    public Kit getKit(String name, UUID playerId) {
        String personalKitName = name + "_" + playerId.toString();
        if (kits.containsKey(personalKitName)) {
            return kits.get(personalKitName);
        }
        return kits.get(name);
    }

    public boolean kitExists(String name, UUID playerId) {
        return kits.containsKey(name);
    }

    public Map<String, Kit> getAllKits(UUID playerId) {
        return new HashMap<>(kits);
    }

    public void startEditing(UUID playerId, String kitName) {
        Kit kit = getKit(kitName, playerId);
        if (kit != null) {
            if (!editingKits.containsKey(playerId)) {
                editingKits.put(playerId, new ConcurrentHashMap<>());
            }
            editingKits.get(playerId).put(kitName, kit.clone());
        }
    }

    public Kit getEditingKit(UUID playerId, String kitName) {
        Map<String, Kit> playerKits = editingKits.get(playerId);
        if (playerKits != null) {
            return playerKits.get(kitName);
        }
        return null;
    }

    public void updateEditingKit(UUID playerId, String kitName, Kit updatedKit) {
        if (!editingKits.containsKey(playerId)) {
            editingKits.put(playerId, new ConcurrentHashMap<>());
        }
        editingKits.get(playerId).put(kitName, updatedKit);
    }

    public void resetEditingKit(UUID playerId, String kitName) {
        Kit originalKit = getKit(kitName, playerId);
        if (originalKit != null) {
            if (!editingKits.containsKey(playerId)) {
                editingKits.put(playerId, new ConcurrentHashMap<>());
            }
            editingKits.get(playerId).put(kitName, originalKit.clone());
        }
    }

    public boolean saveEditedKit(UUID playerId, String kitName) {
        Map<String, Kit> playerKits = editingKits.get(playerId);
        if (playerKits != null) {
            Kit editedKit = playerKits.get(kitName);
            if (editedKit != null) {
                kits.put(kitName + "_" + playerId.toString(), editedKit);
                saveKits();
                return true;
            }
        }
        return false;
    }

    public void stopEditing(UUID playerId, String kitName) {
        Map<String, Kit> playerKits = editingKits.get(playerId);
        if (playerKits != null) {
            playerKits.remove(kitName);
            if (playerKits.isEmpty()) {
                editingKits.remove(playerId);
            }
        }
    }

    private void loadKits() {
        if (!kitsFile.exists()) {
            try {
                kitsFile.getParentFile().mkdirs();
                kitsFile.createNewFile();
            } catch (IOException e) {
            }
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(kitsFile);
        ConfigurationSection kitsSection = config.getConfigurationSection("kits");
        
        if (kitsSection != null) {
            for (String kitName : kitsSection.getKeys(false)) {
                ConfigurationSection kitSection = kitsSection.getConfigurationSection(kitName);
                if (kitSection != null) {
                    Kit kit = deserializeKit(kitSection);
                    if (kit != null) {
                        kits.put(kitName, kit);
                    }
                }
            }
        }
    }

    private void saveKits() {
        try {
            FileConfiguration config = new YamlConfiguration();
            ConfigurationSection kitsSection = config.createSection("kits");
            
            for (Map.Entry<String, Kit> entry : kits.entrySet()) {
                ConfigurationSection kitSection = kitsSection.createSection(entry.getKey());
                serializeKit(entry.getValue(), kitSection);
            }
            
            config.save(kitsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save kits: " + e.getMessage());
        }
    }

    private void serializeKit(Kit kit, ConfigurationSection section) {
        ConfigurationSection itemsSection = section.createSection("items");
        for (Map.Entry<Integer, org.bukkit.inventory.ItemStack> entry : kit.getItems().entrySet()) {
            itemsSection.set(String.valueOf(entry.getKey()), entry.getValue());
        }
        
        ConfigurationSection armorSection = section.createSection("armor");
        org.bukkit.inventory.ItemStack[] armor = kit.getArmor();
        for (int i = 0; i < armor.length; i++) {
            if (armor[i] != null) {
                armorSection.set(String.valueOf(i), armor[i]);
            }
        }
        
        if (kit.getOffhandItem() != null) {
            section.set("offhand", kit.getOffhandItem());
        }
    }

    private Kit deserializeKit(ConfigurationSection section) {
        try {
            Map<Integer, org.bukkit.inventory.ItemStack> items = new HashMap<>();
            ConfigurationSection itemsSection = section.getConfigurationSection("items");
            
            if (itemsSection != null) {
                for (String key : itemsSection.getKeys(false)) {
                    try {
                        int slot = Integer.parseInt(key);
                        org.bukkit.inventory.ItemStack item = itemsSection.getItemStack(key);
                        if (item != null) {
                            items.put(slot, item);
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            }
            
            org.bukkit.inventory.ItemStack[] armor = new org.bukkit.inventory.ItemStack[4];
            ConfigurationSection armorSection = section.getConfigurationSection("armor");
            
            if (armorSection != null) {
                for (String key : armorSection.getKeys(false)) {
                    try {
                        int slot = Integer.parseInt(key);
                        if (slot >= 0 && slot < 4) {
                            armor[slot] = armorSection.getItemStack(key);
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            }
            
            org.bukkit.inventory.ItemStack offhand = section.getItemStack("offhand");
            
            return new Kit(section.getName(), items, armor, offhand);
        } catch (Exception e) {
            return null;
        }
    }
}
