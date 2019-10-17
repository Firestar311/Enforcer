package com.firestar311.enforcer.modules.punishments.type.impl;

import com.firestar311.enforcer.modules.punishments.Visibility;
import com.firestar311.enforcer.modules.punishments.actor.Actor;
import com.firestar311.enforcer.modules.punishments.target.Target;
import com.firestar311.enforcer.modules.punishments.type.PunishmentType;
import com.firestar311.enforcer.modules.punishments.type.abstraction.MutePunishment;

public class PermanentMute extends MutePunishment {
    
    public PermanentMute(String server, Actor punisher, Target target, String reason, long date) {
        super(PunishmentType.PERMANENT_MUTE, server, punisher, target, reason, date);
    }
    
    public PermanentMute(String server, Actor punisher, Target target, String reason, long date, Visibility visibility) {
        super(PunishmentType.PERMANENT_MUTE, server, punisher, target, reason, date, visibility);
    }
    
    public PermanentMute(int id, String server, Actor punisher, Target target, String reason, long date, boolean active, boolean purgatory, Visibility visibility) {
        super(id, PunishmentType.PERMANENT_MUTE, server, punisher, target, reason, date, active, purgatory, visibility);
    }
}
