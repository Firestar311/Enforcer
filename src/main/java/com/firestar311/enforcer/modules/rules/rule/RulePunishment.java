package com.firestar311.enforcer.modules.rules.rule;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.modules.punishments.type.PunishmentType;
import com.firestar311.enforcer.util.Unit;
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
    
    public RulePunishment(PunishmentType type, int cLength, String cUnits) {
        this(type, Enforcer.convertTime(cUnits, cLength), cLength, cUnits);
    }
    
    public RulePunishment(PunishmentType type, int cLength, Unit cUnits) {
        this(type, cUnits.convertTime(cLength), cLength, cUnits.name().toLowerCase());
    }
    
    public String formatLine(String... args) {
        if (length == -1) { return "&dAction: " + id + " " + type.getDisplayName(); }
        return "&dAction: " + id + " " + type.getDisplayName() + " &b" + Utils.formatTime(length);
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
}