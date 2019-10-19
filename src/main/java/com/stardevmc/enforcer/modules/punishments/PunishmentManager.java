package com.stardevmc.enforcer.modules.punishments;

import com.stardevmc.enforcer.Enforcer;
import com.stardevmc.enforcer.modules.base.Manager;
import com.stardevmc.enforcer.modules.prison.Prison;
import com.stardevmc.enforcer.modules.punishments.actor.PlayerActor;
import com.stardevmc.enforcer.modules.punishments.target.*;
import com.stardevmc.enforcer.modules.punishments.type.abstraction.*;
import com.stardevmc.enforcer.modules.punishments.type.impl.*;
import com.stardevmc.enforcer.modules.punishments.type.interfaces.Expireable;
import com.stardevmc.enforcer.modules.reports.enums.ReportOutcome;
import com.stardevmc.enforcer.modules.reports.enums.ReportStatus;
import com.stardevmc.enforcer.modules.rules.rule.Rule;
import com.stardevmc.enforcer.util.Code;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class PunishmentManager extends Manager {
    
    private Map<Integer, String> ackCodes = new TreeMap<>();
    private Map<Integer, Punishment> punishments = new TreeMap<>();
    
    public PunishmentManager(Enforcer plugin) {
        super(plugin, "punishments");
        this.configManager.setup();
    }
    
    public void saveData() {
        FileConfiguration config = configManager.getConfig();
        for (Entry<Integer, Punishment> entry : this.punishments.entrySet()) {
            config.set("punishments." + entry.getKey(), entry.getValue());
        }
        configManager.saveConfig();
    }
    
    public void loadData() {
        FileConfiguration config = configManager.getConfig();
        ConfigurationSection punishmentsSection = config.getConfigurationSection("punishments");
        if (punishmentsSection == null) { return; }
        for (String p : punishmentsSection.getKeys(false)) {
            Punishment punishment = (Punishment) punishmentsSection.get(p);
            this.punishments.put(punishment.getId(), punishment);
        }
    }
    
    public void addBan(BanPunishment punishment) {
        this.addPunishment(punishment);
    }
    
    public void addPunishment(Punishment punishment) {
        if (punishment.getId() == -1 || this.punishments.containsKey(punishment.getId())) {
            int id = this.punishments.keySet().size();
            punishment.setId(id);
        }
        
        if (plugin.getTrainingModule().getManager().isTrainingMode(punishment.getPunisher())) {
            punishment.setTrainingMode(true);
        }
        
        plugin.getReportModule().getManager().getReports().values().stream().filter(report -> report.getTarget().equals(punishment.getTarget())).filter(report -> report.getReason().equalsIgnoreCase(punishment.getReason())).forEach(report -> {
            report.addPunishment(punishment);
            report.setOutcome(ReportOutcome.ACCEPTED);
            report.setStatus(ReportStatus.CLOSED);
        });
        
        this.punishments.put(punishment.getId(), punishment);
    }
    
    public boolean isBanned(UUID uuid) {
        for (Punishment punishment : getBans(uuid)) {
            if (checkActive(punishment)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isBlacklisted(String ip) {
        for (Punishment punishment : this.punishments.values()) {
            if (punishment instanceof BlacklistPunishment) {
                if (punishment.getTarget() instanceof IPTarget) {
                    return ip.equals(punishment.getTarget().getName());
                } else if (punishment.getTarget() instanceof IPListTarget) {
                    for (String tIp : ((IPListTarget) punishment.getTarget()).getIpAddresses()) {
                        if (tIp.equals(ip)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    public Set<Punishment> getBans(UUID uuid) {
        Set<Punishment> set = new HashSet<>();
        for (Punishment punishment : punishments.values()) {
            if (punishment instanceof BanPunishment) {
                if (punishment.getTarget() instanceof PlayerTarget) {
                    PlayerTarget playerTarget = (PlayerTarget) punishment.getTarget();
                    if (playerTarget.getUniqueId().equals(uuid)) {
                        set.add(punishment);
                    }
                }
            }
        }
        return set;
    }
    
    private boolean checkActive(Punishment punishment) {
        if (punishment instanceof Expireable) {
            Expireable expireable = ((Expireable) punishment);
            if (expireable.isExpired()) {
                expireable.onExpire();
            }
        }
        if (punishment.isActive()) {
            if (punishment.getPunisher() instanceof PlayerActor) {
                if (punishment.isTrainingPunishment()) {
                    return plugin.getTrainingModule().getManager().isTrainingMode(punishment.getPunisher());
                }
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
    
    public Set<Punishment> getMutes(UUID uuid) {
        Set<Punishment> set = new HashSet<>();
        for (Punishment punishment : punishments.values()) {
            if (punishment instanceof MutePunishment) {
                if (punishment.getTarget() instanceof PlayerTarget) {
                    PlayerTarget playerTarget = (PlayerTarget) punishment.getTarget();
                    if (playerTarget.getUniqueId().equals(uuid)) {
                        set.add(punishment);
                    }
                }
            }
        }
        return set;
    }
    
    public void addWarning(WarnPunishment punishment) {
        addPunishment(punishment);
    }
    
    public boolean hasBeenWarned(UUID uuid) {
        for (Punishment punishment : getWarnings(uuid)) {
            if (punishment.isActive()) {
                if (punishment.isTrainingPunishment()) {
                    return plugin.getTrainingModule().getManager().isTrainingMode(punishment.getPunisher());
                }
                return true;
            }
        }
        return false;
    }
    
    public Set<Punishment> getWarnings(UUID uuid) {
        Set<Punishment> set = new HashSet<>();
        for (Punishment punishment : punishments.values()) {
            if (punishment instanceof WarnPunishment) {
                if (punishment.getTarget() instanceof PlayerTarget) {
                    PlayerTarget playerTarget = (PlayerTarget) punishment.getTarget();
                    if (playerTarget.getUniqueId().equals(uuid)) {
                        set.add(punishment);
                    }
                }
            }
        }
        return set;
    }
    
    public void addKick(KickPunishment punishment) {
        addPunishment(punishment);
    }
    
    public boolean hasBeenKicked(UUID uuid) {
        for (Punishment punishment : getKicks(uuid)) {
            if (punishment.isActive()) {
                if (punishment.isTrainingPunishment()) {
                    return plugin.getTrainingModule().getManager().isTrainingMode(punishment.getPunisher());
                }
                return true;
            }
        }
        return false;
    }
    
    public Set<Punishment> getKicks(UUID uuid) {
        Set<Punishment> set = new HashSet<>();
        for (Punishment punishment : punishments.values()) {
            if (punishment instanceof KickPunishment) {
                if (punishment.getTarget() instanceof PlayerTarget) {
                    PlayerTarget playerTarget = (PlayerTarget) punishment.getTarget();
                    if (playerTarget.getUniqueId().equals(uuid)) {
                        set.add(punishment);
                    }
                }
            }
        }
        return set;
    }
    
    public void addJailPunishment(JailPunishment punishment) {
        addPunishment(punishment);
        if (punishment.isActive()) {
            try {
                Prison prison = plugin.getPrisonModule().getManager().findPrison();
                punishment.setPrisonId(prison.getId());
            } catch (Exception e) {
                plugin.getLogger().severe("Could not find a prison for an active jail punishment with id " + punishment.getId());
            }
        }
    }
    
    public boolean isJailed(UUID uuid) {
        for (Punishment punishment : getJailPunishments(uuid)) {
            if (punishment.isActive()) {
                if (punishment.isTrainingPunishment()) {
                    return plugin.getTrainingModule().getManager().isTrainingMode(punishment.getPunisher());
                }
                return true;
            }
        }
        return false;
    }
    
    public Set<Punishment> getJailPunishments(UUID uuid) {
        Set<Punishment> set = new HashSet<>();
        for (Punishment punishment : punishments.values()) {
            if (punishment instanceof JailPunishment) {
                if (punishment.getTarget() instanceof PlayerTarget) {
                    PlayerTarget playerTarget = (PlayerTarget) punishment.getTarget();
                    if (playerTarget.getUniqueId().equals(uuid)) {
                        set.add(punishment);
                    }
                }
            }
        }
        return set;
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
    
    public Set<Punishment> getActivePunishments() {
        Set<Punishment> punishments = new HashSet<>();
        punishments.addAll(getActiveBans());
        punishments.addAll(getActiveMutes());
        punishments.addAll(getActiveJails());
        return punishments;
    }
    
    public Set<BanPunishment> getActiveBans() {
        Set<BanPunishment> bans = new HashSet<>();
        for (Punishment punishment : this.punishments.values().stream().filter(BanPunishment.class::isInstance).collect(Collectors.toSet())) {
            if (punishment.isActive()) {
                bans.add((BanPunishment) punishment);
            }
        }
        
        return bans;
    }
    
    public Set<MutePunishment> getActiveMutes() {
        Set<MutePunishment> mutes = new HashSet<>();
        for (Punishment punishment : this.punishments.values().stream().filter(MutePunishment.class::isInstance).collect(Collectors.toSet())) {
            if (punishment.isActive()) {
                mutes.add((MutePunishment) punishment);
            }
        }
        
        return mutes;
    }
    
    public Set<JailPunishment> getActiveJails() {
        Set<JailPunishment> jails = new HashSet<>();
        for (Punishment punishment : this.punishments.values().stream().filter(JailPunishment.class::isInstance).collect(Collectors.toSet())) {
            if (punishment.isActive()) {
                jails.add((JailPunishment) punishment);
            }
        }
        
        return jails;
    }
    
    public Set<Punishment> getActivePunishments(UUID uuid) {
        Set<Punishment> punishments = new HashSet<>();
        punishments.addAll(getActiveBans(uuid));
        punishments.addAll(getActiveMutes(uuid));
        punishments.addAll(getActiveJails(uuid));
        punishments.addAll(getActiveBlacklists(uuid));
        return punishments;
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
    
    public String getAckCode(UUID uuid) {
        for (int id : this.ackCodes.keySet()) {
            WarnPunishment punishment = (WarnPunishment) this.punishments.get(id);
            if (punishment.getTarget() instanceof PlayerTarget) {
                if (((PlayerTarget) punishment.getTarget()).getUniqueId().equals(uuid)) {
                    return this.ackCodes.get(id);
                }
            }
        }
        
        return null;
    }
    
    public Set<Punishment> getPunishmentsByRule(UUID target, Rule rule, boolean trainingMode) {
        Set<Punishment> punishments = new HashSet<>();
        
        for (Punishment punishment : getPunishments()) {
            if (punishment.getTarget() instanceof PlayerTarget) {
                PlayerTarget playerTarget = (PlayerTarget) punishment.getTarget();
                if (playerTarget.getUniqueId().equals(target)) {
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
        }
        
        return punishments;
    }
    
    public Set<Punishment> getPunishments() {
        return new HashSet<>(punishments.values());
    }
    
    public Set<Punishment> getPunishments(UUID uuid) {
        Set<Punishment> punishments = new HashSet<>();
        for (Punishment punishment : this.punishments.values()) {
            if (punishment.getTarget() instanceof PlayerTarget) {
                if (((PlayerTarget) punishment.getTarget()).getUniqueId().equals(uuid)) {
                    punishments.add(punishment);
                }
            }
        }
        return punishments;
    }
    
    public Set<Punishment> getActiveBlacklists(UUID uuid) {
        Set<Punishment> punishments = new HashSet<>();
        List<String> ipAddresses = plugin.getPlayerManager().getPlayerInfo(uuid).getIpAddresses();
        punishmentLoop:
        for (Punishment punishment : this.punishments.values()) {
            if (!(punishment instanceof BlacklistPunishment)) {
                continue;
            }
            
            BlacklistPunishment blacklistPunishment = (BlacklistPunishment) punishment;
            Target target = blacklistPunishment.getTarget();
            if (target instanceof IPTarget) {
                IPTarget ipTarget = (IPTarget) target;
                if (ipAddresses.contains(ipTarget.getIpAddress())) {
                    punishments.add(punishment);
                }
            } else if (target instanceof IPListTarget) {
                IPListTarget ipListTarget = (IPListTarget) target;
                for (String playerIp : ipAddresses) {
                    for (String targetIp : ipListTarget.getIpAddresses()) {
                        if (playerIp.equals(targetIp)) {
                            punishments.add(punishment);
                            continue punishmentLoop;
                        }
                    }
                }
            }
        }
        return punishments;
    }
    
    public Set<Punishment> getActiveBlacklists(String ip) {
        Set<Punishment> punishments = new HashSet<>();
        punishmentLoop:
        for (Punishment punishment : this.punishments.values()) {
            if (!(punishment instanceof BlacklistPunishment)) {
                continue;
            }
        
            BlacklistPunishment blacklistPunishment = (BlacklistPunishment) punishment;
            Target target = blacklistPunishment.getTarget();
            if (target instanceof IPTarget) {
                IPTarget ipTarget = (IPTarget) target;
                if (ipTarget.getIpAddress().equals(ip)) {
                    punishments.add(punishment);
                }
            } else if (target instanceof IPListTarget) {
                IPListTarget ipListTarget = (IPListTarget) target;
                for (String targetIp : ipListTarget.getIpAddresses()) {
                    if (targetIp.equals(ip)) {
                        punishments.add(punishment);
                        continue punishmentLoop;
                    }
                }
            }
        }
        return punishments;
    }
}