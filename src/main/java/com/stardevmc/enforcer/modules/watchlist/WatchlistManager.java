package com.stardevmc.enforcer.modules.watchlist;

import com.stardevmc.enforcer.Enforcer;
import com.stardevmc.enforcer.modules.base.Manager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.Map.Entry;

public class WatchlistManager extends Manager {
    
    private Set<WatchlistEntry> entries = new HashSet<>();
    private Map<UUID, UUID> primaryFocus = new HashMap<>();
    
    public WatchlistManager(Enforcer plugin) {
        super(plugin, "watchlist");
    }
    
    public void saveData() {
        FileConfiguration config = this.configManager.getConfig();
        config.set("entries", null);
        config.set("primaryFocus", null);
        
        for (WatchlistEntry entry : this.entries) {
            config.set("entries." + entry.getTarget().toString(), entry);
        }
        
        for (Entry<UUID, UUID> entry : this.primaryFocus.entrySet()) {
            config.set("primaryFocus." + entry.getKey().toString(), entry.getValue().toString());
        }
        this.configManager.saveConfig();
    }
    
    public void loadData() {
        FileConfiguration config = this.configManager.getConfig();
        ConfigurationSection entriesSection = config.getConfigurationSection("entries");
        if (entriesSection != null) {
        for (String e : entriesSection.getKeys(false)) {
            WatchlistEntry entry = (WatchlistEntry) entriesSection.get(e);
            entries.add(entry);
        }
    }
        
        ConfigurationSection primarySection = config.getConfigurationSection("primaryFocus");
        if (primarySection != null) {
            for (String pf : primarySection.getKeys(false)) {
                UUID staff = UUID.fromString(pf);
                UUID focus = UUID.fromString(primarySection.getString(pf));
                this.primaryFocus.put(staff, focus);
            }
        }
    }
    
    public boolean isWatchedPlayer(UUID uuid) {
        for (WatchlistEntry entry : this.entries) {
            if (entry.getTarget().equals(uuid)) {
                return true;
            }
        }
        return false;
    }
    
    public void clearFocus(UUID uniqueId) {
        this.primaryFocus.remove(uniqueId);
    }
    
    public Set<WatchlistEntry> getEntries() {
        return entries;
    }
    
    public void addEntry(WatchlistEntry entry) {
        this.entries.add(entry);
    }
    
    public void removeEntry(WatchlistEntry entry) {
        this.entries.remove(entry);
    }
    
    public WatchlistEntry getEntry(UUID target) {
        for (WatchlistEntry entry : entries) {
            if (entry.getTarget().equals(target)) {
                return entry;
            }
        }
        return null;
    }
    
    public UUID getPrimaryFocus(UUID uniqueId) {
        return this.primaryFocus.get(uniqueId);
    }
    
    public void setPrimaryFocus(UUID uniqueId, UUID uuid) {
        this.primaryFocus.put(uniqueId, uuid);
    }
}