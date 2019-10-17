package com.firestar311.enforcer.modules.punishments.type.impl;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.modules.prison.Prison;
import com.firestar311.enforcer.modules.punishments.Visibility;
import com.firestar311.enforcer.modules.punishments.actor.Actor;
import com.firestar311.enforcer.modules.punishments.target.Target;
import com.firestar311.enforcer.modules.punishments.type.PunishmentType;
import com.firestar311.enforcer.modules.punishments.type.abstraction.Punishment;
import com.firestar311.lib.items.InventoryStore;
import com.firestar311.lib.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class JailPunishment extends Punishment {
    
    private String jailedInventory;
    private int prisonId = -1;
    private boolean unjailedWhileOffline, notifiedOfOfflineJail, notifiedOfOfflineUnjail;
    
    public JailPunishment(String server, Actor punisher, Target target, String reason, long date, int prisonId) {
        super(PunishmentType.JAIL, server, punisher, target, reason, date);
        this.prisonId = prisonId;
    }
    
    public JailPunishment(String server, Actor punisher, Target target, String reason, long date, Visibility visibility, int prisonId) {
        super(PunishmentType.JAIL, server, punisher, target, reason, date, visibility);
        this.prisonId = prisonId;
    }
    
    public JailPunishment(int id, String server, Actor punisher, Target target, String reason, long date, boolean active, boolean purgatory, Visibility visibility, int prisonId) {
        super(id, PunishmentType.JAIL, server, punisher, target, reason, date, active, purgatory, visibility);
        this.prisonId = prisonId;
    }
    
    public void executePunishment() {
        Player player = target.getPlayer();
        Prison prison = Enforcer.getInstance().getPrisonModule().getManager().getPrison(this.prisonId);
        prison.addInhabitant(player.getUniqueId());
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
    
    public void reversePunishment(Actor remover, long removedDate) {
        setRemover(remover);
        setRemovedDate(removedDate);
        setActive(false);
        sendRemovalMessage();
        
        Player target = this.target.getPlayer();
        Prison prison = Enforcer.getInstance().getPrisonModule().getManager().getPrison(this.prisonId);
        if (prison != null) {
            prison.removeInhabitant(target.getUniqueId());
        }
        
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
    
    public boolean wasUnjailedWhileOffline() {
        return unjailedWhileOffline;
    }
    
    public boolean wasNotifiedOfOfflineJail() {
        return notifiedOfOfflineJail;
    }
    
    public int getPrisonId() {
        return prisonId;
    }
    
    public void setPrisonId(int id) {
        this.prisonId = id;
    }
    
    public String getJailedInventory() {
        return jailedInventory;
    }
    
    public void setJailedInventory(String jailedInventory) {
        this.jailedInventory = jailedInventory;
    }
    
    public void setNotifiedOfOfflineUnjail(boolean notifiedOfOfflineUnjail) {
        this.notifiedOfOfflineUnjail = notifiedOfOfflineUnjail;
    }
    
    public void setNotifiedOfOfflineJail(boolean notifiedOfOfflineJail) {
        this.notifiedOfOfflineJail = notifiedOfOfflineJail;
    }
    
    public Map<String, Object> serialize() {
        Map<String, Object> serialized = super.serializeBase();
        serialized.put("jailedInventory", this.jailedInventory);
        serialized.put("prisonId", this.prisonId + "");
        serialized.put("unjailedWhileOffline", unjailedWhileOffline);
        serialized.put("notifiedOfOfflineJail", notifiedOfOfflineJail);
        serialized.put("notifiedOfOfflineUnjail", notifiedOfOfflineUnjail);
        return serialized;
    }
    
    public static JailPunishment deserialize(Map<String, Object> serialized) {
        String jailedInventory = (String) serialized.get("jailedInventory");
        int prisonId = Integer.parseInt((String) serialized.get("prisonId"));
        boolean unjailedWhileOffline = (boolean) serialized.get("unjailedWhileOffline");
        boolean notifiedOfOfflineJail = (boolean) serialized.get("notifiedOfOfflineJail");
        boolean notifiedOfOfflineUnjail = (boolean) serialized.get("notifiedOfOfflineUnjail");
        JailPunishment jailPunishment = (JailPunishment) Punishment.deserializeBase(serialized).build();
        jailPunishment.notifiedOfOfflineJail = notifiedOfOfflineJail;
        jailPunishment.notifiedOfOfflineUnjail = notifiedOfOfflineUnjail;
        jailPunishment.jailedInventory = jailedInventory;
        jailPunishment.unjailedWhileOffline = unjailedWhileOffline;
        jailPunishment.prisonId = prisonId;
        return jailPunishment;
    }
}
