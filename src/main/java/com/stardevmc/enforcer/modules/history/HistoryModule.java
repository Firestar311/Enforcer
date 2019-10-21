package com.stardevmc.enforcer.modules.history;

import com.stardevmc.enforcer.Enforcer;
import com.stardevmc.enforcer.modules.base.Module;

public class HistoryModule extends Module<HistoryManager> {
    public HistoryModule(Enforcer plugin, String... commands) {
        super(plugin, "history", new HistoryManager(plugin), commands);
    }
    
    public void setup() {
        HistoryCommands historyCommands = new HistoryCommands(plugin);
        registerCommands(historyCommands);
    }
    
    public void desetup() {
        registerCommands(null);
    }
}