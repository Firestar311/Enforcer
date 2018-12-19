package com.firestar311.enforcer.model.rule;

import com.firestar311.lib.pagination.Paginatable;

import java.util.*;

public class RuleOffense implements Paginatable {
    
    private SortedMap<Integer, RulePunishment> punishments = new TreeMap<>();
    
    private int offenseNumber;
    
    public RuleOffense(int offenseNumber) {
        this.offenseNumber = offenseNumber;
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
}