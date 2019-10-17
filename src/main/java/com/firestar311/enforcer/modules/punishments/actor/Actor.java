package com.firestar311.enforcer.modules.punishments.actor;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

public abstract class Actor implements ConfigurationSerializable {
    
    public abstract String getName();
    
    public abstract Player getPlayer();
}