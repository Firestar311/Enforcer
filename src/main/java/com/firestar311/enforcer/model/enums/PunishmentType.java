package com.firestar311.enforcer.model.enums;

public enum PunishmentType {
    PERMANENT_BAN("&4", "&4&lPERMANENT BAN", "BAN"), TEMPORARY_BAN("&c", "&c&lTEMPORARY BAN", "TEMP_BAN", "TEMPBAN"), PERMANENT_MUTE("&1", "&1&lPERMANENT MUTE", "MUTE"), TEMPORARY_MUTE("&9", "&9&lTEMPORARY MUTE", "TEMP_MUTE", "TEMPMUTE"), WARN("&e", "&e&lWARN"), KICK("&a", "&a&lKICK"), JAIL("&d", "&d&lJAIL");
    
    private String color;
    private String displayName;
    
    private String[] aliases;
    
    PunishmentType() {
    }
    
    PunishmentType(String color, String displayName) {
        this.color = color;
        this.displayName = displayName;
    }
    
    PunishmentType(String color, String displayName, String... aliaes) {
        this(color, displayName);
        this.aliases = aliaes;
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
    
    public static PunishmentType getType(String name) {
        PunishmentType type = null;
        try {
            type = PunishmentType.valueOf(name);
        } catch (Exception e) {
            for (PunishmentType t : values()) {
                if (t.getAliases() != null) {
                    for (String alias : t.getAliases()) {
                        if (alias.equalsIgnoreCase(name)) return t;
                    }
                }
            }
        }
        return type;
    }
}