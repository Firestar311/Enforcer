package com.stardevmc.enforcer.modules.rules.rule;

import com.firestar311.lib.pagination.IElement;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.Map.Entry;

public class RuleOffense implements IElement, ConfigurationSerializable {
    
    private SortedMap<Integer, RulePunishment> punishments = new TreeMap<>();
    
    private int offenseNumber;
    private long length;
    
    private Rule parent;
    
    public RuleOffense(Rule parent, int offenseNumber) {
        this.offenseNumber = offenseNumber;
        this.parent = parent;
    }
    
    public RuleOffense() {}
    
    public void addPunishment(RulePunishment punishment) {
        if (punishment.getId() == -1) {
            int id = this.punishments.size();
            punishment.setId(id);
        }
        this.punishments.put(punishment.getId(), punishment);
    }
    
    public void addPunishment(int id, RulePunishment punishment) {
        if (punishment.getId() == -1) {
            punishment.setId(id);
        }
        this.punishments.put(id, punishment);
    }
    
    public void setLength(long length) {
        this.length = length;
    }
    
    public SortedMap<Integer, RulePunishment> getPunishments() {
        return new TreeMap<>(punishments);
    }
    
    public int getOffenseNumber() {
        return offenseNumber;
    }
    
    public void setOffenseNumber(int offenseNumber) {
        this.offenseNumber = offenseNumber;
    }
    
    public void removePunishment(int punishment) {
        this.punishments.remove(punishment);
    }
    
    public String formatLine(String... args) {
        return "&dOffense: &e" + offenseNumber + " &7- &dAction(s): &e" + this.punishments.size();
    }
    
    public void clearPunishments() {
        this.punishments.clear();
    }
    
    public boolean hasPunishment(int id) {
        return this.punishments.containsKey(id);
    }
    
    public String getPermission() {
        return parent.getPermission() + ".offenses." + this.offenseNumber;
    }
    
    public boolean hasPermission(Player player) {
        return player.hasPermission("enforcer.rules.*") || player.hasPermission(parent.getPermission() + ".offenses.*") || player.hasPermission(getPermission());
    }
    
    public long getLength() {
        return length;
    }
    
    public void setParent(Rule rule) {
        this.parent = rule;
    }
    
    public Map<String, Object> serialize() {
        Map<String, Object> serialized = new HashMap<>();
        serialized.put("offenseNumber", this.offenseNumber + "");
        serialized.put("length", this.length + "");
        for (Entry<Integer, RulePunishment> entry : this.punishments.entrySet()) {
            serialized.put("punishment" + entry.getKey(), entry.getValue());
        }
        return serialized;
    }
    
    public static RuleOffense deserialize(Map<String, Object> serialized) {
        int offenseNumber = Integer.parseInt((String) serialized.get("offenseNumber"));
        int length = Integer.parseInt((String) serialized.get("length"));
        SortedMap<Integer, RulePunishment> punishments = new TreeMap<>();
        serialized.forEach((s, o) -> {
            if (s.startsWith("punishment")) {
                RulePunishment rulePunishment = (RulePunishment) o;
                punishments.put(rulePunishment.getId(), rulePunishment);
            }
        });
        
        RuleOffense ruleOffense = new RuleOffense();
        ruleOffense.offenseNumber = offenseNumber;
        ruleOffense.length = length;
        ruleOffense.punishments = punishments;
        return ruleOffense;
    }
}