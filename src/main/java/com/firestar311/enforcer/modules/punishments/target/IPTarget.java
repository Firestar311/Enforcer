package com.firestar311.enforcer.modules.punishments.target;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class IPTarget extends Target {
    
    private String ipAddress;
    
    public IPTarget(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public String getName() {
        return getIpAddress();
    }
    
    public Player getPlayer() {
        return null;
    }
    
    public Map<String, Object> serialize() {
        Map<String, Object> serialized = new HashMap<>();
        serialized.put("ipAddress", this.ipAddress);
        return serialized;
    }
    
    public static IPTarget deserialize(Map<String, Object> serialized) {
        return new IPTarget((String) serialized.get("ipAddress"));
    }
}