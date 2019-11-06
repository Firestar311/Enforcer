package com.stardevmc.enforcer.modules.base;

import com.stardevmc.enforcer.Enforcer;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.HandlerList;

import java.lang.reflect.Constructor;
import java.util.*;

public abstract class Module<M extends Manager> {
    
    protected Enforcer plugin;
    protected String name;
    protected Map<String, Boolean> commands = new HashMap<>();
    protected boolean enabled = true;
    protected List<Module<?>> requiredModules = new ArrayList<>();
    protected List<Class<? extends ModuleListener>> listeners = new ArrayList<>();
    private List<ModuleListener> listenerInstances = new ArrayList<>();
    
    protected final NotEnabledCommand notEnabledExecutor = new NotEnabledCommand();
    
    protected M manager;
    
    public Module(Enforcer plugin, String name, M manager, String... commands) {
        this.plugin = plugin;
        this.name = name;
        this.manager = manager;
        
        if (commands != null) {
            for (String cmd : commands) {
                this.commands.put(cmd.toLowerCase(), true);
            }
        }
    }
    
    public Module(Enforcer plugin, String name, M manager, Map<String, Boolean> commands) {
        this.plugin = plugin;
        this.name = name;
        this.manager = manager;
        
        if (commands != null && !commands.isEmpty()) {
            commands.keySet().forEach(c -> c = c.toLowerCase());
            this.commands.putAll(commands);
        }
    }
    
    public abstract void setup();
    
    public abstract void desetup();
    
    public void enable() {
        setEnabled(true);
        setup();
    }
    
    public void disable() {
        setEnabled(false);
    }
    
    public String getName() {
        return name;
    }
    
    public M getManager() {
        return manager;
    }
    
    public void addCommand(String command) {
        this.commands.put(command.toLowerCase(), true);
    }
    
    public void addCommand(String command, boolean status) {
        this.commands.put(command.toLowerCase(), status);
    }
    
    public void enableCommand(String command) {
        this.commands.put(command.toLowerCase(), true);
    }
    
    public void disableCommand(String command) {
        this.commands.put(command.toLowerCase(), false);
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public boolean getCommandStatus(String command) {
        return this.commands.get(command.toLowerCase());
    }
    
    public void registerCommands(CommandExecutor executor) {
        this.commands.forEach((cmd, status) -> {
            if (executor != null || status) {
                plugin.getCommand(cmd).setExecutor(executor);
            } else {
                plugin.getCommand(cmd).setExecutor(notEnabledExecutor);
            }
        });
    }
    
    public List<Module<?>> getRequiredModules() {
        return new ArrayList<>(requiredModules);
    }
    
    public void addRequiredModule(Module<?> module) {
        this.requiredModules.add(module);
    }
    
    public void addListenerClass(Class<? extends ModuleListener>... clazzes) {
        this.listeners.addAll(Arrays.asList(clazzes));
    }
    
    public final void registerListeners() {
        unregisterListeners();
        for (Class<? extends ModuleListener> listenerClass : this.listeners) {
            try {
                Constructor<?> listenerConstructor = listenerClass.getDeclaredConstructor();
                ModuleListener instance = (ModuleListener) listenerConstructor.newInstance();
                this.listenerInstances.add(instance);
                Enforcer.getInstance().getServer().getPluginManager().registerEvents(instance, Enforcer.getInstance());
            } catch (Exception e) {
            
            }
        }
    }
    
    public final void unregisterListeners() {
        if (this.listenerInstances.isEmpty()) return;
        for (ModuleListener listener : this.listenerInstances) {
            HandlerList.unregisterAll(listener);
        }
    }
}