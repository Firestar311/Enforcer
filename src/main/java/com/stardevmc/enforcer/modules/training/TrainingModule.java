package com.stardevmc.enforcer.modules.training;

import com.stardevmc.enforcer.Enforcer;
import com.stardevmc.enforcer.modules.base.Module;

public class TrainingModule extends Module<TrainingManager> {
    public TrainingModule(Enforcer plugin, String... commands) {
        super(plugin, "training", new TrainingManager(plugin), commands);
    }
    
    public void setup() {
        if (enabled) {
            manager.loadData();
        }
        
        TrainingCommand trainingCommand = new TrainingCommand(plugin);
        registerCommands(trainingCommand);
    }
    
    public void desetup() {
        manager.saveData();
        registerCommands(null);
    }
}