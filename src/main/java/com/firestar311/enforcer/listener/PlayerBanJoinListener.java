package com.firestar311.enforcer.listener;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.model.enums.PunishmentType;
import com.firestar311.enforcer.model.punishment.abstraction.BanPunishment;
import com.firestar311.enforcer.model.punishment.abstraction.Punishment;
import com.firestar311.enforcer.model.punishment.type.TemporaryBan;
import com.firestar311.enforcer.util.Messages;
import com.firestar311.enforcer.util.Perms;
import com.firestar311.lib.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

import java.util.*;

public class PlayerBanJoinListener implements Listener {
    
    private Enforcer plugin;
    
    private Map<UUID, Integer> notifications = new HashMap<>();
    
    public PlayerBanJoinListener(Enforcer plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPreJoin(AsyncPlayerPreLoginEvent e) {
        UUID player = e.getUniqueId();
        if (plugin.getDataManager().isBanned(e.getUniqueId())) {
            List<Punishment> bans = new ArrayList<>(plugin.getDataManager().getActiveBans(e.getUniqueId()));
            BanPunishment worsePunishment = null;
            for (Punishment punishment : bans) {
                if (worsePunishment == null) {
                    worsePunishment = (BanPunishment) punishment;
                    if (punishment.getType().equals(PunishmentType.PERMANENT_BAN)) break;
                } else {
                    if (punishment.getType().equals(PunishmentType.PERMANENT_BAN) && worsePunishment.getType().equals(PunishmentType.TEMPORARY_BAN)) {
                        worsePunishment = (BanPunishment) punishment;
                        break;
                    } else {
                        TemporaryBan worseBan = ((TemporaryBan) worsePunishment);
                        TemporaryBan currentBan = ((TemporaryBan) punishment);
                        if (currentBan.getExpireDate() > worseBan.getExpireDate()) {
                            worsePunishment = (BanPunishment) punishment;
                        }
                    }
                }
            }
            e.disallow(Result.KICK_BANNED, Utils.color(Messages.formatPunishKick(worsePunishment)));
    
            if (this.notifications.containsKey(player)) {
                this.notifications.put(player, this.notifications.get(player) + 1);
            } else {
                this.notifications.put(player, 1);
            }
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission(Perms.NOTIFY_JOIN)) {
                    if (this.notifications.containsKey(player)) {
                        if (this.notifications.get(player) < 5) {
                            p.sendMessage(Utils.color("&c" + e.getName() + " tried to join but is banned."));
                        } else if (this.notifications.get(player) == 5) {
                            p.sendMessage(Utils.color("&c" + e.getName() + " continues to join, silencing notifications"));
                        }
                    }
                }
            }
        }
    }
}