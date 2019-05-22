package com.firestar311.enforcer.util;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.model.Prison;
import com.firestar311.enforcer.model.punishment.abstraction.Punishment;
import com.firestar311.enforcer.model.punishment.type.*;
import com.firestar311.enforcer.model.rule.RulePunishment;
import com.firestar311.lib.pagination.Paginator;
import com.firestar311.lib.pagination.PaginatorFactory;

import java.util.*;

public class EnforcerUtils {
    public static Punishment getPunishmentFromRule(Enforcer plugin, UUID target, String server, long currentTime, UUID punisher, String reason, RulePunishment rulePunishment) {
        long expire = currentTime + rulePunishment.getLength();
        switch (rulePunishment.getType()) {
            case PERMANENT_BAN:
                return new PermanentBan(server, punisher, target, reason, currentTime);
            case TEMPORARY_BAN:
                return new TemporaryBan(server, punisher, target, reason, currentTime, expire);
            case PERMANENT_MUTE:
                return new PermanentMute(server, punisher, target, reason, currentTime);
            case TEMPORARY_MUTE:
                return new TemporaryMute(server, punisher, target, reason, currentTime, expire);
            case WARN:
                return new WarnPunishment(server, punisher, target, reason, currentTime);
            case KICK:
                return new KickPunishment(server, punisher, target, reason, currentTime);
            case JAIL:
                Prison prison = plugin.getPrisonManager().findPrison();
                return new JailPunishment(server, punisher, target, reason, currentTime, prison.getId());
        }
        return null;
    }
    
    public static Paginator<Punishment> generatePaginatedPunishmentList(List<Punishment> punishments, String header, String footer) {
        Collections.sort(punishments);
        PaginatorFactory<Punishment> factory = new PaginatorFactory<>();
        factory.setMaxElements(7).setHeader(header).setFooter(footer);
        punishments.forEach(factory::addElement);
        return factory.build();
    }
}
