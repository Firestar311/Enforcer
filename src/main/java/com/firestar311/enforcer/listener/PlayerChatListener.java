package com.firestar311.enforcer.listener;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.model.punishment.abstraction.Punishment;
import com.firestar311.enforcer.model.punishment.type.WarnPunishment;
import com.firestar311.enforcer.util.Perms;
import com.firestar311.lib.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;

public class PlayerChatListener implements Listener {
    
    private Enforcer plugin;
    
    private Map<UUID, Integer> notifications = new HashMap<>();
    
    public PlayerChatListener(Enforcer plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        
        if (plugin.getDataManager().isJailed(player.getUniqueId())) {
            e.setCancelled(true);
            player.sendMessage(Utils.color("&cYou cannnot speak while jailed."));
            return;
        }
        if (plugin.getDataManager().isMuted(player.getUniqueId())) {
            e.setCancelled(true);
            player.sendMessage(Utils.color("&cYou cannot speak while muted."));
            if (this.notifications.containsKey(player.getUniqueId())) {
                this.notifications.put(player.getUniqueId(), this.notifications.get(player.getUniqueId()) + 1);
            } else {
                this.notifications.put(player.getUniqueId(), 1);
            }
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission(Perms.NOTIFY_PUNISHMENTS)) {
                    if (this.notifications.containsKey(player.getUniqueId())) {
                        if (this.notifications.get(player.getUniqueId()) < 5) {
                            p.sendMessage(Utils.color("&c" + player.getName() + " tried to speak but is muted."));
                        } else if (this.notifications.get(player.getUniqueId()) == 5) {
                            p.sendMessage(Utils.color("&c" + player.getName() + " continues to speak, silencing notifications"));
                        }
                    }
                }
            }
            return;
        }
    
        e.getRecipients().removeIf(recipient -> plugin.getDataManager().isJailed(recipient.getUniqueId()));
        
        for (Punishment punishment : plugin.getDataManager().getWarnings(player.getUniqueId())) {
            if (!plugin.getDataManager().isTrainingMode()) {
                WarnPunishment warning = (WarnPunishment) punishment;
                if (!warning.isAcknowledged()) {
                    e.setCancelled(true);
                    plugin.getLogger().info(player.getName() + " is warned");
                    warning.createPrompt();
                    break;
                }
            }
        }
    }
}