package org.cEditor.utils;

import org.bukkit.ChatColor;

public class MessageUtilsSimple {
    
    public static String format(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
