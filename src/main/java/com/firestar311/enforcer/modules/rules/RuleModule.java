package com.firestar311.enforcer.modules.rules;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.modules.base.Module;

public class RuleModule extends Module<RuleManager> {
    public RuleModule(Enforcer plugin, String name, RuleManager manager, String... commands) {
        super(plugin, name, manager, commands);
    }
    
    public void setup() {
        if (enabled) {
            manager.loadData();
        }
        RuleCommand ruleCommand = new RuleCommand(plugin);
        registerCommands(ruleCommand);
    }
    
    public void desetup() {
        manager.saveData();
        registerCommands(null);
    }
}