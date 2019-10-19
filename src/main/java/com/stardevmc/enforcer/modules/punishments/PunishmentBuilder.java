package com.stardevmc.enforcer.modules.punishments;

import com.stardevmc.enforcer.Enforcer;
import com.stardevmc.enforcer.modules.punishments.actor.Actor;
import com.stardevmc.enforcer.modules.punishments.actor.PlayerActor;
import com.stardevmc.enforcer.modules.punishments.target.Target;
import com.stardevmc.enforcer.modules.punishments.type.PunishmentType;
import com.stardevmc.enforcer.modules.punishments.type.abstraction.Punishment;
import com.stardevmc.enforcer.modules.punishments.type.impl.*;
import com.stardevmc.enforcer.modules.punishments.type.interfaces.Expireable;
import com.stardevmc.enforcer.util.evidence.Evidence;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

public class PunishmentBuilder {
    
    protected int id, ruleId = -1, offenseNumber = -1, prisonId;
    protected PunishmentType type;
    protected String server;
    protected Actor remover;
    protected String reason;
    protected long date, removedDate, length;
    protected boolean active, purgatory, offline = false, trainingMode = false;
    protected Visibility visibility, pardonVisibility = Visibility.NORMAL;
    protected Evidence evidence;
    protected Actor actor;
    protected Target target;
    private String removedReason;
    
    public PunishmentBuilder(Target target, PunishmentType type) {
        this.target = target;
        this.type = type;
    }
    
    public PunishmentBuilder(Map<String, Object> serialized) {
        for (Field field : getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                if (serialized.containsKey(field.getName())) {
                    if (field.getType().isEnum()) {
                        Class<?> enumClass = field.getType();
                        Method valueOf = enumClass.getMethod("valueOf", String.class);
                        field.set(this, valueOf.invoke(null, (String) serialized.get(field.getName())));
                    } else if (field.getType().isAssignableFrom(UUID.class)) {
                        field.set(this, UUID.fromString((String) serialized.get(field.getName())));
                    } else {
                        field.set(this, serialized.get(field.getName()));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public PunishmentBuilder(Target target) {
        this.target = target;
    }
    
    public PunishmentBuilder() { }
    
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
    
    public String getRemovedReason() {
        return removedReason;
    }
    
    public PunishmentBuilder setPunisher(UUID actor) {
        this.actor = new PlayerActor(actor);
        return this;
    }
    
    public PunishmentBuilder setPunisher(Actor actor) {
        this.actor = actor;
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
        if (actor == null) {
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
    
    public int getRuleId() {
        return ruleId;
    }
    
    public int getOffenseNumber() {
        return offenseNumber;
    }
    
    public PunishmentType getType() {
        return type;
    }
    
    public String getServer() {
        return server;
    }
    
    public Actor getActor() {
        return actor;
    }
    
    public Target getTarget() {
        return target;
    }
    
    public String getReason() {
        return reason;
    }
    
    public long getDate() {
        return date;
    }
    
    public long getLength() {
        return length;
    }
    
    public boolean isOffline() {
        return offline;
    }
    
    public boolean isTrainingMode() {
        return trainingMode;
    }
    
    public Visibility getVisibility() {
        return visibility;
    }
    
    public int getPrisonId() {
        return prisonId;
    }
    
    public Punishment build() {
        performChecks();
        Punishment punishment = null;
        long expire = date + length;
        switch (type) {
            case PERMANENT_BAN: punishment = new PermanentBan(server, actor, target, reason, date, visibility);
                break;
            case TEMPORARY_BAN: punishment = new TemporaryBan(server, actor, target, reason, date, visibility, expire);
                break;
            case PERMANENT_MUTE: punishment = new PermanentMute(server, actor, target, reason, date, visibility);
                break;
            case TEMPORARY_MUTE: punishment = new TemporaryMute(server, actor, target, reason, date, visibility, expire);
                break;
            case WARN: punishment = new WarnPunishment(server, actor, target, reason, date, visibility);
                break;
            case KICK: punishment = new KickPunishment(server, actor, target, reason, date, visibility);
                break;
            case JAIL: punishment = new JailPunishment(server, actor, target, reason, date, visibility, prisonId);
                break;
            case BLACKLIST: punishment = new BlacklistPunishment(server, actor, target, reason, date, visibility);
        }
        
        if (ruleId != -1) {
            punishment.setRuleId(ruleId);
        }
        
        if (offenseNumber != -1) {
            punishment.setOffenseNumber(offenseNumber);
        }
        
        punishment.setOffline(offline);
        punishment.setTrainingMode(trainingMode);
        punishment.setEvidence(evidence);
        punishment.setRemover(remover);
        punishment.setRemovedDate(removedDate);
        punishment.setPardonVisibility(pardonVisibility);
        punishment.setRemovedReason(removedReason);
        punishment.setActive(active);
        punishment.setId(id);
        punishment.setPurgatory(purgatory);
        return punishment;
    }
    
    public PunishmentBuilder setActive(boolean active) {
        this.active = active;
        return this;
    }
    
    public PunishmentBuilder setPurgatory(boolean purgatory) {
        this.purgatory = purgatory;
        return this;
    }
    
    public PunishmentBuilder setRemovedDate(long removedDate) {
        this.removedDate = removedDate;
        return this;
    }
    
    public PunishmentBuilder setEvidence(Evidence evidence) {
        this.evidence = evidence;
        return this;
    }
    
    public PunishmentBuilder setId(int id) {
        this.id = id;
        return this;
    }
    
    public PunishmentBuilder setRemovedReason(String removedReason) {
        this.removedReason = removedReason;
        return this;
    }
    
    public PunishmentBuilder setRemover(Actor remover) {
        this.remover = remover;
        return this;
    }
    
    public PunishmentBuilder setTarget(Target target) {
        this.target = target;
        return this;
    }
    
    public PunishmentBuilder setPardonVisibility(Visibility pardonVisibility) {
        this.pardonVisibility = pardonVisibility;
        return this;
    }
}