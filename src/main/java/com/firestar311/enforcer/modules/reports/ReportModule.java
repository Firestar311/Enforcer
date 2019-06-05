package com.firestar311.enforcer.modules.reports;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.modules.base.Module;

public class ReportModule extends Module<ReportManager> {
    public ReportModule(Enforcer plugin, String name, ReportManager manager, String... commands) {
        super(plugin, name, manager, commands);
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