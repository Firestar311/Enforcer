package com.firestar311.enforcer.util;

import java.util.concurrent.TimeUnit;

public enum Unit {
    SECONDS("seconds", "second", "s") { public long convertTime(long rawLength) {
        return TimeUnit.SECONDS.toMillis(rawLength);
    }},
    
    MINUTES("minutes", "minute", "min", "m") { public long convertTime(long rawLength) {
        return TimeUnit.MINUTES.toMillis(rawLength);
    }},
    
    HOURS("hours", "hour", "h") { public long convertTime(long rawLength) {
        return TimeUnit.HOURS.toMillis(rawLength);
    }},
    
    DAYS("days", "day", "d") { public long convertTime(long rawLength) {
        return TimeUnit.DAYS.toMillis(rawLength);
    }},
    
    WEEKS("weeks", "week", "w") { public long convertTime(long rawLength) {
        return TimeUnit.DAYS.toMillis(rawLength) * 7;
    }},
    
    MONTHS("months", "month", "mo") { public long convertTime(long rawLength) {
        return TimeUnit.DAYS.toMillis(rawLength) * 30;
    }},
    
    YEARS("years", "year", "y") { public long convertTime(long rawLength) {
        return TimeUnit.DAYS.toMillis(rawLength) * 365;
    }};
    
    private String name;
    private String[] aliases;
    
    Unit(String name, String... aliases) {
        this.name = name;
        this.aliases = aliases;
    }
    
    public String getName() {
        return name;
    }
    
    public String[] getAliases() {
        return aliases;
    }
    
    public abstract long convertTime(long rawLength);
    
    public static Unit matchUnit(String unitString) {
        for (Unit unit : values()) {
            if (unit.getName().equalsIgnoreCase(unitString)) {
                return unit;
            }
            
            for (String alias : unit.getAliases()) {
                if (alias.equalsIgnoreCase(unitString)) {
                    return unit;
                }
            }
        }
        
        throw new IllegalArgumentException("Invalid unit type");
    }
}