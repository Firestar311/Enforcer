package com.firestar311.enforcer.modules.punishments.type.impl;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.modules.prison.Prison;
import com.firestar311.enforcer.modules.punishments.type.PunishmentType;
import com.firestar311.enforcer.modules.punishments.Visibility;
import com.firestar311.enforcer.modules.punishments.type.abstraction.Punishment;
import com.firestar311.lib.items.InventoryStore;
import com.firestar311.lib.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

public class JailPunishment extends Punishment {
    
    private int prisonId = -1;
    private String jailedInventory;
    private boolean unjailedWhileOffline, notifiedOfOfflineJail, notifiedOfOfflineUnjail;
    
    public JailPunishment(Map<String, Object> serialized) {
        super(serialized);
        if (serialized.containsKey("prisonId")) {
            this.prisonId = (int) serialized.get("prisonId");
        }
        
        if (serialized.containsKey("jailedInventory")) {
            this.jailedInventory = (String) serialized.get("jailedInventory");
        }
        
        if (serialized.containsKey("unjailedWhileOffline")) {
            this.unjailedWhileOffline = (boolean) serialized.get("unjailedWhileOffline");
        }
        
        if (serialized.containsKey("notifiedOfOfflineJail")) {
            this.notifiedOfOfflineJail = (boolean) serialized.get("notifiedOfOfflineJail");
        }
        
        if (serialized.containsKey("notifiedOfOfflineUnjail")) {
            this.notifiedOfOfflineUnjail = (boolean) serialized.get("notifiedOfOfflineJail");
        }
    }
    
    public JailPunishment(String server, UUID punisher, UUID target, String reason, long date, int prisonId) {
        super(PunishmentType.JAIL, server, punisher, target, reason, date);
        this.prisonId = prisonId;
    }
    
    public JailPunishment(String server, UUID punisher, UUID target, String reason, long date, Visibility visibility, int prisonId) {
        super(PunishmentType.JAIL, server, punisher, target, reason, date, visibility);
        this.prisonId = prisonId;
    }
    
    public JailPunishment(int id, String server, UUID punisher, UUID target, String reason, long date, boolean active, boolean purgatory, Visibility visibility, int prisonId) {
        super(id, PunishmentType.JAIL, server, punisher, target, reason, date, active, purgatory, visibility);
        this.prisonId = prisonId;
    }
    
    public void executePunishment() {
        Player player = Bukkit.getPlayer(target);
        Prison prison = Enforcer.getInstance().getPrisonManager().getPrison(this.prisonId);
        prison.addInhabitant(this.target);
        if (player != null) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Enforcer.getInstance(), () -> player.teleport(prison.getLocation()));
            player.sendMessage(Utils.color("&cYou have been jailed by &7" + getPunisherName() + " &cfor the reason &7" + this.getReason()));
            this.jailedInventory = InventoryStore.itemsToString(player.getInventory().getContents());
            player.getInventory().clear();
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
        
        Prison prison = Enforcer.getInstance().getPrisonManager().getPrison(this.prisonId);
        if (prison != null) {
            prison.removeInhabitant(this.target);
        }
        
        Player target = Bukkit.getPlayer(this.target);
        if (target != null) {
            target.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
            target.sendMessage(Utils.color("&aYou have been unjailed by &b" + getRemoverName()));
            try {
                ItemStack[] items = InventoryStore.stringToItems(this.jailedInventory);
                target.getInventory().setContents(items);
                target.sendMessage(Utils.color("&7&oYour inventory items have been restored."));
            } catch (Exception e) {
                target.sendMessage(Utils.color("&cThere was a problem restoring your inventory. Please contact the plugin developer"));
            }
        } else {
            this.unjailedWhileOffline = true;
            this.notifiedOfOfflineJail = false;
        }
    }
    
    public boolean wasNotifiedOfOfflineUnjail() {
        return notifiedOfOfflineUnjail;
    }
    
    public void setNotifiedOfOfflineUnjail(boolean notifiedOfOfflineUnjail) {
        this.notifiedOfOfflineUnjail = notifiedOfOfflineUnjail;
    }
    
    public int getPrisonId() {
        return prisonId;
    }
    
    public void setPrisonId(int id) {
        this.getAuditLog().addAuditEntry("Prison ID changed from " + this.prisonId + " to " + id);
        this.prisonId = id;
    }
    
    public void setJailedInventory(String jailedInventory) {
        this.jailedInventory = jailedInventory;
    }
    
    public String getJailedInventory() {
        return jailedInventory;
    }
    
    public boolean wasUnjailedWhileOffline() {
        return unjailedWhileOffline;
    }
    
    public boolean wasNotifiedOfOfflineJail() {
        return notifiedOfOfflineJail;
    }
    
    public void setNotifiedOfOfflineJail(boolean notifiedOfOfflineJail) {
        this.notifiedOfOfflineJail = notifiedOfOfflineJail;
    }
}
