package com.stardevmc.enforcer.modules.punishments.target;

import org.bukkit.entity.Player;

import java.util.*;

public class IPListTarget extends Target {
    
    private List<String> ipAddresses;
    
    public IPListTarget(List<String> ipAddresses) {
        this.ipAddresses = ipAddresses;
    }
    
    public List<String> getIpAddresses() {
        return ipAddresses;
    }
    
    public String getName() {
        return ipAddresses.toString();
    }
    
    public Player getPlayer() {
        return null;
    }
    
    public Map<String, Object> serialize() {
        Map<String, Object> serialized = new HashMap<>();
        serialized.put("ipAddresses", this.ipAddresses);
        return serialized;
    }
    
    public static IPListTarget deserialize(Map<String, Object> serialized) {
        return new IPListTarget((List<String>) serialized.get("ipAddresses")); //FIXME Might need to handle saving a little differently
    }
}