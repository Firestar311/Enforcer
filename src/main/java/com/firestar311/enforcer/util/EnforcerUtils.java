package com.firestar311.enforcer.util;

import com.firestar311.enforcer.modules.punishments.PunishmentBuilder;
import com.firestar311.enforcer.modules.punishments.type.PunishmentType;
import com.firestar311.enforcer.modules.punishments.type.abstraction.Punishment;
import com.firestar311.lib.pagination.Paginator;
import com.firestar311.lib.pagination.PaginatorFactory;
import com.firestar311.lib.util.Utils;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

public class EnforcerUtils {
    public static Paginator<Punishment> generatePaginatedPunishmentList(List<Punishment> punishments, String header, String footer) {
        Collections.sort(punishments);
        PaginatorFactory<Punishment> factory = new PaginatorFactory<>();
        factory.setMaxElements(7).setHeader(header).setFooter(footer);
        punishments.forEach(factory::addElement);
        return factory.build();
    }
    
    public static long parseTime(String rawTime) {
        var years = extractRawTime(rawTime, Unit.YEARS);
        var months = extractRawTime(years.getValue(), Unit.MONTHS);
        var weeks = extractRawTime(months.getValue(), Unit.WEEKS);
        var days = extractRawTime(weeks.getValue(), Unit.DAYS);
        var hours = extractRawTime(days.getValue(), Unit.HOURS);
        var minutes = extractRawTime(hours.getValue(), Unit.MINUTES);
        var seconds = extractRawTime(minutes.getValue(), Unit.SECONDS);
        return years.getKey() + months.getKey() + weeks.getKey() + days.getKey() + hours.getKey() + minutes.getKey() + seconds.getKey();
    }
    
    private static Entry<Long, String> extractRawTime(String rawTime, Unit unit) {
        rawTime = rawTime.toLowerCase();
        String[] rawArray;
        for (String alias : unit.getAliases()) {
            alias = alias.toLowerCase();
            if (rawTime.contains(alias)) {
                rawArray = rawTime.split(alias);
                String fh = rawArray[0];
                long rawLength;
                try {
                    rawLength = Integer.parseInt(fh);
                } catch (NumberFormatException e) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = fh.length() - 1; i > 0; i--) {
                        char c = fh.charAt(i);
                        if (Character.isDigit(c)) {
                            sb.insert(0, c);
                        } else {
                            break;
                        }
                    }
                    rawLength = Integer.parseInt(sb.toString());
                }
                rawTime = rawTime.replace(rawLength + alias, "");
                
                return new SimpleEntry<>(unit.convertTime(rawLength), rawTime);
            }
        }
        
        return new SimpleEntry<>(0L, rawTime);
    }
    
    public static String getPunishString(PunishmentBuilder puBuilder) {
        return getPunishString(puBuilder.getType(), puBuilder.getLength());
    }
    
    public static String getPunishString(PunishmentType type, long length) {
        String punishmentString = type.getColor();
        switch (type) {
            case PERMANENT_BAN:
                punishmentString += "Permanent Ban";
                break;
            case TEMPORARY_BAN:
                punishmentString += "Ban for " + Utils.formatTime(length);
                break;
            case PERMANENT_MUTE:
                punishmentString += "Permanent Mute";
                break;
            case TEMPORARY_MUTE:
                punishmentString += "Mute for " + Utils.formatTime(length);
                break;
            case WARN:
                punishmentString += "Warning";
                break;
            case KICK:
                punishmentString += "Kick";
                break;
            case JAIL:
                punishmentString += "Jail";
                break;
        }
        return punishmentString;
    }
}
