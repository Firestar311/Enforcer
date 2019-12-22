package com.stardevmc.enforcer.modules.punishments.actor;

import com.stardevmc.enforcer.Enforcer;
import com.firestar311.lib.player.User;
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
    
    public User getPlayerInfo() {
        return Enforcer.getInstance().getPlayerManager().getUser(this.uniqueId);
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