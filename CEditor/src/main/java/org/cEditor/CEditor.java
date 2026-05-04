package org.cEditor;

import org.bukkit.plugin.java.JavaPlugin;
import org.cEditor.commands.KitCommand;
import org.cEditor.managers.KitManager;
import org.cEditor.managers.MessageManager;

public final class CEditor extends JavaPlugin {

    private KitManager kitManager;
    private MessageManager messageManager;

    @Override
    public void onEnable() {
        try {
            messageManager = new MessageManager(this);
            kitManager = new KitManager(this);
            getCommand("kit").setExecutor(new KitCommand(this, kitManager, messageManager));
            getLogger().info("Плагин вкл!");
        } catch (Exception e) {
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    public KitManager getKitManager() {
        return kitManager;
    }
    public MessageManager getMessageManager() {
        return messageManager;
    }
}
