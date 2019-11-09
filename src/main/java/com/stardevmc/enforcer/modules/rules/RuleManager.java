package com.stardevmc.enforcer.modules.rules;

import com.stardevmc.enforcer.Enforcer;
import com.stardevmc.enforcer.modules.base.Manager;
import com.stardevmc.enforcer.modules.punishments.type.abstraction.Punishment;
import com.stardevmc.enforcer.modules.rules.rule.Rule;
import com.stardevmc.enforcer.modules.rules.rule.RuleOffense;
import com.stardevmc.enforcer.util.EnforcerUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
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
        
        for (Entry<Integer, Rule> entry : this.rules.entrySet()) {
            config.set("rules." + entry.getValue().getInternalId(), entry.getValue());
        }
        
        this.configManager.saveConfig();
    }
    
    public void loadData() {
        FileConfiguration config = configManager.getConfig();
        ConfigurationSection rulesSection = config.getConfigurationSection("rules");
        if (rulesSection == null) { return; }
        for (String r : rulesSection.getKeys(false)) {
            try {
                Rule rule = (Rule) config.get(r);
                if (rule != null) { this.rules.put(rule.getId(), rule); }
            } catch (Exception e) {}
        }
        
        if (this.rules.isEmpty()) {
            this.rules.putAll(EnforcerUtils.getOldRules(config));
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
    
    public Set<Rule> getRules() {
        return new TreeSet<>(this.rules.values());
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