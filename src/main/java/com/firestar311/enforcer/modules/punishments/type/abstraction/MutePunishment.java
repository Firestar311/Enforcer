package com.firestar311.enforcer.modules.punishments.type.abstraction;

import com.firestar311.enforcer.modules.punishments.Visibility;
import com.firestar311.enforcer.modules.punishments.actor.Actor;
import com.firestar311.enforcer.modules.punishments.target.Target;
import com.firestar311.enforcer.modules.punishments.type.PunishmentType;
import com.firestar311.lib.util.Utils;
import org.bukkit.entity.Player;

import java.util.Map;

public abstract class MutePunishment extends Punishment {
    public MutePunishment(PunishmentType type, String server, Actor punisher, Target target, String reason, long date) {
        super(type, server, punisher, target, reason, date);
    }
    
    public MutePunishment(PunishmentType type, String server, Actor punisher, Target target, String reason, long date, Visibility visibility) {
        super(type, server, punisher, target, reason, date, visibility);
    }
    
    public MutePunishment(int id, PunishmentType type, String server, Actor punisher, Target target, String reason, long date, boolean active, boolean purgatory, Visibility visibility) {
        super(id, type, server, punisher, target, reason, date, active, purgatory, visibility);
    }
    
    public void executePunishment() {
        Player player = this.target.getPlayer();
        if (player != null) {
            player.sendMessage(Utils.color("&cYou have been muted by &7" + getPunisherName() + " &cfor &7" + this.reason));
        } else {
            setOffline(true);
        }
        sendPunishMessage();
    }
    
    public void reversePunishment(Actor remover, long removedDate) {
        setRemover(remover);
        setRemovedDate(removedDate);
        setActive(false);
        sendRemovalMessage();
        Player player = this.target.getPlayer();
        if (player != null) {
            player.sendMessage(Utils.color("&aYou have been unmuted by &b" + getRemoverName()));
        }
    }
    
    public Map<String, Object> serialize() {
        return super.serializeBase();
    }
    
    public static MutePunishment deserialize(Map<String, Object> serialized) {
        return (MutePunishment) Punishment.deserializeBase(serialized).build();
    }
}
