package com.firestar311.enforcer.manager;

import com.firestar311.enforcer.Enforcer;

import java.io.File;

public class SettingsManager {
    
    private final int configVersion = 2;
    
    private boolean usingDisplayNames, confirmPunishments;
    
    private String prefix;
    private String serverName;
    
    public SettingsManager(Enforcer plugin) {
        if (plugin.getConfig().getInt("config-version") != this.configVersion) {
            File pluginConfig = new File(plugin.getDataFolder() + File.separator, "config.yml");
            pluginConfig.delete();
            plugin.saveDefaultConfig();
        }
        
        if (!plugin.getConfig().contains("usingdisplaynames")) {
            plugin.getConfig().set("usingdisplaynames", false);
        }
        this.usingDisplayNames = plugin.getConfig().getBoolean("usingdisplaynames");
        if (!plugin.getConfig().contains("prefix")) {
            plugin.getConfig().set("prefix", "Enforcer");
        }
        this.prefix = plugin.getConfig().getString("prefix");
        if (!plugin.getConfig().contains("server")) {
            plugin.getConfig().set("server", "Server");
        }
        this.serverName = plugin.getConfig().getString("server");
        if (!plugin.getConfig().getBoolean("confirmpunishments")) {
            plugin.getConfig().set("confirmpunishments", true);
        }
        this.confirmPunishments = plugin.getConfig().getBoolean("confirmpunishments");
        plugin.saveConfig();
    }
    
    public int getConfigVersion() {
        return configVersion;
    }
    
    public boolean isUsingDisplayNames() {
        return usingDisplayNames;
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    public String getServerName() {
        return serverName;
    }
    
    public void setUsingDisplayNames(boolean usingDisplayNames) {
        this.usingDisplayNames = usingDisplayNames;
    }
    
    public boolean mustConfirmPunishments() {
        return confirmPunishments;
    }
    
    public SettingsManager setConfirmPunishments(boolean confirmPunishments) {
        this.confirmPunishments = confirmPunishments;
        return this;
    }
}