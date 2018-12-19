package com.firestar311.enforcer.model.punishment.type;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.model.Prison;
import com.firestar311.enforcer.model.enums.PunishmentType;
import com.firestar311.enforcer.model.enums.Visibility;
import com.firestar311.enforcer.model.punishment.abstraction.Punishment;
import com.firestar311.lib.items.InventoryStore;
import com.firestar311.lib.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

public class JailPunishment extends Punishment {
    
    private int jailId = -1;
    
    public JailPunishment(Map<String, Object> serialized) {
        super(serialized);
        if (serialized.containsKey("jailId")) {
            this.jailId = (int) serialized.get("jailId");
        }
    }
    
    public JailPunishment(String server, UUID punisher, UUID target, String reason, long date, int jailId) {
        super(PunishmentType.JAIL, server, punisher, target, reason, date);
        this.jailId = jailId;
    }
    
    public JailPunishment(String server, UUID punisher, UUID target, String reason, long date, Visibility visibility, int jailId) {
        super(PunishmentType.JAIL, server, punisher, target, reason, date, visibility);
        this.jailId = jailId;
    }
    
    public JailPunishment(int id, String server, UUID punisher, UUID target, String reason, long date, boolean active, boolean purgatory, Visibility visibility, int jailId) {
        super(id, PunishmentType.JAIL, server, punisher, target, reason, date, active, purgatory, visibility);
        this.jailId = jailId;
    }
    
    public void executePunishment() {
        Player player = Bukkit.getPlayer(target);
        Prison prison = Enforcer.getInstance().getDataManager().getPrison(this.jailId);
        prison.addInhabitant(this.target);
        if (player != null) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Enforcer.getInstance(), () -> player.teleport(prison.getLocation()));
            player.sendMessage(Utils.color("&cYou have been jailed by &7" + getPunisherName() + " &cfor the reason &7" + this.getReason()));
            String inv = InventoryStore.itemsToString(player.getInventory().getContents());
            Enforcer.getInstance().getDataManager().addInvString(player.getUniqueId(), inv);
            player.getInventory().clear();
        } else {
            setOffline(true);
        }
        
        sendPunishMessage();
    }
    
    public void executePardon(UUID remover, long removedDate) {
        setRemover(remover);
        setRemovedDate(removedDate);
        setActive(false);
        sendRemovalMessage();
        
        Prison prison = Enforcer.getInstance().getDataManager().getPrison(this.jailId);
        if (prison != null) {
            prison.removeInhabitant(this.target);
        }
        
        Player target = Bukkit.getPlayer(this.target);
        if (target != null) {
            target.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
            target.sendMessage(Utils.color("&aYou have been unjailed by &b" + getRemoverName()));
            try {
                ItemStack[] items = InventoryStore.stringToItems(Enforcer.getInstance().getDataManager().getJailedInv(target.getUniqueId()));
                target.getInventory().setContents(items);
                target.sendMessage(Utils.color("&7&oYour inventory items have been restored."));
            } catch (Exception e) {
                target.sendMessage(Utils.color("&cThere was a problem restoring your inventory. Please contact the plugin developer"));
            }
        } else {
            Enforcer.getInstance().getDataManager().addUnjailedWhileOffline(this.target);
        }
    }
    
    public int getJailId() {
        return jailId;
    }
    
    public void setJailId(int id) {
        this.getAuditLog().addAuditEntry("Prison ID changed from " + this.jailId + " to " + id);
        this.jailId = id;
    }
}
