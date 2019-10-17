package com.firestar311.enforcer.modules.punishments.type.abstraction;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.modules.punishments.Visibility;
import com.firestar311.enforcer.modules.punishments.actor.Actor;
import com.firestar311.enforcer.modules.punishments.target.Target;
import com.firestar311.enforcer.modules.punishments.type.PunishmentType;
import com.firestar311.enforcer.util.Messages;
import com.firestar311.lib.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;

public abstract class BanPunishment extends Punishment {
    
    public BanPunishment(PunishmentType type, String server, Actor punisher, Target target, String reason, long date) {
        super(type, server, punisher, target, reason, date);
    }
    
    public BanPunishment(PunishmentType type, String server, Actor punisher, Target target, String reason, long date, Visibility visibility) {
        super(type, server, punisher, target, reason, date, visibility);
    }
    
    public BanPunishment(int id, PunishmentType type, String server, Actor punisher, Target target, String reason, long date, boolean active, boolean purgatory, Visibility visibility) {
        super(id, type, server, punisher, target, reason, date, active, purgatory, visibility);
    }
    
    public void executePunishment() {
        Player player = target.getPlayer();
        if (player != null) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Enforcer.getInstance(), () -> player.kickPlayer(Utils.color(Messages.formatPunishKick(this))));
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
    }
    
    public Map<String, Object> serialize() {
        return super.serializeBase();
    }
    
    public static BanPunishment deserialize(Map<String, Object> serialized) {
        return (BanPunishment) Punishment.deserializeBase(serialized).build();
    }
}
