package com.stardevmc.enforcer.modules.punishments.target;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

public abstract class Target implements ConfigurationSerializable {
    
    public abstract String getName();
    public abstract Player getPlayer();
}