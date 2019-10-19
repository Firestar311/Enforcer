package com.stardevmc.enforcer.modules.prison;

import com.firestar311.lib.pagination.IElement;
import com.firestar311.lib.region.Cuboid;
import com.firestar311.lib.util.Utils;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.*;

public class Prison extends Cuboid implements IElement, ConfigurationSerializable {
    
    private int id;
    private Location location;
    private int maxPlayers;
    private Set<UUID> inhabitants = new HashSet<>();
    private String name;
    
    public Prison(int id, Location location, int maxPlayers, Location minLocation, Location maxLocation) {
        super(minLocation, maxLocation);
        this.id = id;
        this.location = location;
        this.maxPlayers = maxPlayers;
    }
    
    public Prison(int id, Location location, int maxPlayers, Set<UUID> inhabitants, Location minLocation, Location maxLocation) {
        super(minLocation, maxLocation);
        this.id = id;
        this.location = location;
        this.maxPlayers = maxPlayers;
        this.inhabitants = inhabitants;
    }
    
    public Prison(int id, Location location, int maxPlayers) {
        super(location, location);
        this.id = id;
        this.location = location;
        this.maxPlayers = maxPlayers;
    }
    
    public Map<String, Object> serialize() {
        Map<String, Object> serialized = new HashMap<>();
        serialized.put("id", id + "");
        serialized.put("spawn", this.location);
        serialized.put("maxPlayers", this.maxPlayers + "");
        serialized.put("name", this.name);
        serialized.put("inhabitants", Utils.convertUUIDListToStringList(this.inhabitants));
        serialized.put("min", this.getMinimum());
        serialized.put("max", this.getMaximum());
        return serialized;
    }
    
    public Prison(Location pos1, Location pos2, int id, Location location, int maxPlayers, Set<UUID> inhabitants, String name) {
        super(pos1, pos2);
        this.id = id;
        this.location = location;
        this.maxPlayers = maxPlayers;
        this.inhabitants = inhabitants;
        this.name = name;
    }
    
    public static Prison deserialize(Map<String, Object> serialized) {
        int id = Integer.parseInt((String) serialized.get("id"));
        Location location = (Location) serialized.get("spawn");
        int maxPlayers = Integer.parseInt((String) serialized.get("maxPlayers"));
        Set<UUID> inhabitants = new HashSet<>(Utils.getUUIDListFromStringList((List<String>) serialized.get("inhabitants")));
        String name = (String) serialized.get("name");
        Location min = (Location) serialized.get("min");
        Location max = (Location) serialized.get("max");
        return new Prison(min, max, id, location, maxPlayers, inhabitants, name);
    }
    
    public Location getLocation() {
        return location;
    }
    
    public void setLocation(Location location) {
        this.location = location;
    }
    
    public int getMaxPlayers() {
        return maxPlayers;
    }
    
    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }
    
    public int getId() {
        return id;
    }
    
    public Set<UUID> getInhabitants() {
        return new HashSet<>(inhabitants);
    }
    
    public void addInhabitant(UUID uuid) {
        this.inhabitants.add(uuid);
    }
    
    public void removeInhabitant(UUID uuid) {
        this.inhabitants.remove(uuid);
    }
    
    public boolean isFull() {
        return this.inhabitants.size() >= maxPlayers;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public boolean isInhabitant(UUID uuid) {
        return this.inhabitants.contains(uuid);
    }
    
    public void setInhabitants(Set<UUID> inhabitants) {
        this.inhabitants = inhabitants;
    }
    
    public String getName() {
        return name == null ? id + "" : name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDisplayName() {
        if (this.name == null || this.name.equalsIgnoreCase("") || this.name.equalsIgnoreCase("null")) {
            return this.id + "";
        }
        return this.name;
    }
    
    public String formatLine(String... args) {
        if (this.name != null && !this.name.equals("")) {
            return "&bPrison &d" + this.id + " &bhas the name &e" + this.name + " &band has &a" + this.inhabitants.size() + " &bout of &a" + this.maxPlayers + " &bplayers";
        }
        return "&bPrison &d" + this.id + " &bhas no name set and has &a" + this.inhabitants.size() + " &bout of &a" + this.maxPlayers + " &bplayers";
    }
}