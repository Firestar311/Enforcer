package com.stardevmc.enforcer.modules.rules;

import com.stardevmc.enforcer.Enforcer;
import com.stardevmc.enforcer.modules.base.Module;

public class RuleModule extends Module<RuleManager> {
    public RuleModule(Enforcer plugin, String... commands) {
        super(plugin, "rules", new RuleManager(plugin), commands);
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