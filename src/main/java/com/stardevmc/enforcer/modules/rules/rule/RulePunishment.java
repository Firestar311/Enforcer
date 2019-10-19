package com.stardevmc.enforcer.modules.rules.rule;

import com.stardevmc.enforcer.modules.punishments.type.PunishmentType;
import com.firestar311.lib.pagination.IElement;
import com.firestar311.lib.util.Unit;
import com.firestar311.lib.util.Utils;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public class RulePunishment implements IElement, ConfigurationSerializable {
    
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
        this(type, Unit.matchUnit(cUnits).convertTime(cLength), cLength, cUnits);
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
        this.length = Unit.matchUnit(cUnits).convertTime(cLength);
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
    
    public Map<String, Object> serialize() {
        Map<String, Object> serialized = new HashMap<>();
        serialized.put("type", this.type.name());
        serialized.put("length", this.length + "");
        serialized.put("cLength", this.cLength + "");
        serialized.put("cUnits", this.cUnits);
        serialized.put("id", this.id + "");
        return serialized;
    }
    
    public static RulePunishment deserialize(Map<String, Object> serialized) {
        PunishmentType type = PunishmentType.valueOf((String) serialized.get("type"));
        long length = Long.parseLong((String) serialized.get("length"));
        int cLength = Integer.parseInt((String) serialized.get("cLength"));
        String cUnits = (String) serialized.get("cUnits");
        int id = Integer.parseInt((String) serialized.get("id"));
        RulePunishment punishment = new RulePunishment(type, length, cLength, cUnits);
        punishment.id = id;
        return punishment;
    }
}