package com.stardevmc.enforcer.modules.pardon;

import com.stardevmc.enforcer.Enforcer;
import com.stardevmc.enforcer.modules.base.Module;

public class PardonModule extends Module<PardonManager> {
    public PardonModule(Enforcer plugin, String... commands) {
        super(plugin, "pardon", new PardonManager(plugin), commands);
    }
    
    public void setup() {
        PardonCommands pardonCommands = new PardonCommands(plugin);
        registerCommands(pardonCommands);
    }
    
    public void desetup() {
        registerCommands(null);
    }
}