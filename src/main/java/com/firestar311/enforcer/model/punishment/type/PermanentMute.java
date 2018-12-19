package com.firestar311.enforcer.model.punishment.type;

import com.firestar311.enforcer.model.enums.PunishmentType;
import com.firestar311.enforcer.model.enums.Visibility;
import com.firestar311.enforcer.model.punishment.abstraction.MutePunishment;

import java.util.Map;
import java.util.UUID;

public class PermanentMute extends MutePunishment {
    
    public PermanentMute(Map<String, Object> serialized) {
        super(serialized);
    }
    public PermanentMute(String server, UUID punisher, UUID target, String reason, long date) {
        super(PunishmentType.PERMANENT_MUTE, server, punisher, target, reason, date);
    }
    
    public PermanentMute(String server, UUID punisher, UUID target, String reason, long date, Visibility visibility) {
        super(PunishmentType.PERMANENT_MUTE, server, punisher, target, reason, date, visibility);
    }
    
    public PermanentMute(int id, String server, UUID punisher, UUID target, String reason, long date, boolean active, boolean purgatory, Visibility visibility) {
        super(id, PunishmentType.PERMANENT_MUTE, server, punisher, target, reason, date, active, purgatory, visibility);
    }
}
