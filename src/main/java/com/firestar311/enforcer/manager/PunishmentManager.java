package com.firestar311.enforcer.manager;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.model.Prison;
import com.firestar311.enforcer.model.enums.ReportOutcome;
import com.firestar311.enforcer.model.enums.ReportStatus;
import com.firestar311.enforcer.model.punishment.abstraction.*;
import com.firestar311.enforcer.model.punishment.interfaces.Expireable;
import com.firestar311.enforcer.model.punishment.type.*;
import com.firestar311.enforcer.model.rule.Rule;
import com.firestar311.enforcer.util.Code;
import com.firestar311.lib.config.ConfigManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class PunishmentManager {
    
    private final int CODE_AMOUNT = 6;
    private Enforcer plugin;
    private ConfigManager configManager;
    private Map<Integer, Punishment> punishments = new TreeMap<>();
    private Map<Integer, String> ackCodes = new TreeMap<>();
    
    public PunishmentManager(Enforcer plugin) {
        this.plugin = plugin;
        
        this.configManager = new ConfigManager(plugin, "punishments");
        this.configManager.setup();
    }
    
    public void savePunishmentData() {
        FileConfiguration config = configManager.getConfig();
        for (Punishment punishment : punishments.values()) {
            Map<String, Object> serialized = Punishment.serialize(punishment);
            for (Entry<String, Object> entry : serialized.entrySet()) {
                config.set("punishments." + punishment.getId() + "." + entry.getKey(), entry.getValue());
            }
        }
        configManager.saveConfig();
    }
    
    public void loadPunishmentData() {
        FileConfiguration config = configManager.getConfig();
        if (!config.contains("punishments")) {
            plugin.getLogger().info("Could not find any punishments to load.");
            return;
        }
        
        for (String pi : config.getConfigurationSection("punishments").getKeys(false)) {
            Map<String, Object> serialized = new HashMap<>();
            for (String key : config.getConfigurationSection("punishments." + pi).getKeys(false)) {
                serialized.put(key, config.get("punishments." + pi + "." + key));
            }
            
            Punishment punishment = Punishment.deserialize(serialized);
            this.punishments.put(punishment.getId(), punishment);
        }
    }
    
    public void addPunishment(Punishment punishment) {
        if (punishment.getId() == -1 || this.punishments.containsKey(punishment.getId())) {
            int id = this.punishments.keySet().size();
            punishment.setId(id);
        }
        
        if (plugin.getTrainingModeManager().isTrainingMode(punishment.getPunisher())) {
            punishment.setTrainingMode(true);
        }
        
        plugin.getReportManager().getReports().values().stream()
              .filter(report -> report.getTarget().equals(punishment.getTarget()))
              .filter(report -> report.getReason().equalsIgnoreCase(punishment.getReason())).forEach(report -> {
            report.addPunishment(punishment);
            report.setOutcome(ReportOutcome.ACCEPTED);
            report.setStatus(ReportStatus.CLOSED);
        });
        
        this.punishments.put(punishment.getId(), punishment);
    }
    
    public void addBan(BanPunishment punishment) {
        this.addPunishment(punishment);
    }
    
    public boolean isBanned(UUID uuid) {
        for (Punishment punishment : getBans(uuid)) {
            if (checkActive(punishment)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean checkActive(Punishment punishment) {
        if (punishment instanceof Expireable) {
            Expireable expireable = ((Expireable) punishment);
            if (expireable.isExpired()) {
                expireable.onExpire();
            }
        }
        if (punishment.isActive()) {
            if (punishment.isTrainingPunishment()) {
                return plugin.getTrainingModeManager().isTrainingMode(punishment.getPunisher());
            }
            return true;
        }
        return false;
    }
    
    public void addMute(MutePunishment punishment) {
        addPunishment(punishment);
    }
    
    public boolean isMuted(UUID uuid) {
        for (Punishment punishment : getMutes(uuid)) {
            if (checkActive(punishment)) {
                return true;
            }
        }
        return false;
    }
    
    public void addWarning(WarnPunishment punishment) {
        addPunishment(punishment);
    }
    
    public boolean hasBeenWarned(UUID uuid) {
        for (Punishment punishment : getWarnings(uuid)) {
            if (punishment.isActive()) {
                if (punishment.isTrainingPunishment()) {
                    return plugin.getTrainingModeManager().isTrainingMode(punishment.getPunisher());
                }
                return true;
            }
        }
        return false;
    }
    
    public void addKick(KickPunishment punishment) {
        addPunishment(punishment);
    }
    
    public boolean hasBeenKicked(UUID uuid) {
        for (Punishment punishment : getKicks(uuid)) {
            if (punishment.isActive()) {
                if (punishment.isTrainingPunishment()) {
                    return plugin.getTrainingModeManager().isTrainingMode(punishment.getPunisher());
                }
                return true;
            }
        }
        return false;
    }
    
    public void addJailPunishment(JailPunishment punishment) {
        addPunishment(punishment);
        if (punishment.isActive()) {
            try {
                Prison prison = plugin.getPrisonManager().findPrison();
                punishment.setPrisonId(prison.getId());
            } catch (Exception e) {
                plugin.getLogger()
                      .severe("Could not find a prison for an active jail punishment with id " + punishment.getId());
            }
        }
    }
    
    public boolean isJailed(UUID uuid) {
        for (Punishment punishment : getJailPunishments(uuid)) {
            if (punishment.isActive()) {
                if (punishment.isTrainingPunishment()) {
                    return plugin.getTrainingModeManager().isTrainingMode(punishment.getPunisher());
                }
                return true;
            }
        }
        return false;
    }
    
    public void addAckCode(int id, String code) {
        this.ackCodes.put(id, code);
    }
    
    public String generateAckCode(int id) {
        String code = Code.generateNewCode(6);
        this.ackCodes.put(id, code);
        return code;
    }
    
    public Punishment getPunishment(int id) {
        return this.punishments.get(id);
    }
    
    public Set<Punishment> getBans(UUID uuid) {
        return punishments.values().stream().filter(BanPunishment.class::isInstance)
                          .filter(punishment -> punishment.getTarget().equals(uuid)).collect(Collectors.toSet());
    }
    
    public Set<Punishment> getMutes(UUID uuid) {
        return punishments.values().stream().filter(MutePunishment.class::isInstance)
                          .filter(punishment -> punishment.getTarget().equals(uuid)).collect(Collectors.toSet());
    }
    
    public Set<Punishment> getWarnings(UUID uuid) {
        return punishments.values().stream().filter(WarnPunishment.class::isInstance)
                          .filter(punishment -> punishment.getTarget().equals(uuid)).collect(Collectors.toSet());
    }
    
    public Set<Punishment> getKicks(UUID uuid) {
        return punishments.values().stream().filter(KickPunishment.class::isInstance)
                          .filter(punishment -> punishment.getTarget().equals(uuid)).collect(Collectors.toSet());
    }
    
    public Set<Punishment> getJailPunishments(UUID uuid) {
        return punishments.values().stream().filter(JailPunishment.class::isInstance)
                          .filter(punishment -> punishment.getTarget().equals(uuid)).collect(Collectors.toSet());
    }
    
    public Set<Punishment> getActiveBans(UUID uuid) {
        return this.getBans(uuid).stream().filter(Punishment::isActive).collect(Collectors.toSet());
    }
    
    public Set<Punishment> getActiveMutes(UUID uuid) {
        return this.getMutes(uuid).stream().filter(Punishment::isActive).collect(Collectors.toSet());
    }
    
    public Set<Punishment> getActiveJails(UUID uuid) {
        return this.getJailPunishments(uuid).stream().filter(Punishment::isActive).collect(Collectors.toSet());
    }
    
    public Set<BanPunishment> getActiveBans() {
        Set<BanPunishment> bans = new HashSet<>();
        for (Punishment punishment : this.punishments.values().stream().filter(BanPunishment.class::isInstance)
                                                     .collect(Collectors.toSet())) {
            if (punishment.isActive()) {
                bans.add((BanPunishment) punishment);
            }
        }
        
        return bans;
    }
    
    public Set<MutePunishment> getActiveMutes() {
        Set<MutePunishment> mutes = new HashSet<>();
        for (Punishment punishment : this.punishments.values().stream().filter(MutePunishment.class::isInstance)
                                                     .collect(Collectors.toSet())) {
            if (punishment.isActive()) {
                mutes.add((MutePunishment) punishment);
            }
        }
        
        return mutes;
    }
    
    public Set<JailPunishment> getActiveJails() {
        Set<JailPunishment> jails = new HashSet<>();
        for (Punishment punishment : this.punishments.values().stream().filter(JailPunishment.class::isInstance)
                                                     .collect(Collectors.toSet())) {
            if (punishment.isActive()) {
                jails.add((JailPunishment) punishment);
            }
        }
        
        return jails;
    }
    
    public Set<Punishment> getActivePunishments() {
        Set<Punishment> punishments = new HashSet<>();
        punishments.addAll(getActiveBans());
        punishments.addAll(getActiveMutes());
        punishments.addAll(getActiveJails());
        return punishments;
    }
    
    public Set<Punishment> getActivePunishments(UUID uuid) {
        Set<Punishment> punishments = new HashSet<>();
        punishments.addAll(getActiveBans(uuid));
        punishments.addAll(getActiveMutes(uuid));
        punishments.addAll(getActiveJails(uuid));
        return punishments;
    }
    
    public Set<Punishment> getPunishments() {
        return new HashSet<>(punishments.values());
    }
    
    public String getAckCode(UUID uuid) {
        for (int id : this.ackCodes.keySet()) {
            WarnPunishment punishment = (WarnPunishment) this.punishments.get(id);
            if (punishment.getTarget().equals(uuid)) {
                return this.ackCodes.get(id);
            }
        }
        
        return null;
    }
    
    public Set<Punishment> getPunishmentsByRule(UUID target, Rule rule, boolean trainingMode) {
        Set<Punishment> punishments = new HashSet<>();
        
        for (Punishment punishment : plugin.getPunishmentManager().getPunishments()) {
            if (punishment.getTarget().equals(target)) {
                if (punishment.getRuleId() == rule.getId()) {
                    boolean toAdd = true;
                    for (Punishment p : punishments) {
                        if (p.getOffenseNumber() == punishment.getOffenseNumber()) {
                            toAdd = false;
                        }
                        if (p.isTrainingPunishment() && punishment.isTrainingPunishment() && !trainingMode) {
                            toAdd = false;
                        }
                    }
                    if (toAdd) {
                        punishments.add(punishment);
                    }
                }
            }
        }
        
        return punishments;
    }
}