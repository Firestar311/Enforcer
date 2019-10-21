package com.stardevmc.enforcer.modules.watchlist;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.*;

public class WatchlistNote implements ConfigurationSerializable {
    private UUID creator;
    private String text;
    
    public WatchlistNote(UUID creator, String text) {
        this.creator = creator;
        this.text = text;
    }
    
    public Map<String, Object> serialize() {
        Map<String, Object> serialized = new HashMap<>();
        serialized.put("creator", this.creator.toString());
        serialized.put("text", this.text);
        return serialized;
    }
    
    public static WatchlistNote deserialize(Map<String, Object> serialized) {
        UUID creator = UUID.fromString((String) serialized.get("creator"));
        String text = (String) serialized.get("text");
        return new WatchlistNote(creator, text);
    }
    
    public UUID getCreator() {
        return creator;
    }
    
    public String getText() {
        return text;
    }
}