package com.firestar311.enforcer.model;

import com.firestar311.lib.pagination.Paginatable;
import com.firestar311.lib.region.Cuboid;
import com.firestar311.lib.util.Utils;
import org.bukkit.Location;

import java.util.*;

public class Prison extends Cuboid implements Paginatable {
    
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
    
    public String serialize() {
        return "id=" + id + ",location=" + Utils.convertLocationToString(location) + ",maxPlayers=" + maxPlayers + ",name=" + name + ",minLoc=" + Utils.convertLocationToString(this.getMinimum()) + ",maxLoc=" + Utils.convertLocationToString(this.getMaximum());
    }
    
    public static Prison deserialize(String serialized) {
        int id = 0, maxPlayers = 0;
        Location location = null, minLocation = null, maxLocation = null;
        String name = "";
        
        for (String string : serialized.split(",")) {
            String[] field = string.split("=");
            if (field[0].equalsIgnoreCase("id")) {
                id = Integer.parseInt(field[1]);
            } else if (field[0].equalsIgnoreCase("maxPlayers")) {
                maxPlayers = Integer.parseInt(field[1]);
            } else if (field[0].equalsIgnoreCase("location")) {
                location = Utils.getLocationFromString(field[1]);
            } else if (field[0].equalsIgnoreCase("name")) {
                name = field[1];
                if (name.equalsIgnoreCase("null")) {
                    name = null;
                }
            } else if (field[0].equalsIgnoreCase("minLoc")) {
                minLocation = Utils.getLocationFromString(field[1]);
            } else if (field[0].equalsIgnoreCase("maxLoc")) {
                maxLocation = Utils.getLocationFromString(field[1]);
            }
        }
        Prison prison;
        if (maxLocation != null && minLocation != null) {
            prison = new Prison(id, location, maxPlayers, minLocation, maxLocation);
        } else {
            prison = new Prison(id, location, maxPlayers);
        }
        if (name != null) {
            prison.setName(name);
        }
        return prison;
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
        return name;
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