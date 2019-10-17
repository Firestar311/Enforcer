package com.firestar311.enforcer.modules.punishments.actor;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.lib.player.PlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerActor extends Actor {

    private UUID uniqueId;
    
    public PlayerActor(UUID uniqueId) {
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
        return Bukkit.getPlayer(this.uniqueId);
    }
    
    public Map<String, Object> serialize() {
        Map<String, Object> serialized = new HashMap<>();
        serialized.put("uuid", this.uniqueId.toString());
        return serialized;
    }
    
    public static PlayerActor deserialize(Map<String, Object> serialized) {
        UUID uuid = UUID.fromString((String) serialized.get("uuid"));
        return new PlayerActor(uuid);
    }
}