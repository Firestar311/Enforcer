package com.firestar311.enforcer.modules.punishments;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.modules.base.Module;
import com.firestar311.enforcer.modules.punishments.cmds.PunishmentCommands;
import com.firestar311.enforcer.modules.punishments.listeners.PlayerChatListener;
import com.firestar311.enforcer.modules.punishments.listeners.PlayerJoinListener;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;

public class PunishmentModule extends Module<PunishmentManager> {
    private PlayerChatListener playerChatListener;
    private PlayerJoinListener playerJoinListener;
    
    public PunishmentModule(Enforcer plugin, String name, PunishmentManager manager, String... commands) {
        super(plugin, name, manager, commands);
        this.playerChatListener = new PlayerChatListener(plugin);
        this.playerJoinListener = new PlayerJoinListener(plugin);
    }
    
    public void setup() {
        if (enabled) {
            PluginManager pm = plugin.getServer().getPluginManager();
            pm.registerEvents(playerChatListener, plugin);
            pm.registerEvents(playerJoinListener, plugin);
            
            manager.loadData();
        }
        PunishmentCommands puCommands = new PunishmentCommands(plugin);
        registerCommands(puCommands);
    }
    
    public void desetup() {
        HandlerList.unregisterAll(playerChatListener);
        HandlerList.unregisterAll(playerJoinListener);
        manager.saveData();
        registerCommands(null);
    }
}