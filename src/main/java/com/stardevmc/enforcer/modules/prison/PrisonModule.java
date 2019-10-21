package com.stardevmc.enforcer.modules.prison;

import com.stardevmc.enforcer.Enforcer;
import com.stardevmc.enforcer.modules.base.Module;
import org.bukkit.event.HandlerList;

public class PrisonModule extends Module<PrisonManager> {
    private PlayerPrisonListener playerPrisonListener;
    public PrisonModule(Enforcer plugin, String... commands) {
        super(plugin, "prisons", new PrisonManager(plugin), commands);
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