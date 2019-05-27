package com.firestar311.enforcer.util;

import com.firestar311.enforcer.modules.punishments.PunishmentBuilder;
import com.firestar311.enforcer.modules.punishments.type.PunishmentType;
import com.firestar311.enforcer.modules.punishments.type.abstraction.Punishment;
import com.firestar311.lib.pagination.Paginator;
import com.firestar311.lib.pagination.PaginatorFactory;
import com.firestar311.lib.util.Utils;

import java.util.Collections;
import java.util.List;

public class EnforcerUtils {
    public static Paginator<Punishment> generatePaginatedPunishmentList(List<Punishment> punishments, String header, String footer) {
        Collections.sort(punishments);
        PaginatorFactory<Punishment> factory = new PaginatorFactory<>();
        factory.setMaxElements(7).setHeader(header).setFooter(footer);
        punishments.forEach(factory::addElement);
        return factory.build();
    }
    
    public static String getPunishString(PunishmentBuilder puBuilder) {
        return getPunishString(puBuilder.getType(), puBuilder.getLength());
    }
    
    public static String getPunishString(PunishmentType type, long length) {
        String punishmentString = type.getColor();
        switch (type) {
            case PERMANENT_BAN: punishmentString += "Permanent Ban";
                break;
            case TEMPORARY_BAN: punishmentString += "Ban for " + Utils.formatTime(length);
                break;
            case PERMANENT_MUTE: punishmentString += "Permanent Mute";
                break;
            case TEMPORARY_MUTE: punishmentString += "Mute for " + Utils.formatTime(length);
                break;
            case WARN: punishmentString += "Warning";
                break;
            case KICK: punishmentString += "Kick";
                break;
            case JAIL: punishmentString += "Jail";
                break;
        }
        return punishmentString;
    }
}
