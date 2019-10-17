package com.firestar311.enforcer.modules.training;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.modules.base.Manager;
import com.firestar311.enforcer.modules.punishments.actor.Actor;
import com.firestar311.enforcer.modules.punishments.actor.PlayerActor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class TrainingManager extends Manager {
    
    private boolean globalTrainingMode = false;
    private Set<UUID> trainingMode = new HashSet<>();
    
    public TrainingManager(Enforcer plugin) {
        super(plugin, "training");
    }
    
    public void saveData() {
        FileConfiguration config = configManager.getConfig();
        config.set("global", globalTrainingMode);
        List<String> tm = new ArrayList<>();
        trainingMode.forEach(uuid -> tm.add(uuid.toString()));
        config.set("individual", tm);
    }
    
    public void loadData() {
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
    
    public boolean isTrainingMode(Actor actor) {
        if (globalTrainingMode) return true;
        if (actor instanceof PlayerActor) {
            PlayerActor playerActor = ((PlayerActor) actor);
            return trainingMode.contains(playerActor.getUniqueId());
        }
        return false;
    }
}