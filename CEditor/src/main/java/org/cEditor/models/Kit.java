package org.cEditor.models;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Kit implements Serializable {
    private final String name;
    private final Map<Integer, ItemStack> items;
    private final ItemStack[] armor;
    private final ItemStack offhandItem;

    public Kit(String name, PlayerInventory inventory) {
        this.name = name;
        this.items = new HashMap<>();
        
        for (int i = 0; i < 36; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null) {
                this.items.put(i, item.clone());
            }
        }
        
        this.armor = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            ItemStack armorPiece = inventory.getArmorContents()[i];
            if (armorPiece != null) {
                this.armor[i] = armorPiece.clone();
            }
        }
        
        ItemStack offhand = inventory.getItemInOffHand();
        this.offhandItem = offhand != null ? offhand.clone() : null;
    }

    public Kit(String name, Map<Integer, ItemStack> items, ItemStack[] armor, ItemStack offhandItem) {
        this.name = name;
        this.items = new HashMap<>(items);
        this.armor = new ItemStack[4];
        if (armor != null) {
            for (int i = 0; i < 4; i++) {
                this.armor[i] = armor[i] != null ? armor[i].clone() : null;
            }
        }
        this.offhandItem = offhandItem != null ? offhandItem.clone() : null;
    }

    public String getName() {
        return name;
    }

    public Map<Integer, ItemStack> getItems() {
        return new HashMap<>(items);
    }

    public ItemStack[] getArmor() {
        ItemStack[] armorCopy = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            armorCopy[i] = armor[i] != null ? armor[i].clone() : null;
        }
        return armorCopy;
    }

    public ItemStack getOffhandItem() {
        return offhandItem != null ? offhandItem.clone() : null;
    }

    public Kit clone() {
        return new Kit(name, items, armor, offhandItem);
    }
}
