package com.stardevmc.enforcer.modules.base;

import com.stardevmc.enforcer.Enforcer;
import com.firestar311.lib.config.ConfigManager;

public abstract class Manager {
    
    protected ConfigManager configManager;
    protected Enforcer plugin;
    
    public Manager(Enforcer plugin, String fileName) {
        this(plugin, fileName, true);
    }
    
    public Manager(Enforcer plugin, String fileName, boolean setup) {
        this.configManager = new ConfigManager(plugin, fileName);
        if (setup) { this.configManager.setup(); }
        this.plugin = plugin;
    }
    
    public abstract void saveData();
    
    public abstract void loadData();
}