package com.stardevmc.enforcer.modules.punishments.type.impl;

import com.stardevmc.enforcer.Enforcer;
import com.stardevmc.enforcer.modules.punishments.Visibility;
import com.stardevmc.enforcer.modules.punishments.actor.Actor;
import com.stardevmc.enforcer.modules.punishments.target.Target;
import com.stardevmc.enforcer.modules.punishments.type.PunishmentType;
import com.stardevmc.enforcer.modules.punishments.type.abstraction.Punishment;
import com.stardevmc.enforcer.util.Messages;
import com.firestar311.lib.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;

public class KickPunishment extends Punishment {
    
    public KickPunishment(String server, Actor punisher, Target target, String reason, long date) {
        super(PunishmentType.KICK, server, punisher, target, reason, date);
    }
    
    public KickPunishment(String server, Actor punisher, Target target, String reason, long date, Visibility visibility) {
        super(PunishmentType.KICK, server, punisher, target, reason, date, visibility);
    }
    
    public KickPunishment(int id, String server, Actor punisher, Target target, String reason, long date, boolean active, boolean purgatory, Visibility visibility) {
        super(id, PunishmentType.KICK, server, punisher, target, reason, date, active, purgatory, visibility);
    }
    
    public void executePunishment() {
        Player player = target.getPlayer();
        if (player != null) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Enforcer.getInstance(), () -> player.kickPlayer(Utils.color(Messages.formatPunishKick(this))));
        }
    
        sendPunishMessage();
    }
    
    public void reversePunishment(Actor remover, long removedDate) {}
    
    public Map<String, Object> serialize() {
        return super.serializeBase();
    }
    
    public static KickPunishment deserialize(Map<String, Object> serialized) {
        return (KickPunishment) Punishment.deserializeBase(serialized).build();
    }
}
