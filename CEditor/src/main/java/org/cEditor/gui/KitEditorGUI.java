package org.cEditor.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.cEditor.CEditor;
import org.cEditor.managers.KitManager;
import org.cEditor.models.Kit;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class KitEditorGUI implements Listener {
    private final CEditor plugin;
    private final KitManager kitManager;
    private final Player player;
    private final String kitName;
    private final Inventory inventory;
    private final Map<Integer, Boolean> editableSlots;
    private final Map<Integer, Boolean> armorSlots;

    public KitEditorGUI(CEditor plugin, KitManager kitManager, Player player, String kitName) {
        this.plugin = plugin;
        this.kitManager = kitManager;
        this.player = player;
        this.kitName = kitName;
        this.inventory = Bukkit.createInventory(null, 54, "§0Редактор кита: " + kitName);
        this.editableSlots = new HashMap<>();
        this.armorSlots = new HashMap<>();
        
        initializeSlots();
        loadKitToInventory();
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void initializeSlots() {
        for (int i = 0; i < 36; i++) {
            editableSlots.put(i, true);
        }
        
        for (int i = 36; i < 54; i++) {
            editableSlots.put(i, false);
        }
        editableSlots.put(49, true);
        for (int i = 36; i < 45; i++) {
            inventory.setItem(i, createGlassPanel());
        }
        
        inventory.setItem(45, createArmorSlot(Material.LEATHER_BOOTS, "Ботинки"));
        inventory.setItem(46, createArmorSlot(Material.LEATHER_LEGGINGS, "Штаны"));
        inventory.setItem(47, createArmorSlot(Material.LEATHER_CHESTPLATE, "Нагрудник"));
        inventory.setItem(48, createArmorSlot(Material.LEATHER_HELMET, "Шлем"));
        inventory.setItem(49, createArmorSlot(Material.SHIELD, "Вторая рука"));
        inventory.setItem(50, createGlassPanel());
        inventory.setItem(51, createSaveButton());
        inventory.setItem(52, createResetButton());
        inventory.setItem(53, createCancelButton());
    }
    private void loadKitToInventory() {
        Kit kit = kitManager.getEditingKit(player.getUniqueId(), kitName);
        if (kit == null) return;
        for (Map.Entry<Integer, ItemStack> entry : kit.getItems().entrySet()) {
            if (entry.getKey() < 36) {
                inventory.setItem(entry.getKey(), entry.getValue() != null ? entry.getValue().clone() : null);
            }
        }
        ItemStack[] armor = kit.getArmor();
        inventory.setItem(45, armor[3] != null ? armor[3].clone() : null);
        inventory.setItem(46, armor[2] != null ? armor[2].clone() : null);
        inventory.setItem(47, armor[1] != null ? armor[1].clone() : null);
        inventory.setItem(48, armor[0] != null ? armor[0].clone() : null);
        ItemStack offhand = kit.getOffhandItem();
        if (offhand != null) {
            inventory.setItem(49, offhand.clone());
        }
    }

    private ItemStack createGlassPanel() {
        ItemStack panel = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        ItemMeta meta = panel.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§5ꜰᴀᴡᴇ");
            panel.setItemMeta(meta);
        }
        return panel;
    }

    private ItemStack createArmorSlot(Material material, String name) {
        ItemStack slot = new ItemStack(material);
        ItemMeta meta = slot.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§7" + name);
            if (material == Material.SHIELD) {
                meta.setLore(Arrays.asList("§7Слот для второй руки", "§aМожно редактировать"));
            } else {
                meta.setLore(Arrays.asList("§cЭтот слот нельзя редактировать"));
            }
            slot.setItemMeta(meta);
        }
        return slot;
    }


    private ItemStack createSaveButton() {
        ItemStack button = new ItemStack(Material.GREEN_CONCRETE);
        ItemMeta meta = button.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getMessageManager().getMessage("editor.buttons.save.display_name"));
            button.setItemMeta(meta);
        }
        return button;
    }

    private ItemStack createResetButton() {
        ItemStack button = new ItemStack(Material.ORANGE_CONCRETE);
        ItemMeta meta = button.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getMessageManager().getMessage("editor.buttons.reset.display_name"));
            button.setItemMeta(meta);
        }
        return button;
    }

    private ItemStack createCancelButton() {
        ItemStack button = new ItemStack(Material.RED_CONCRETE);
        ItemMeta meta = button.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(plugin.getMessageManager().getMessage("editor.buttons.cancel.display_name"));
            button.setItemMeta(meta);
        }
        return button;
    }

    public void open() {
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) {
            return;
        }

        if (event.getClickedInventory() != inventory) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        int slot = event.getSlot();

        if (slot == 44 || slot == 49) {
            if (slot == 44) {
                return;
            }
            event.setCancelled(false);
            return;
        }

        if (slot == 51) {
            saveKit();
            player.closeInventory();
            return;
        }

        if (slot == 52) {
            resetKit();
            return;
        }

        if (slot == 53) {
            player.closeInventory();
            return;
        }

        if (editableSlots.containsKey(slot) && editableSlots.get(slot)) {
            handleItemClick(event);
        }
        if (slot == 49) {
            handleItemClick(event);
        }
    }

    private void handleItemClick(InventoryClickEvent event) {
        if (event.isShiftClick()) {
            return;
        }

        if (event.getClickedInventory() != inventory) {
            return;
        }

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        if (cursor != null && cursor.getType() != Material.AIR) {
            if (current == null || current.getType() == Material.AIR) {
                event.setCurrentItem(cursor.clone());
                event.setCursor(null);
            } else {
                event.setCursor(current.clone());
                event.setCurrentItem(cursor.clone());
            }
        } else if (current != null && current.getType() != Material.AIR) {
            event.setCursor(current.clone());
            event.setCurrentItem(null);
        }

        updateEditingKit();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) {
            return;
        }

        if (event.getPlayer().equals(player)) {
            if (player.getItemOnCursor() != null && player.getItemOnCursor().getType() != Material.AIR) {
                player.setItemOnCursor(null);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    player.openInventory(inventory);
                    player.sendMessage(plugin.getMessageManager().getMessage("editor.close_with_item"));
                }, 1L);
                return;
            }
            
            HandlerList.unregisterAll(this);
            kitManager.stopEditing(player.getUniqueId(), kitName);
        }
    }

    private void updateEditingKit() {
        Map<Integer, ItemStack> items = new HashMap<>();
        for (int i = 0; i < 36; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                items.put(i, item.clone());
            }
        }
        ItemStack[] armor = new ItemStack[4];
        armor[3] = inventory.getItem(45);
        armor[2] = inventory.getItem(46);
        armor[1] = inventory.getItem(47);
        armor[0] = inventory.getItem(48);

        ItemStack offhand = inventory.getItem(49);
        if (offhand != null && offhand.getType() == Material.PURPLE_STAINED_GLASS_PANE) {
            offhand = null;
        }
        Kit updatedKit = new Kit(kitName, items, armor, offhand);
        kitManager.updateEditingKit(player.getUniqueId(), kitName, updatedKit);
    }
    private void saveKit() {
        updateEditingKit();
        if (kitManager.saveEditedKit(player.getUniqueId(), kitName)) {
            player.sendMessage(plugin.getMessageManager().getMessage("editor.save.success", "kit", kitName));
        } else {
            player.sendMessage(plugin.getMessageManager().getMessage("editor.save.failed"));
        }
    }

    private void resetKit() {
        kitManager.resetEditingKit(player.getUniqueId(), kitName);
        loadKitToInventory();
        player.sendMessage(plugin.getMessageManager().getMessage("editor.reset.success"));
    }
}
