package com.firestar311.enforcer.modules.punishments.type.abstraction;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.modules.punishments.type.PunishmentType;
import com.firestar311.enforcer.modules.punishments.Visibility;
import com.firestar311.enforcer.util.Messages;
import com.firestar311.lib.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public abstract class BanPunishment extends Punishment {
    
    public BanPunishment(Map<String, Object> serialized) {
        super(serialized);
    }
    
    public BanPunishment(PunishmentType type, String server, UUID punisher, UUID target, String reason, long date) {
        super(type, server, punisher, target, reason, date);
    }
    
    public BanPunishment(PunishmentType type, String server, UUID punisher, UUID target, String reason, long date, Visibility visibility) {
        super(type, server, punisher, target, reason, date, visibility);
    }
    
    public BanPunishment(int id, PunishmentType type, String server, UUID punisher, UUID target, String reason, long date, boolean active, boolean purgatory, Visibility visibility) {
        super(id, type, server, punisher, target, reason, date, active, purgatory, visibility);
    }
    
    public void executePunishment() {
        Player player = Bukkit.getPlayer(target);
        if (player != null) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Enforcer.getInstance(), () -> player.kickPlayer(Utils.color(Messages.formatPunishKick(this))));
        } else {
            setOffline(true);
        }
        
        sendPunishMessage();
    }
    
    public void reversePunishment(UUID remover, long removedDate) {
        setRemover(remover);
        setRemovedDate(removedDate);
        setActive(false);
        sendRemovalMessage();
    }
}
