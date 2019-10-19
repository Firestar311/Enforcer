package com.stardevmc.enforcer.modules.punishments.target;

import com.stardevmc.enforcer.Enforcer;
import com.firestar311.lib.player.PlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerTarget extends Target {
    private UUID uniqueId;
    
    public PlayerTarget(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }
    
    public UUID getUniqueId() {
        return uniqueId;
    }
    
    public PlayerInfo getPlayerInfo() {
        return Enforcer.getInstance().getPlayerManager().getPlayerInfo(this.uniqueId);
    }
    
    public String getName() {
        return getPlayerInfo().getLastName();
    }
    
    public Player getPlayer() {
        return Bukkit.getPlayer(uniqueId);
    }
    
    public Map<String, Object> serialize() {
        Map<String, Object> serialized = new HashMap<>();
        serialized.put("uuid", this.uniqueId.toString());
        return serialized;
    }
    
    public static PlayerTarget deserialize(Map<String, Object> serialized) {
        UUID uuid = UUID.fromString((String) serialized.get("uuid"));
        return new PlayerTarget(uuid);
    }
}