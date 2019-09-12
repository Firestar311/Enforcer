package com.firestar311.enforcer.modules.rules;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.modules.base.Manager;
import com.firestar311.enforcer.modules.punishments.type.PunishmentType;
import com.firestar311.enforcer.modules.punishments.type.abstraction.Punishment;
import com.firestar311.enforcer.modules.rules.rule.*;
import com.firestar311.lib.util.Unit;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.*;

public class RuleManager extends Manager {
    
    private SortedMap<Integer, Rule> rules = new TreeMap<>();
    
    public RuleManager(Enforcer plugin) {
        super(plugin, "rules", false);
        
        File rulesFile = new File(plugin.getDataFolder() + File.separator + "rules.yml");
        if (!rulesFile.exists()) {
            plugin.saveResource("rules.yml", true);
        }
        
        this.configManager.setup();
    }
    
    public void saveData() {
        FileConfiguration config = this.configManager.getConfig();
        config.set("rules", null);
        
        for (Rule rule : this.rules.values()) {
            String basePath = "rules." + rule.getInternalId();
            config.set(basePath + ".id", rule.getId());
            config.set(basePath + ".name", rule.getName());
            config.set(basePath + ".description", rule.getDescription());
            if (rule.getMaterial() != null) {
                config.set(basePath + ".material", rule.getMaterial().name());
            }
            
            if (rule.getOffenses().isEmpty()) { continue; }
            
            for (Entry<Integer, RuleOffense> actionEntry : rule.getOffenses().entrySet()) {
                config.set(basePath + ".offenses." + actionEntry.getKey() + ".length", actionEntry.getValue().getLength());
                String actionBase = basePath + ".offenses." + actionEntry.getKey() + ".actions";
                for (Entry<Integer, RulePunishment> punishmentEntry : actionEntry.getValue().getPunishments().entrySet()) {
                    String punishmentBase = actionBase + "." + punishmentEntry.getKey();
                    config.set(punishmentBase + ".punishment", punishmentEntry.getValue().getType().toString().toLowerCase());
                    config.set(punishmentBase + ".length", punishmentEntry.getValue().getcLength());
                    config.set(punishmentBase + ".unit", punishmentEntry.getValue().getcUnits());
                    config.set(punishmentBase + ".id", punishmentEntry.getValue().getId());
                }
            }
        }
        this.configManager.saveConfig();
    }
    
    public void loadData() {
        FileConfiguration config = configManager.getConfig();
        if (config.getConfigurationSection("rules") == null) { return; }
        for (String r : config.getConfigurationSection("rules").getKeys(false)) {
            Rule rule = new Rule(config.getInt("rules." + r + ".id"), r, config.getString("rules." + r + ".name"), config.getString("rules." + r + ".description"));
            if (config.contains("rules." + r + ".material")) {
                rule.setMaterial(Material.valueOf(config.getString("rules." + r + ".material").toUpperCase()));
            }
            if (config.contains("rules." + r + ".offenses")) {
                for (String o : config.getConfigurationSection("rules." + r + ".offenses").getKeys(false)) {
                    int offenseNumber = Integer.parseInt(o);
                    RuleOffense action = new RuleOffense(rule, offenseNumber);
                    int actionLength = 0;
                    if (config.contains("rules." + r + ".offenses." + o + ".length")) {
                        actionLength = config.getInt("rules." + r + ".offenses." + o + ".length");
                    }
                    action.setLength(actionLength);
                    for (String a : config.getConfigurationSection("rules." + r + ".offenses." + o + ".actions").getKeys(false)) {
                        int aN = Integer.parseInt(a);
                        PunishmentType type = PunishmentType.getType(config.getString("rules." + r + ".offenses." + o + ".actions." + a + ".punishment").toUpperCase());
                        int rawLength = -1;
                        String units = "";
                        int id = -1;
                        if (config.contains("rules." + r + ".offenses." + o + ".actions." + a + ".length")) {
                            rawLength = config.getInt("rules." + r + ".offenses." + o + ".actions." + a + ".length");
                        }
                        
                        if (config.contains("rules." + r + ".offenses." + o + ".actions." + a + ".unit")) {
                            units = config.getString("rules." + r + ".offenses." + o + ".actions." + a + ".unit");
                        }
                        
                        if (config.contains("rules." + r + ".offenses." + o + ".actions." + a + ".id")) {
                            id = config.getInt("rules." + r + ".offenses." + o + ".actions." + a + ".id");
                        }
                        
                        long length = -1;
                        if (!StringUtils.isEmpty(units)) {
                            Unit unit = Unit.matchUnit(units);
                            length = unit.convertTime(rawLength);
                        }
                        
                        RulePunishment punishment = new RulePunishment(type, length, rawLength, units);
                        punishment.setId(id);
                        action.addPunishment(aN, punishment);
                    }
                    rule.addOffense(offenseNumber, action);
                }
            }
            this.addRule(rule);
        }
    }
    
    public void addRule(Rule rule) {
        if (rule.getId() == -1) {
            int lastId = this.rules.lastKey();
            rule.setId(lastId + 1);
        }
        
        this.rules.put(rule.getId(), rule);
    }
    
    public void removeRule(int id) {
        this.rules.remove(id);
    }
    
    public Set<Rule> getRules() {
        return new TreeSet<>(this.rules.values());
    }
    
    public Rule getRule(String ruleString) {
        Rule rule = null;
        ruleString = StringUtils.strip(ruleString);
        try {
            int id = Integer.parseInt(ruleString);
            return this.rules.get(id);
        } catch (NumberFormatException e) {
            String ruleInternalName = ruleString.toLowerCase().replace(" ", "_");
            for (Rule r : this.getRules()) {
                if (r.getInternalId().equalsIgnoreCase(ruleInternalName)) {
                    rule = r;
                }
            }
        }
        
        return rule;
    }
    
    public Entry<Integer, Integer> getNextOffense(UUID punisher, UUID target, Rule rule) {
        Set<Punishment> punishments = plugin.getPunishmentModule().getManager().getPunishmentsByRule(target, rule, plugin.getTrainingModule().getManager().isTrainingMode(punisher));
        if (punishments.isEmpty()) { return new SimpleEntry<>(1, 1); }
        int offense = punishments.size() + 1;
        RuleOffense previousOffense = rule.getOffense(punishments.size());
        if (previousOffense.getLength() != 0) {
            Punishment latestPunishment = null;
            for (Punishment punishment : punishments) {
                if (latestPunishment == null) {
                    latestPunishment = punishment;
                } else {
                    if (punishment.getDate() > latestPunishment.getDate()) {
                        latestPunishment = punishment;
                    }
                }
            }
            
            if (latestPunishment != null) {
                long expire = latestPunishment.getDate() + previousOffense.getLength();
                if (System.currentTimeMillis() > expire) {
                    return new SimpleEntry<>(latestPunishment.getOffenseNumber(), latestPunishment.getOffenseNumber());
                }
            }
        }
        if (offense > rule.getOffenses().size()) {
            return new SimpleEntry<>(rule.getOffenses().size(), offense);
        }
        return new SimpleEntry<>(punishments.size() + 1, punishments.size() + 1);
    }
}