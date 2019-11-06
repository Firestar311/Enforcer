package com.stardevmc.enforcer.modules.punishments;

import com.stardevmc.enforcer.Enforcer;
import com.stardevmc.enforcer.modules.base.Module;
import com.stardevmc.enforcer.modules.punishments.cmds.PunishmentCommands;
import com.stardevmc.enforcer.modules.punishments.listeners.PlayerChatListener;
import com.stardevmc.enforcer.modules.punishments.listeners.PlayerJoinListener;

public class PunishmentModule extends Module<PunishmentManager> {
    public PunishmentModule(Enforcer plugin, String... commands) {
        super(plugin, "punishments", new PunishmentManager(plugin), commands);
        this.addListenerClass(PlayerChatListener.class, PlayerJoinListener.class);
    }
    
    public void setup() {
        if (enabled) {
            manager.loadData();
        }
        PunishmentCommands puCommands = new PunishmentCommands(plugin);
        registerCommands(puCommands);
        registerListeners();
    }
    
    public void desetup() {
        manager.saveData();
        registerCommands(null);
        unregisterListeners();
    }
}