package org.cEditor.utils;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtilsFixed {
    
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<gradient:#([A-Fa-f0-9]{6}):#([A-Fa-f0-9]{6})>(.*?)</gradient>");
    
    public static String format(String message) {
        if (message == null) return "";
        
        try {
            message = ChatColor.translateAlternateColorCodes('&', message);
            message = processGradients(message);
            message = processHexColors(message);
            return message;
        } catch (Exception e) {
            return ChatColor.translateAlternateColorCodes('&', message);
        }
    }
    private static String processHexColors(String message) {
        try {
            Matcher matcher = HEX_PATTERN.matcher(message);
            StringBuffer buffer = new StringBuffer();
            
            while (matcher.find()) {
                String hex = matcher.group(1);
                try {
                    ChatColor color = ChatColor.valueOf("#" + hex);
                    matcher.appendReplacement(buffer, color.toString());
                } catch (IllegalArgumentException | NoSuchMethodError e) {
                    matcher.appendReplacement(buffer, matcher.group(0));
                }
            }
            
            return matcher.appendTail(buffer).toString();
        } catch (Exception e) {
            return message;
        }
    }
    
    private static String processGradients(String message) {
        try {
            Matcher matcher = GRADIENT_PATTERN.matcher(message);
            StringBuffer buffer = new StringBuffer();
            
            while (matcher.find()) {
                String startHex = matcher.group(1);
                String endHex = matcher.group(2);
                String text = matcher.group(3);
                
                try {
                    String gradient = createGradient(text, startHex, endHex);
                    matcher.appendReplacement(buffer, gradient);
                } catch (IllegalArgumentException e) {
                    matcher.appendReplacement(buffer, matcher.group(0));
                }
            }
            
            return matcher.appendTail(buffer).toString();
        } catch (Exception e) {
            return message;
        }
    }
    
    private static String createGradient(String text, String startHex, String endHex) {
        if (text.isEmpty()) return "";
        
        try {
            int startColor = Integer.parseInt(startHex, 16);
            int endColor = Integer.parseInt(endHex, 16);
            
            StringBuilder gradient = new StringBuilder();
            
            for (int i = 0; i < text.length(); i++) {
                float ratio = text.length() == 1 ? 0.0f : (float) i / (text.length() - 1);
                try {
                    int color = interpolateColor(startColor, endColor, ratio);
                    
                    String hex = String.format("%06X", color);
                    ChatColor chatColor;
                    
                    try {
                        chatColor = ChatColor.valueOf("#" + hex);
                    } catch (IllegalArgumentException | NoSuchMethodError e) {
                        chatColor = ChatColor.WHITE;
                    }
                    
                    gradient.append(chatColor.toString()).append(text.charAt(i));
                } catch (Exception e) {
                    gradient.append(text.charAt(i));
                }
            }
            
            return gradient.toString();
        } catch (NumberFormatException e) {
            return text;
        }
    }
    
    private static int interpolateColor(int start, int end, float ratio) {
        int red = interpolateChannel((start >> 16) & 0xFF, (end >> 16) & 0xFF, ratio);
        int green = interpolateChannel((start >> 8) & 0xFF, (end >> 8) & 0xFF, ratio);
        int blue = interpolateChannel(start & 0xFF, end & 0xFF, ratio);
        
        return (red << 16) | (green << 8) | blue;
    }
    
    private static int interpolateChannel(int start, int end, float ratio) {
        return (int) (start + (end - start) * ratio);
    }
}
