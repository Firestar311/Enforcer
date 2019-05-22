package com.firestar311.enforcer.manager;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.lib.config.ConfigManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class TrainingModeManager {
    
    private ConfigManager configManager;
    
    private boolean globalTrainingMode = false;
    private Set<UUID> trainingMode = new HashSet<>();
    
    public TrainingModeManager(Enforcer plugin) {
        this.configManager = new ConfigManager(plugin, "trainingmode");
        this.configManager.setup();
    }
    
    public void saveTrainingData() {
        FileConfiguration config = configManager.getConfig();
        config.set("global", globalTrainingMode);
        List<String> tm = new ArrayList<>();
        trainingMode.forEach(uuid -> tm.add(uuid.toString()));
        config.set("individual", tm);
    }
    
    public void loadTrainingData() {
        FileConfiguration config = configManager.getConfig();
        this.globalTrainingMode = config.getBoolean("global");
        List<String> tm = config.getStringList("individual");
        tm.forEach(u -> trainingMode.add(UUID.fromString(u)));
    }
    
    public void setGlobalTrainingMode(boolean value) {
        this.globalTrainingMode = value;
    }
    
    public boolean getGlobalTrainingMode() {
        return globalTrainingMode;
    }
    
    public boolean toggleTrainingMode(UUID uuid) {
        if (this.trainingMode.contains(uuid)) {
            this.trainingMode.remove(uuid);
            return false;
        } else {
            this.trainingMode.add(uuid);
            return true;
        }
    }
    
    public boolean isTrainingMode(UUID uuid) {
        return globalTrainingMode || trainingMode.contains(uuid);
    }
}