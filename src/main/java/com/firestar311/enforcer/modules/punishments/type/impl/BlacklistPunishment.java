package com.firestar311.enforcer.modules.punishments.type.impl;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.modules.punishments.Visibility;
import com.firestar311.enforcer.modules.punishments.actor.Actor;
import com.firestar311.enforcer.modules.punishments.target.*;
import com.firestar311.enforcer.modules.punishments.type.PunishmentType;
import com.firestar311.enforcer.modules.punishments.type.abstraction.Punishment;
import com.firestar311.lib.player.PlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class BlacklistPunishment extends Punishment {
    
    public BlacklistPunishment(String server, Actor punisher, Target target, String reason, long date) {
        super(PunishmentType.BLACKLIST, server, punisher, target, reason, date);
    }
    
    public BlacklistPunishment(String server, Actor punisher, Target target, String reason, long date, Visibility visibility) {
        super(PunishmentType.BLACKLIST, server, punisher, target, reason, date, visibility);
    }
    
    public BlacklistPunishment(int id, String server, Actor punisher, Target target, String reason, long date, boolean active, boolean purgatory, Visibility visibility) {
        super(id, PunishmentType.BLACKLIST, server, punisher, target, reason, date, active, purgatory, visibility);
    }
    
    public void executePunishment() {
        List<PlayerInfo> affectedPlayers = new ArrayList<>();
        for (PlayerInfo info : Enforcer.getInstance().getPlayerManager().getPlayers().values()) {
            playerIPLoop:
            for (String ip : info.getIpAddresses()) {
                if (target instanceof IPTarget) {
                    IPTarget ipTarget = (IPTarget) this.target;
                    if (ip.equals(ipTarget.getIpAddress())) {
                        affectedPlayers.add(info);
                    }
                } else if (target instanceof IPListTarget) {
                    IPListTarget ipListTarget = (IPListTarget) this.target;
                    for (String ipt : ipListTarget.getIpAddresses()) {
                        if (ip.equals(ipt)) {
                            affectedPlayers.add(info);
                            continue playerIPLoop;
                        }
                    }
                }
            }
        }
        
        for (PlayerInfo affectedPlayer : affectedPlayers) {
            Player player = Bukkit.getPlayer(affectedPlayer.getUuid());
            if (player != null) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(Enforcer.getInstance(), () -> player.kickPlayer("Blacklisted"));
            }
        }
        
        sendPunishMessage();
    }
    
    public void reversePunishment(Actor remover, long removedDate) {
        setActive(false);
        setRemover(remover);
        setRemovedDate(removedDate);
        sendRemovalMessage();
    }
    
    public Map<String, Object> serialize() {
        return super.serializeBase();
    }
    
    public static BlacklistPunishment deserialize(Map<String, Object> serialized) {
        return ((BlacklistPunishment) Punishment.deserializeBase(serialized).build());
    }
}