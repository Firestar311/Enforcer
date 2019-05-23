package com.firestar311.enforcer.model.enums;

import com.firestar311.enforcer.model.punishment.abstraction.Punishment;
import com.firestar311.enforcer.model.punishment.type.*;

public enum PunishmentType {
    PERMANENT_BAN("&4", "&4&lPERMANENT BAN", PermanentBan.class, "BAN"), TEMPORARY_BAN("&c", "&c&lTEMPORARY BAN", TemporaryBan.class, "TEMP_BAN", "TEMPBAN"),
    PERMANENT_MUTE("&1", "&1&lPERMANENT MUTE", PermanentMute.class, "MUTE"), TEMPORARY_MUTE("&9", "&9&lTEMPORARY MUTE", TemporaryMute.class, "TEMP_MUTE", "TEMPMUTE"),
    WARN("&e", "&e&lWARN", WarnPunishment.class), KICK("&a", "&a&lKICK", KickPunishment.class), JAIL("&d", "&d&lJAIL", JailPunishment.class);
    
    private String color;
    private String displayName;
    private Class<? extends Punishment> clazz;
    
    private String[] aliases;
    
    PunishmentType() {
    }
    
    PunishmentType(String color, String displayName, Class<? extends Punishment> clazz) {
        this.color = color;
        this.displayName = displayName;
        this.clazz = clazz;
    }
    
    PunishmentType(String color, String displayName, Class<? extends Punishment> clazz, String... aliases) {
        this(color, displayName, clazz);
        this.aliases = aliases;
    }
    
    public String getColor() {
        return color;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String[] getAliases() {
        return aliases;
    }
    
    public Class<? extends Punishment> getPunishmentClass() {
        return clazz;
    }
    
    public static PunishmentType getType(String name) {
        try {
            return PunishmentType.valueOf(name);
        } catch (Exception e) {
            for (PunishmentType t : values()) {
                if (t.getAliases() != null) {
                    for (String alias : t.getAliases()) {
                        if (alias.equalsIgnoreCase(name)) return t;
                    }
                }
            }
        }
        return null;
    }
}