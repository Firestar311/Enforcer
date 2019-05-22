package com.firestar311.enforcer.listener;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.model.Prison;
import com.firestar311.enforcer.model.punishment.abstraction.Punishment;
import com.firestar311.enforcer.model.punishment.type.JailPunishment;
import com.firestar311.enforcer.util.Perms;
import com.firestar311.lib.items.InventoryStore;
import com.firestar311.lib.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerPrisonListener implements Listener {
    
    private Enforcer plugin;
    
    public PlayerPrisonListener(Enforcer plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (plugin.getPunishmentManager().isJailed(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(Utils.color("&cYou cannot break blocks while jailed."));
        }
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (plugin.getPunishmentManager().isJailed(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(Utils.color("&cYou cannot place blocks while jailed."));
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (plugin.getPunishmentManager().isJailed(e.getPlayer().getUniqueId())) {
            Prison prison = plugin.getPrisonManager().getPrison(player.getUniqueId());
            if (prison != null) {
                if (!prison.contains(player)) {
                    player.teleport(prison.getLocation());
                    if (player.getInventory().getSize() > 0) {
                        player.getInventory().clear();
                    }
                    player.sendMessage(Utils
                            .color("&cYou were outside of the prison bounds, teleporting you to the spawn location."));
                }
            }
        } else {
            for (Punishment punishment : plugin.getPunishmentManager().getJailPunishments(player.getUniqueId())) {
                JailPunishment jailPunishment = ((JailPunishment) punishment);
                
                new BukkitRunnable() {
                    public void run() {
                        player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                        if (jailPunishment.wasUnjailedWhileOffline() && !jailPunishment.wasNotifiedOfOfflineUnjail()) {
                            player.sendMessage(Utils.color("&aYou have been unjailed while you were offline"));
                            try {
                                ItemStack[] items = InventoryStore.stringToItems(jailPunishment.getJailedInventory());
                                player.getInventory().setContents(items);
                                player.sendMessage(Utils.color("&7&oYour inventory items have been restored."));
                                jailPunishment.setNotifiedOfOfflineUnjail(true);
                            } catch (Exception e) {
                                player.sendMessage(Utils
                                        .color("&cThere was a problem restoring your inventory. Please contact the plugin developer"));
                            }
                        }
                    }
                }.runTaskLater(plugin, 5L);
            }
        }
        
        if (plugin.getPrisonManager().getPrisons().isEmpty()) {
            if (player.hasPermission(Perms.ENFORCER_ADMIN)) {
                player.sendMessage(Utils
                        .color("&c&lThere are currently no prisons set and the jail punishment type is enabled. Jails will not work."));
            }
        }
    }
    
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (plugin.getPunishmentManager().isJailed(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            p.sendMessage(Utils.color("&cYou cannot use commands while jailed."));
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            Player p = ((Player) e.getDamager());
            if (plugin.getPunishmentManager().isJailed(p.getUniqueId())) {
                e.setCancelled(true);
                p.sendMessage(Utils.color("&cYou cannot damage entites while in jail."));
            }
        }
    }
    
    @EventHandler
    public void foodChangeEvent(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = ((Player) e.getEntity());
            if (plugin.getPunishmentManager().isJailed(p.getUniqueId())) {
                p.setFoodLevel(20);
                p.setSaturation(20L);
            }
        }
    }
}