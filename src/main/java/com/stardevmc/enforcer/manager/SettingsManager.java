package com.stardevmc.enforcer.manager;

import com.stardevmc.enforcer.Enforcer;

import java.io.File;

public class SettingsManager {
    
    private final int configVersion = 3;
    
    private boolean usingDisplayNames, confirmPunishments, replaceActorName;
    
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
        if (!plugin.getConfig().contains("confirmpunishments")) {
            plugin.getConfig().set("confirmpunishments", true);
        }
        this.confirmPunishments = plugin.getConfig().getBoolean("confirmpunishments");
        if (!plugin.getConfig().contains("replaceactorname")) {
            plugin.getConfig().set("replaceactorname", true);
        }
        this.replaceActorName = plugin.getConfig().getBoolean("replaceactorname");
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
    
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
    
    public void setReplaceActorName(boolean replaceActorName) {
        this.replaceActorName = replaceActorName;
    }
    
    public boolean getReplaceActorName() {
        return replaceActorName;
    }
}