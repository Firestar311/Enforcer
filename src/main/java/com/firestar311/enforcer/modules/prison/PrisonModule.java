package com.firestar311.enforcer.modules.prison;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.modules.base.Module;
import org.bukkit.event.HandlerList;

public class PrisonModule extends Module<PrisonManager> {
    private PlayerPrisonListener playerPrisonListener;
    public PrisonModule(Enforcer plugin, String name, PrisonManager manager, String... commands) {
        super(plugin, name, manager, commands);
        this.playerPrisonListener = new PlayerPrisonListener(plugin);
    }
    
    
    public void setup() {
        if (enabled) {
            plugin.getServer().getPluginManager().registerEvents(playerPrisonListener, plugin);
            manager.loadData();
        }
        PrisonCommand prisonCommand = new PrisonCommand(plugin);
        registerCommands(prisonCommand);
    }
    
    public void desetup() {
        manager.saveData();
        HandlerList.unregisterAll(playerPrisonListener);
        registerCommands(null);
    }
}