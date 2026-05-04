package org.cEditor.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.cEditor.CEditor;
import org.cEditor.gui.KitEditorGUI;
import org.cEditor.managers.KitManager;
import org.cEditor.managers.MessageManager;
import org.cEditor.models.Kit;

import java.util.Map;

public class KitCommand implements CommandExecutor {
    private final CEditor plugin;
    private final KitManager kitManager;
    private final MessageManager messageManager;

    public KitCommand(CEditor plugin, KitManager kitManager, MessageManager messageManager) {
        this.plugin = plugin;
        this.kitManager = kitManager;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messageManager.getMessage("kit.general.no_permission"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("ceditor.kit")) {
            player.sendMessage(messageManager.getMessage("kit.general.no_permission"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(messageManager.getMessage("kit.general.usage"));
            player.sendMessage(messageManager.getMessage("kit.general.commands.create"));
            player.sendMessage(messageManager.getMessage("kit.general.commands.delete"));
            player.sendMessage(messageManager.getMessage("kit.general.commands.edit"));
            player.sendMessage(messageManager.getMessage("kit.general.commands.give"));
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "create":
                return handleCreate(player, args);
            case "delete":
                return handleDelete(player, args);
            case "edit":
                return handleEdit(player, args);
            default:
                return handleGiveKit(player, args);
        }
    }

    private boolean handleCreate(Player player, String[] args) {
        if (!player.hasPermission("ceditor.kit.create")) {
            player.sendMessage(messageManager.getMessage("kit.create.no_permission"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(messageManager.getMessage("kit.create.usage"));
            return true;
        }
        String kitName = args[1];
        if (kitManager.kitExists(kitName, null)) {
            player.sendMessage(messageManager.getMessage("kit.create.exists", "kit", kitName));
            return true;
        }

        if (kitManager.createKit(kitName, player.getInventory(), player.getUniqueId())) {
            player.sendMessage(messageManager.getMessage("kit.create.success", "kit", kitName));
        } else {
            player.sendMessage(messageManager.getMessage("kit.create.failed"));
        }

        return true;
    }

    private boolean handleDelete(Player player, String[] args) {
        if (!player.hasPermission("ceditor.kit.delete")) {
            player.sendMessage(messageManager.getMessage("kit.delete.no_permission"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(messageManager.getMessage("kit.delete.usage"));
            return true;
        }

        String kitName = args[1];

        if (!kitManager.kitExists(kitName, null)) {
            player.sendMessage(messageManager.getMessage("kit.delete.not_found", "kit", kitName));
            return true;
        }

        if (kitManager.deleteKit(kitName, player.getUniqueId())) {
            player.sendMessage(messageManager.getMessage("kit.delete.success", "kit", kitName));
        } else {
            player.sendMessage(messageManager.getMessage("kit.delete.failed"));
        }

        return true;
    }

    private boolean handleEdit(Player player, String[] args) {
        if (!player.hasPermission("ceditor.kit.edit")) {
            player.sendMessage(messageManager.getMessage("kit.edit.no_permission"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(messageManager.getMessage("kit.edit.usage"));
            return true;
        }

        String kitName = args[1];

        if (!kitManager.kitExists(kitName, null)) {
            player.sendMessage(messageManager.getMessage("kit.edit.not_found", "kit", kitName));
            return true;
        }

        kitManager.startEditing(player.getUniqueId(), kitName);
        KitEditorGUI editorGUI = new KitEditorGUI(plugin, kitManager, player, kitName);
        editorGUI.open();

        return true;
    }

    private boolean handleGiveKit(Player player, String[] args) {
        if (!player.hasPermission("ceditor.kit.give")) {
            player.sendMessage(messageManager.getMessage("kit.give.no_permission"));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(messageManager.getMessage("kit.give.usage"));
            return true;
        }

        String kitName = args[0];
        Player target;
        String personalKitPermission = "ceditor.kit." + kitName.toLowerCase();
        boolean hasPersonalKitPermission = player.hasPermission(personalKitPermission);
        
        if (args.length >= 2) {
            String targetName = args[1];
            target = plugin.getServer().getPlayer(targetName);
            if (target == null) {
                player.sendMessage(messageManager.getMessage("kit.give.player_not_found", "player", targetName));
                return true;
            }
        } else {
            target = player;
        }
        Kit kit = kitManager.getKit(kitName, target.getUniqueId());
        if (kit == null && !hasPersonalKitPermission) {
            kit = kitManager.getKit(kitName, null);
        }

        if (kit == null) {
            player.sendMessage(messageManager.getMessage("kit.give.not_found", "kit", kitName));
            return true;
        }

        giveKitToPlayer(target, kit);

        if (target.equals(player)) {
            if (hasPersonalKitPermission) {
                player.sendMessage(messageManager.getMessage("kit.give.success_self_personal", "kit", kitName));
            } else {
                player.sendMessage(messageManager.getMessage("kit.give.success_self", "kit", kitName));
            }
        } else {
            player.sendMessage(messageManager.getMessage("kit.give.success_other", "kit", kitName, "player", target.getName()));
            target.sendMessage(messageManager.getMessage("kit.give.received", "kit", kitName, "player", player.getName()));
        }

        return true;
    }

    private void giveKitToPlayer(Player player, Kit kit) {
        org.bukkit.inventory.PlayerInventory inventory = player.getInventory();
        
        // Проверяем свободное место для всех предметов кита
        if (!hasEnoughSpace(inventory, kit)) {
            player.sendMessage(messageManager.getMessage("kit.give.no_space"));
            return;
        }
        
        for (Map.Entry<Integer, org.bukkit.inventory.ItemStack> entry : kit.getItems().entrySet()) {
            int slot = entry.getKey();
            org.bukkit.inventory.ItemStack item = entry.getValue();
            
            if (slot >= 0 && slot < 36) {
                if (inventory.getItem(slot) == null || inventory.getItem(slot).getType() == org.bukkit.Material.AIR) {
                    inventory.setItem(slot, item.clone());
                } else {
                    inventory.addItem(item.clone());
                }
            }
        }
        
        org.bukkit.inventory.ItemStack[] armor = kit.getArmor();
        org.bukkit.inventory.ItemStack[] currentArmor = inventory.getArmorContents();
        org.bukkit.inventory.ItemStack[] newArmor = new org.bukkit.inventory.ItemStack[4];
        for (int i = 0; i < armor.length; i++) {
            if (armor[i] != null) {
                newArmor[i] = armor[i].clone();
            } else {
                newArmor[i] = currentArmor[i];
            }
        }
        inventory.setArmorContents(newArmor);
        
        org.bukkit.inventory.ItemStack offhand = kit.getOffhandItem();
        org.bukkit.inventory.ItemStack currentOffhand = inventory.getItemInOffHand();
        
        if (offhand != null) {
            org.bukkit.inventory.ItemStack offhandClone = offhand.clone();
            inventory.setItemInOffHand(offhandClone);
        } else {
        }
    }
    
    private boolean hasEnoughSpace(org.bukkit.inventory.PlayerInventory inventory, Kit kit) {
        int freeSlots = 0;
        
        for (int i = 0; i < 36; i++) {
            org.bukkit.inventory.ItemStack item = inventory.getItem(i);
            if (item == null || item.getType() == org.bukkit.Material.AIR) {
                freeSlots++;
            }
        }
        int requiredSlots = 0;
        for (Map.Entry<Integer, org.bukkit.inventory.ItemStack> entry : kit.getItems().entrySet()) {
            int slot = entry.getKey();
            org.bukkit.inventory.ItemStack item = entry.getValue();
            
            if (slot >= 0 && slot < 36 && item != null && item.getType() != org.bukkit.Material.AIR) {
                if (inventory.getItem(slot) != null && inventory.getItem(slot).getType() != org.bukkit.Material.AIR) {
                    requiredSlots++;
                }
            }
        }
        
        return freeSlots >= requiredSlots;
    }
}
