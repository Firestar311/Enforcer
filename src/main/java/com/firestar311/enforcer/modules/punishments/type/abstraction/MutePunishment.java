package com.firestar311.enforcer.modules.punishments.type.abstraction;

import com.firestar311.enforcer.modules.punishments.type.PunishmentType;
import com.firestar311.enforcer.modules.punishments.Visibility;
import com.firestar311.lib.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public abstract class MutePunishment extends Punishment {
    public MutePunishment(PunishmentType type, String server, UUID punisher, UUID target, String reason, long date) {
        super(type, server, punisher, target, reason, date);
    }
    
    public MutePunishment(PunishmentType type, String server, UUID punisher, UUID target, String reason, long date, Visibility visibility) {
        super(type, server, punisher, target, reason, date, visibility);
    }
    
    public MutePunishment(int id, PunishmentType type, String server, UUID punisher, UUID target, String reason, long date, boolean active, boolean purgatory, Visibility visibility) {
        super(id, type, server, punisher, target, reason, date, active, purgatory, visibility);
    }
    
    public MutePunishment(Map<String, Object> serialized) {
        super(serialized);
    }
    
    public void executePunishment() {
        Player player = Bukkit.getPlayer(this.target);
        if (player != null) {
            player.sendMessage(Utils.color("&cYou have been muted by &7" + getPunisherName() + " &cfor &7" + this.reason));
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
        Player player = Bukkit.getPlayer(this.target);
        if (player != null) {
            player.sendMessage(Utils.color("&aYou have been unmuted by &b" + getRemoverName()));
        }
    }
}
