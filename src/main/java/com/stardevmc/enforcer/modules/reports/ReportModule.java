package com.stardevmc.enforcer.modules.reports;

import com.stardevmc.enforcer.Enforcer;
import com.stardevmc.enforcer.modules.base.Module;

public class ReportModule extends Module<ReportManager> {
    public ReportModule(Enforcer plugin, String... commands) {
        super(plugin, "reports", new ReportManager(plugin), commands);
    }
    
    public void setup() {
        if (enabled) {
            manager.loadData();
        }
        ReportCommands reportCommands = new ReportCommands(plugin);
        registerCommands(reportCommands);
    }
    
    public void desetup() {
        manager.saveData();
        registerCommands(null);
    }
}