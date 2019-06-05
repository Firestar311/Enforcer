package com.firestar311.enforcer.modules.pardon;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.modules.base.Module;

public class PardonModule extends Module<PardonManager> {
    public PardonModule(Enforcer plugin, String name, PardonManager manager, String... commands) {
        super(plugin, name, manager, commands);
    }
    
    public void setup() {
        PardonCommands pardonCommands = new PardonCommands(plugin);
        registerCommands(pardonCommands);
    }
    
    public void desetup() {
        registerCommands(null);
    }
}