package com.firestar311.enforcer.model.punishment.type;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.model.enums.PunishmentType;
import com.firestar311.enforcer.model.enums.Visibility;
import com.firestar311.enforcer.model.punishment.abstraction.Punishment;
import com.firestar311.enforcer.util.Messages;
import com.firestar311.lib.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class KickPunishment extends Punishment {
    
    public KickPunishment(Map<String, Object> serialized) {
        super(serialized);
    }
    
    public KickPunishment(String server, UUID punisher, UUID target, String reason, long date) {
        super(PunishmentType.KICK, server, punisher, target, reason, date);
    }
    
    public KickPunishment(String server, UUID punisher, UUID target, String reason, long date, Visibility visibility) {
        super(PunishmentType.KICK, server, punisher, target, reason, date, visibility);
    }
    
    public KickPunishment(int id, String server, UUID punisher, UUID target, String reason, long date, boolean active, boolean purgatory, Visibility visibility) {
        super(id, PunishmentType.KICK, server, punisher, target, reason, date, active, purgatory, visibility);
    }
    
    public void executePunishment() {
        Player player = Bukkit.getPlayer(target);
        if (player != null) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Enforcer.getInstance(), () -> player.kickPlayer(Utils.color(Messages.formatPunishKick(this))));
        }
    
        sendPunishMessage();
    }
    
    public void executePardon(UUID remover, long removedDate) {
    
    }
}
