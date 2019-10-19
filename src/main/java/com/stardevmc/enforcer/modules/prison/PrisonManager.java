package com.stardevmc.enforcer.modules.prison;

import com.stardevmc.enforcer.Enforcer;
import com.stardevmc.enforcer.modules.base.Manager;
import com.firestar311.lib.util.Utils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.Map.Entry;

public class PrisonManager extends Manager {
    
    private Map<Integer, Prison> prisons = new TreeMap<>();
    
    public PrisonManager(Enforcer plugin) {
        super(plugin, "prisons");
    }
    
    public void saveData() {
        FileConfiguration config = configManager.getConfig();
        config.set("prisons", null);
        for (Entry<Integer, Prison> entry : this.prisons.entrySet()) {
            Integer id = entry.getKey();
            Prison prison = entry.getValue();
            config.set("prisons." + id, prison.serialize());
        }
        configManager.saveConfig();
    }
    
    public void loadData() {
        FileConfiguration config = configManager.getConfig();
        if (config.contains("prisons")) {
            for (String i : config.getConfigurationSection("prisons").getKeys(false)) {
                int id = Integer.parseInt(i);
                Prison prison = (Prison) config.get("prisons." + i);
                this.prisons.put(id, prison);
            }
        }
    }
    
    public Prison findPrison() {
        Prison prison = null;
        boolean allPrisonsFull = true;
        for (Prison j : this.prisons.values()) {
            if (!j.isFull()) {
                allPrisonsFull = false;
                prison = j;
                break;
            }
        }
        if (allPrisonsFull) {
            for (Prison j : this.prisons.values()) {
                if (prison == null) {
                    prison = j;
                } else {
                    int amountOver1 = prison.getInhabitants().size() - prison.getMaxPlayers();
                    int amountOver2 = j.getInhabitants().size() - j.getMaxPlayers();
                    if (amountOver2 < amountOver1) {
                        prison = j;
                    }
                }
            }
        }
        return prison;
    }
    
    public Prison getPrison(int id) {
        return this.prisons.get(id);
    }
    
    public Set<Prison> getPrisons() {
        return new HashSet<>(this.prisons.values());
    }
    
    public void addPrison(Prison prison) {
        if (prison.getId() == -1) {
            prison.setId(this.prisons.size());
        }
        this.prisons.put(prison.getId(), prison);
    }
    
    public Prison getPrison(UUID uuid) {
        for (Prison prison : this.prisons.values()) {
            if (prison.isInhabitant(uuid)) {
                return prison;
            }
        }
        return null;
    }
    
    public void removePrison(int id) {
        this.prisons.remove(id);
    }
    
    public Prison getPrisonFromString(Player player, String i) {
        int prisonId = -1;
        try {
            prisonId = Integer.parseInt(i);
        } catch (NumberFormatException e) {
            for (Prison prison : getPrisons()) {
                if (prison.getName() != null) {
                    if (prison.getName().equalsIgnoreCase(i)) {
                        prisonId = prison.getId();
                    }
                }
            }
            if (prisonId == -1) {
                player.sendMessage(Utils.color("&cA prison could not be found with that name or id."));
                return null;
            }
        }
        
        Prison prison = getPrison(prisonId);
        if (prison == null) {
            player.sendMessage(Utils.color("&cA prison could not be found with that id."));
            return null;
        }
        return prison;
    }
    
    public Set<Prison> getPrisonsWithOverflow() {
        Set<Prison> prisons = new HashSet<>();
        for (Prison prison : getPrisons()) {
            if (prison.getInhabitants().size() > prison.getMaxPlayers()) {
                prisons.add(prison);
            }
        }
        
        return prisons;
    }
}