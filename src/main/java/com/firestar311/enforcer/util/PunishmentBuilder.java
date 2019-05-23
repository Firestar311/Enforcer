package com.firestar311.enforcer.util;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.model.enums.PunishmentType;
import com.firestar311.enforcer.model.enums.Visibility;
import com.firestar311.enforcer.model.punishment.abstraction.Punishment;
import com.firestar311.enforcer.model.punishment.interfaces.Expireable;
import com.firestar311.enforcer.model.punishment.type.*;
import org.apache.commons.lang.StringUtils;

import java.util.UUID;

public class PunishmentBuilder {
    
    private int ruleId = -1, offenseNumber = -1;
    private PunishmentType type;
    private String server;
    private UUID punisher, target;
    private String reason;
    private long date = 0, length = 0;
    private boolean offline = false, trainingMode = false;
    private Visibility visibility;
    private int prisonId = -1;
    
    public PunishmentBuilder(UUID target, PunishmentType type) {
        this.target = target;
        this.type = type;
    }
    
    public PunishmentBuilder setRuleId(int ruleId) {
        this.ruleId = ruleId;
        return this;
    }
    
    public PunishmentBuilder setOffenseNumber(int offenseNumber) {
        this.offenseNumber = offenseNumber;
        return this;
    }
    
    public PunishmentBuilder setType(PunishmentType type) {
        this.type = type;
        return this;
    }
    
    public PunishmentBuilder setServer(String server) {
        this.server = server;
        return this;
    }
    
    public PunishmentBuilder setPunisher(UUID punisher) {
        this.punisher = punisher;
        return this;
    }
    
    public PunishmentBuilder setReason(String reason) {
        this.reason = reason;
        return this;
    }
    
    public PunishmentBuilder setDate(long date) {
        this.date = date;
        return this;
    }
    
    public PunishmentBuilder setLength(long length) {
        this.length = length;
        return this;
    }
    
    public PunishmentBuilder setOffline(boolean offline) {
        this.offline = offline;
        return this;
    }
    
    public PunishmentBuilder setTrainingMode(boolean trainingMode) {
        this.trainingMode = trainingMode;
        return this;
    }
    
    public PunishmentBuilder setVisibility(Visibility visibility) {
        this.visibility = visibility;
        return this;
    }
    
    public PunishmentBuilder setPrisonId(int prisonId) {
        this.prisonId = prisonId;
        return this;
    }
    
    public void performChecks() {
        if (punisher == null) {
            throw new IllegalArgumentException("Punisher must not be null");
        }
    
        if  (target == null) {
            throw new IllegalArgumentException("Target must not be null");
        }
    
        if (type == null) {
            throw new IllegalArgumentException("Type must not be null");
        }
    
        if (StringUtils.isEmpty(reason)) {
            throw new IllegalArgumentException("Reason must not be null");
        }
    
        if (date == 0) {
            throw new IllegalArgumentException("Date must not be 0");
        }
    
        if (visibility == null) {
            this.visibility = Visibility.NORMAL;
        }
    
        if (StringUtils.isEmpty(server)) {
            this.server = Enforcer.getInstance().getSettingsManager().getServerName();
        }
        
        if (type.equals(PunishmentType.JAIL)) {
            if (prisonId == -1) {
                throw new IllegalArgumentException("Punishment Type is of Jail Type and does not have a prison id set.");
            }
        }
        
        if (type.getPunishmentClass().isAssignableFrom(Expireable.class)) {
            if (this.length == 0) {
                throw new IllegalArgumentException("Punishment type can expire but no length for that time is defined");
            }
        }
    }
    
    public Punishment build() {
        performChecks();
        Punishment punishment = null;
        long expire = date + length;
        switch (type) {
            case PERMANENT_BAN: punishment = new PermanentBan(server, punisher, target, reason, date, visibility);
                break;
            case TEMPORARY_BAN: punishment = new TemporaryBan(server, punisher, target, reason, date, visibility, expire);
                break;
            case PERMANENT_MUTE: punishment = new PermanentMute(server, punisher, target, reason, date, visibility);
                break;
            case TEMPORARY_MUTE: punishment = new TemporaryMute(server, punisher, target, reason, date, visibility, expire);
                break;
            case WARN: punishment = new WarnPunishment(server, punisher, target, reason, date, visibility);
                break;
            case KICK: punishment = new KickPunishment(server, punisher, target, reason, date, visibility);
                break;
            case JAIL: punishment = new JailPunishment(server, punisher, target, reason, date, visibility, prisonId);
                break;
        }
        
        if (ruleId != -1) {
            punishment.setRuleId(ruleId);
        }
        
        if (offenseNumber != -1) {
            punishment.setOffenseNumber(offenseNumber);
        }
        
        punishment.setOffline(offline);
        punishment.setTrainingMode(trainingMode);
        return punishment;
    }
}