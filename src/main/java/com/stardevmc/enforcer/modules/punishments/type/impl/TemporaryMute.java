package com.stardevmc.enforcer.modules.punishments.type.impl;

import com.stardevmc.enforcer.modules.punishments.Visibility;
import com.stardevmc.enforcer.modules.punishments.actor.Actor;
import com.stardevmc.enforcer.modules.punishments.target.Target;
import com.stardevmc.enforcer.modules.punishments.type.PunishmentType;
import com.stardevmc.enforcer.modules.punishments.type.abstraction.MutePunishment;
import com.stardevmc.enforcer.modules.punishments.type.interfaces.Expireable;
import com.firestar311.lib.util.Utils;

import java.util.Map;

public class TemporaryMute extends MutePunishment implements Expireable {
    
    private long expire;
    
    public TemporaryMute(String server, Actor punisher, Target target, String reason, long date, long expire) {
        super(PunishmentType.TEMPORARY_MUTE, server, punisher, target, reason, date);
        this.expire = expire;
    }
    
    public TemporaryMute(String server, Actor punisher, Target target, String reason, long date, Visibility visibility, long expire) {
        super(PunishmentType.TEMPORARY_MUTE, server, punisher, target, reason, date, visibility);
        this.expire = expire;
    }
    
    public TemporaryMute(int id, String server, Actor punisher, Target target, String reason, long date, boolean active, boolean purgatory, Visibility visibility, long expire) {
        super(id, PunishmentType.TEMPORARY_MUTE, server, punisher, target, reason, date, active, purgatory, visibility);
        this.expire = expire;
    }
    
    public long getExpireDate() {
        return expire;
    }
    
    public boolean isExpired() {
        return System.currentTimeMillis() >= expire;
    }
    
    public String formatExpireTime() {
        return Utils.formatTime(expire - System.currentTimeMillis());
    }
    
    public void onExpire() {
        active = false;
    }
    
    public void setExpireDate(long expireDate) {
        this.expire = expireDate;
    }
    
    public Map<String, Object> serialize() {
        Map<String, Object> serialized = super.serialize();
        serialized.put("expire", this.expire + "");
        return serialized;
    }
    
    public static TemporaryMute deserialize(Map<String, Object> serialized) {
        long expire = Long.parseLong((String) serialized.get("expire"));
        TemporaryMute tempMute = (TemporaryMute) MutePunishment.deserialize(serialized);
        tempMute.expire = expire;
        return tempMute;
    }
}
