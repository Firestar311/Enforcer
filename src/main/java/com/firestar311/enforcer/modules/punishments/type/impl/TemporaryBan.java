package com.firestar311.enforcer.modules.punishments.type.impl;

import com.firestar311.enforcer.modules.punishments.type.PunishmentType;
import com.firestar311.enforcer.modules.punishments.Visibility;
import com.firestar311.enforcer.modules.punishments.type.abstraction.BanPunishment;
import com.firestar311.enforcer.modules.punishments.type.interfaces.Expireable;
import com.firestar311.lib.util.Utils;

import java.util.Map;
import java.util.UUID;

public class TemporaryBan extends BanPunishment implements Expireable {
    
    private long expire;
    
    public TemporaryBan(Map<String, Object> serialized) {
        super(serialized);
        if (serialized.containsKey("expire")) {
            this.expire = Long.valueOf(serialized.get("expire") + "");
        }
    }
    
    public TemporaryBan(String server, UUID punisher, UUID target, String reason, long date, long expire) {
        super(PunishmentType.TEMPORARY_BAN, server, punisher, target, reason, date);
        this.expire = expire;
    }
    
    public TemporaryBan(String server, UUID punisher, UUID target, String reason, long date, Visibility visibility, long expire) {
        super(PunishmentType.TEMPORARY_BAN, server, punisher, target, reason, date, visibility);
        this.expire = expire;
    }
    
    public TemporaryBan(int id, String server, UUID punisher, UUID target, String reason, long date, boolean active, boolean purgatory, Visibility visibility, long expire) {
        super(id, PunishmentType.TEMPORARY_BAN, server, punisher, target, reason, date, active, purgatory, visibility);
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
        setActive(false);
    }
    
    public void setExpireDate(long expireDate) {
        this.getAuditLog().addAuditEntry("Expire dated changed from " + this.expire + " to " + expireDate);
        this.expire = expireDate;
    }
}
