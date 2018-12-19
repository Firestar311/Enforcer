package com.firestar311.enforcer.model.rule;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.model.enums.PunishmentType;
import com.firestar311.lib.pagination.Paginatable;
import com.firestar311.lib.util.Utils;

public class RulePunishment implements Paginatable {
    
    private PunishmentType type;
    private long length;
    private int cLength;
    private String cUnits;
    private int id = -1;
    
    public RulePunishment(PunishmentType type, long length, int cLength, String cUnits) {
        this.type = type;
        this.length = length;
        this.cLength = cLength;
        this.cUnits = cUnits;
    }
    
    public RulePunishment(PunishmentType type, int cLenth, String cUnits) {
        this.type = type;
        this.cLength = cLenth;
        this.cUnits = cUnits;
        this.length = Enforcer.convertTime(cUnits, cLength);
    }
    
    public PunishmentType getType() {
        return type;
    }
    
    public long getLength() {
        this.length = Enforcer.convertTime(cUnits, cLength);
        return length;
    }
    
    public int getcLength() {
        return cLength;
    }
    
    public String getcUnits() {
        return cUnits;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String formatLine(String... args) {
        if (length == -1) return "&dAction: " + id + " " + type.getDisplayName();
        else return "&dAction: " + id + " " + type.getDisplayName() + " &b" + Utils.formatTime(length);
    }
}