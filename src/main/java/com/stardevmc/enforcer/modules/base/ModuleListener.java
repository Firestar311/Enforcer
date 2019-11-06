package com.stardevmc.enforcer.modules.base;

import com.stardevmc.enforcer.Enforcer;
import org.bukkit.event.Listener;

public abstract class ModuleListener implements Listener {

    protected static final Enforcer plugin = Enforcer.getInstance();
    
    public ModuleListener() {}
}