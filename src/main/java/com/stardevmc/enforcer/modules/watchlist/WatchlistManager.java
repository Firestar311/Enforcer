package com.stardevmc.enforcer.modules.watchlist;

import com.stardevmc.enforcer.Enforcer;
import com.stardevmc.enforcer.modules.base.Manager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class WatchlistManager extends Manager {
    
    private Set<WatchlistEntry> entries = new HashSet<>();
    
    public WatchlistManager(Enforcer plugin) {
        super(plugin, "watchlist");
    }
    
    public void saveData() {
        FileConfiguration config = this.configManager.getConfig();
        config.set("entries", null);
        
        for (WatchlistEntry entry : this.entries) {
            config.set("entries." + entry.getTarget().toString(), entry);
        }
        this.configManager.saveConfig();
    }
    
    public void loadData() {
        FileConfiguration config = this.configManager.getConfig();
        ConfigurationSection section = config.getConfigurationSection("entries");
        if (section == null) return;
        for (String e : section.getKeys(false)) {
            WatchlistEntry entry = (WatchlistEntry) config.get("entries." + e);
            entries.add(entry);
        }
    }
    
    public Set<WatchlistEntry> getEntries() {
        return entries;
    }
    
    public void addEntry(WatchlistEntry entry) {
        this.entries.add(entry);
    }
}