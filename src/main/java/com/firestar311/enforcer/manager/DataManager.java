package com.firestar311.enforcer.manager;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.data.PunishmentDatabase;
import com.firestar311.enforcer.model.Prison;
import com.firestar311.enforcer.model.enums.*;
import com.firestar311.enforcer.model.note.Note;
import com.firestar311.enforcer.model.punishment.abstraction.*;
import com.firestar311.enforcer.model.punishment.interfaces.Expireable;
import com.firestar311.enforcer.model.punishment.type.*;
import com.firestar311.enforcer.model.rule.*;
import com.firestar311.lib.FireLib;
import com.firestar311.lib.player.PlayerInfo;
import com.firestar311.lib.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class DataManager {
    
    private Enforcer plugin;
    
    private PunishmentDatabase database;
    
    private File jailsFile, notesFile, rulesFile, punishmentsFile;
    private FileConfiguration jailsConfig, notesConfig, rulesConfig, punishmentsConfig;
    
    private Map<Integer, Punishment> punishments = new TreeMap<>();
    
    private SortedMap<Integer, Rule> rules = new TreeMap<>();
    private Map<Integer, Note> notes = new TreeMap<>();
    
    private Map<Integer, String> ackCodes = new TreeMap<>();
    private Map<UUID, String> jailedInventories = new HashMap<>();
    private Set<UUID> unjailedWhileOffline = new HashSet<>();
    private Map<Integer, Prison> prisons = new TreeMap<>();
    
    private boolean usingDisplayNames = false, trainingMode = false;
    
    private String storageType = "";
    
    private String prefix;
    private String serverName;
    
    private final char[] CODE_CHARS = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    private final int CODE_AMOUNT = 6;
    
    public DataManager(Enforcer plugin) {
        this.plugin = plugin;
        if (plugin.getConfig().getString("storage-type").equalsIgnoreCase("yaml")) {
            storageType = "yaml";
            jailsFile = new File(plugin.getDataFolder(), plugin.getConfig().getString("storage-options.yaml.jails-file"));
            notesFile = new File(plugin.getDataFolder(), plugin.getConfig().getString("storage-options.yaml.notes-file"));
            rulesFile = new File(plugin.getDataFolder(), plugin.getConfig().getString("storage-options.yaml.rules-file"));
            punishmentsFile = new File(plugin.getDataFolder(), plugin.getConfig().getString("storage-options.yaml.punishments-file"));
            createFiles(jailsFile, notesFile, rulesFile, punishmentsFile);
            jailsConfig = YamlConfiguration.loadConfiguration(jailsFile);
            notesConfig = YamlConfiguration.loadConfiguration(notesFile);
            rulesConfig = YamlConfiguration.loadConfiguration(rulesFile);
            punishmentsConfig = YamlConfiguration.loadConfiguration(punishmentsFile);
            
            if (!rulesConfig.contains("rules")) {
                plugin.saveResource("rules.yml", true);
                rulesConfig = YamlConfiguration.loadConfiguration(rulesFile);
            }
        } else if (plugin.getConfig().getString("storage-type").equalsIgnoreCase("mysql")) {
            this.storageType = "mysql";
            database = new PunishmentDatabase(plugin.getConfig().getString("storage-options.mysql.connection.user"), plugin.getConfig().getString("storage-options.mysql.connection.database"), plugin.getConfig().getString("storage-options.mysql.connection.password"), plugin.getConfig().getInt("storage-options.mysql.connection.port"), plugin.getConfig().getString("storage-options.mysql.connection.hostname"));
            database.openConnection();
            database.createTables();
        } else {
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
        
        prefix = plugin.getConfig().getString("prefix").toUpperCase();
        serverName = plugin.getConfig().getString("server");
    }
    
    public void saveData() {
        if (storageType.equalsIgnoreCase("yaml")) {
            try {
                this.saveJailDataToYaml();
                jailsConfig.save(jailsFile);
                
                notesConfig.save(notesFile);
                
                this.saveRuleDataToYaml();
                rulesConfig.save(rulesFile);
                
                this.savePunishmentData();
                punishmentsConfig.save(punishmentsFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (storageType.equalsIgnoreCase("mysql")) {
            database.closeConnection();
        }
    }
    
    public void loadData() {
        if (storageType.equalsIgnoreCase("yaml")) {
            plugin.getLogger().info("Loading Rule data....");
            this.loadRuleDataFromYaml();
            plugin.getLogger().info("Rule loading finished");
            plugin.getLogger().info("Loading Punishment data....");
            this.loadPunishmentData();
            plugin.getLogger().info("Punishment loading finished");
            plugin.getLogger().info("Loading prison and jail data....");
            this.loadJailDataFromYaml();
            plugin.getLogger().info("Prison and jail data loading finished.");
        }
    }
    
    private void savePunishmentData() {
        for (Punishment punishment : punishments.values()) {
            Map<String, Object> serialized = Punishment.serialize(punishment);
            for (Entry<String, Object> entry : serialized.entrySet()) {
                punishmentsConfig.set("punishments." + punishment.getId() + "." + entry.getKey(), entry.getValue());
            }
        }
    }
    
    private void loadPunishmentData() {
        if (!punishmentsConfig.contains("punishments")) {
            plugin.getLogger().info("Could not find any punishments to load.");
            return;
        }
        
        for (String pi : punishmentsConfig.getConfigurationSection("punishments").getKeys(false)) {
            Map<String, Object> serialized = new HashMap<>();
            for (String key : punishmentsConfig.getConfigurationSection("punishments." + pi).getKeys(false)) {
                serialized.put(key, punishmentsConfig.get("punishments." + pi + "." + key));
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
        
        if (trainingMode) {
            punishment.setTrainingMode(true);
        }
        
        plugin.getReportManager().getReports().values().stream().filter(report -> report.getTarget().equals(punishment.getTarget())).filter(report -> report.getReason().equalsIgnoreCase(punishment.getReason())).forEach(report -> {
            report.addPunishment(punishment);
            report.setOutcome(ReportOutcome.ACCEPTED);
            report.setStatus(ReportStatus.CLOSED);
        });
        
        this.punishments.put(punishment.getId(), punishment);
    }
    
    public Punishment getPunishment(int id) {
        return this.punishments.get(id);
    }
    
    private void saveJailDataToYaml() {
        jailsConfig.set("inventories", null);
        for (Entry<UUID, String> entry : this.jailedInventories.entrySet()) {
            jailsConfig.set("inventories." + entry.getKey().toString(), entry.getValue());
        }
        
        List<String> unjailed = new ArrayList<>();
        unjailedWhileOffline.forEach(uuid -> unjailed.add(uuid.toString()));
        jailsConfig.set("unjailed", unjailed);
        
        jailsConfig.set("jaillocations", null);
        for (Entry<Integer, Prison> entry : this.prisons.entrySet()) {
            Integer id = entry.getKey();
            Prison prison = entry.getValue();
            jailsConfig.set("jaillocations." + id, prison.serialize());
        }
    }
    
    private void loadJailDataFromYaml() {
        if (this.jailsConfig.contains("jaillocations")) {
            for (String i : this.jailsConfig.getConfigurationSection("jaillocations").getKeys(false)) {
                int id = Integer.parseInt(i);
                Prison prison = Prison.deserialize(this.jailsConfig.getString("jaillocations." + i));
                this.prisons.put(id, prison);
            }
        }
        
        if (!this.jailsConfig.contains("inventories")) return;
        for (String u : jailsConfig.getConfigurationSection("inventories").getKeys(false)) {
            UUID uuid = UUID.fromString(u);
            String inv = jailsConfig.getString("inventories." + u);
            this.jailedInventories.put(uuid, inv);
        }
        
        if (!this.jailsConfig.contains("unjailed")) return;
        List<String> unjailed = this.jailsConfig.getStringList("unjailed");
        unjailed.forEach(u -> this.unjailedWhileOffline.add(UUID.fromString(u)));
    }
    
    private void loadRuleDataFromYaml() {
        if (this.rulesConfig.getConfigurationSection("rules") == null) return;
        for (String r : this.rulesConfig.getConfigurationSection("rules").getKeys(false)) {
            Rule rule = new Rule(rulesConfig.getInt("rules." + r + ".id"), r, rulesConfig.getString("rules." + r + ".name"), rulesConfig.getString("rules." + r + ".description"));
            if (rulesConfig.contains("rules." + r + ".material")) {
                rule.setMaterial(Material.valueOf(rulesConfig.getString("rules." + r + ".material").toUpperCase()));
            }
            if (this.rulesConfig.contains("rules." + r + ".offenses")) {
                for (String o : this.rulesConfig.getConfigurationSection("rules." + r + ".offenses").getKeys(false)) {
                    int offenseNumber = Integer.parseInt(o);
                    RuleOffense action = new RuleOffense(offenseNumber);
                    for (String a : this.rulesConfig.getConfigurationSection("rules." + r + ".offenses." + o + ".actions").getKeys(false)) {
                        int aN = Integer.parseInt(a);
                        PunishmentType type = PunishmentType.getType(rulesConfig.getString("rules." + r + ".offenses." + o + ".actions." + a + ".punishment").toUpperCase());
                        int rawLength = -1;
                        String units = "";
                        int id = -1;
                        if (rulesConfig.contains("rules." + r + ".offenses." + o + ".actions." + a + ".length")) {
                            rawLength = rulesConfig.getInt("rules." + r + ".offenses." + o + ".actions." + a + ".length");
                        }
                        
                        if (rulesConfig.contains("rules." + r + ".offenses." + o + ".actions." + a + ".unit")) {
                            units = rulesConfig.getString("rules." + r + ".offenses." + o + ".actions." + a + ".unit");
                        }
                        
                        if (rulesConfig.contains("rules." + r + ".offenses." + o + ".actions." + a + ".id")) {
                            id = rulesConfig.getInt("rules." + r + ".offenses." + o + ".actions." + a + ".id");
                        }
                        
                        long length = Enforcer.convertTime(units, rawLength);
                        
                        RulePunishment punishment = new RulePunishment(type, length, rawLength, units);
                        punishment.setId(id);
                        action.addPunishment(aN, punishment);
                    }
                    //plugin.getLogger().info("Loaded " + action.getPunishments().size() + " punishment action(s) for offense number " + offenseNumber + " for the rule " + rule.getName());
                    rule.addOffense(offenseNumber, action);
                }
            }
            //plugin.getLogger().info("Loaded " + rule.getOffenses().size() + " offense(s) for the rule " + rule.getName());
            this.addRule(rule);
        }
        //plugin.getLogger().info("Loaded " + this.rules.size() + " rules");
    }
    
    private void saveRuleDataToYaml() {
        this.rulesConfig.set("rules", null);
        
        for (Rule rule : this.rules.values()) {
            String basePath = "rules." + rule.getInternalId();
            rulesConfig.set(basePath + ".id", rule.getId());
            rulesConfig.set(basePath + ".name", rule.getName());
            rulesConfig.set(basePath + ".description", rule.getDescription());
            if (rule.getMaterial() != null) {
                rulesConfig.set(basePath + ".material", rule.getMaterial().name());
            }
            
            if (rule.getOffenses().isEmpty()) continue;
            
            for (Entry<Integer, RuleOffense> actionEntry : rule.getOffenses().entrySet()) {
                String actionBase = basePath + ".offenses." + actionEntry.getKey() + ".actions";
                for (Entry<Integer, RulePunishment> punishmentEntry : actionEntry.getValue().getPunishments().entrySet()) {
                    String punishmentBase = actionBase + "." + punishmentEntry.getKey();
                    rulesConfig.set(punishmentBase + ".punishment", punishmentEntry.getValue().getType().toString().toLowerCase());
                    rulesConfig.set(punishmentBase + ".length", punishmentEntry.getValue().getcLength());
                    rulesConfig.set(punishmentBase + ".unit", punishmentEntry.getValue().getcUnits());
                    rulesConfig.set(punishmentBase + ".id", punishmentEntry.getValue().getId());
                }
            }
        }
    }
    
    private void createFiles(File... files) {
        if (files != null) {
            for (File file : files) {
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
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
                return isTrainingMode();
            } else {
                return true;
            }
        }
        return false;
    }
    
    public Set<Punishment> getBans(UUID uuid) {
        return punishments.values().stream().filter(punishment -> punishment instanceof BanPunishment).filter(punishment -> punishment.getTarget().equals(uuid)).collect(Collectors.toSet());
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
        return punishments.values().stream().filter(punishment -> punishment instanceof MutePunishment).filter(punishment -> punishment.getTarget().equals(uuid)).collect(Collectors.toSet());
    }
    
    public void addWarning(WarnPunishment punishment) {
        addPunishment(punishment);
    }
    
    public boolean hasBeenWarned(UUID uuid) {
        for (Punishment punishment : getWarnings(uuid)) {
            if (punishment.isActive()) {
                if (punishment.isTrainingPunishment()) {
                    return isTrainingMode();
                } else {
                    return true;
                }
            }
        }
        return false;
    }
    
    public Set<Punishment> getWarnings(UUID uuid) {
        return punishments.values().stream().filter(punishment -> punishment instanceof WarnPunishment).filter(punishment -> punishment.getTarget().equals(uuid)).collect(Collectors.toSet());
    }
    
    public void addKick(KickPunishment punishment) {
        addPunishment(punishment);
    }
    
    public boolean hasBeenKicked(UUID uuid) {
        for (Punishment punishment : getKicks(uuid)) {
            if (punishment.isActive()) {
                if (punishment.isTrainingPunishment()) {
                    return isTrainingMode();
                } else {
                    return true;
                }
            }
        }
        return false;
    }
    
    public Set<Punishment> getKicks(UUID uuid) {
        return punishments.values().stream().filter(punishment -> punishment instanceof KickPunishment).filter(punishment -> punishment.getTarget().equals(uuid)).collect(Collectors.toSet());
    }
    
    public void addJailPunishment(JailPunishment punishment) {
        addPunishment(punishment);
        if (punishment.isActive()) {
            try {
                Prison prison = findPrison();
                punishment.setJailId(prison.getId());
            } catch (Exception e) {
                plugin.getLogger().severe("Could not find a prison for an active jail punishment with id " + punishment.getId());
            }
        }
    }
    
    public Prison findPrison() {
        Prison prison = null;
        boolean allPrisonsFull = true;
        for (Prison j : this.prisons.values()) {
            if (!j.isFull()) {
                allPrisonsFull = false;
                prison = j;
                break;
            }
        }
        if (allPrisonsFull) {
            for (Prison j : this.prisons.values()) {
                if (prison == null) {
                    prison = j;
                } else {
                    int amountOver1 = prison.getInhabitants().size() - prison.getMaxPlayers();
                    int amountOver2 = j.getInhabitants().size() - j.getMaxPlayers();
                    if (amountOver2 < amountOver1) {
                        prison = j;
                    }
                }
            }
        }
        return prison;
    }
    
    public boolean isJailed(UUID uuid) {
        for (Punishment punishment : getJailPunishments(uuid)) {
            if (punishment.isActive()) {
                if (punishment.isTrainingPunishment()) {
                    return isTrainingMode();
                } else {
                    return true;
                }
            }
        }
        return false;
    }
    
    public Set<Punishment> getJailPunishments(UUID uuid) {
        return punishments.values().stream().filter(punishment -> punishment instanceof JailPunishment).filter(punishment -> punishment.getTarget().equals(uuid)).collect(Collectors.toSet());
    }
    
    public void addNote(Note note) {
        this.notes.put(this.notes.keySet().size(), note);
    }
    
    public Set<Note> getNotes(UUID uuid) {
        return notes.values().stream().filter(note -> note.getTarget().equals(uuid)).collect(Collectors.toSet());
    }
    
    public PlayerInfo getInfo(UUID uuid) {
        FireLib fireLib = (FireLib) Bukkit.getPluginManager().getPlugin("FireLib");
        return fireLib.getPlayerManager().getPlayerInfo(uuid);
    }
    
    public void addRule(Rule rule) {
        if (rule.getId() == -1) {
            int lastId = this.rules.lastKey();
            rule.setId(lastId + 1);
        }
        
        this.rules.put(rule.getId(), rule);
    }
    
    public void addAckCode(int id, String code) {
        this.ackCodes.put(id, code);
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
    
    public String generateAckCode(int id) {
        StringBuilder codeBuilder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < CODE_AMOUNT; i++) {
            char c = CODE_CHARS[random.nextInt(CODE_CHARS.length - 1)];
            if (random.nextInt(100) < 50) {
                c = Character.toUpperCase(c);
            }
            codeBuilder.append(c);
        }
        
        String code = codeBuilder.toString();
        this.ackCodes.put(id, code);
        return code;
    }
    
    public void addInvString(UUID uuid, String string) {
        this.jailedInventories.put(uuid, string);
    }
    
    public void removeInvString(UUID uuid) {
        this.jailedInventories.remove(uuid);
    }
    
    public String getJailedInv(UUID uuid) {
        return this.jailedInventories.get(uuid);
    }
    
    public void addUnjailedWhileOffline(UUID uuid) {
        this.unjailedWhileOffline.add(uuid);
    }
    
    public void removeUnjailedWhileOffline(UUID uuid) {
        this.unjailedWhileOffline.remove(uuid);
    }
    
    public boolean wasUnjailedWhileOffline(UUID uuid) {
        return this.unjailedWhileOffline.contains(uuid);
    }
    
    public Set<Punishment> getActivePunishments(UUID uuid) {
        Set<Punishment> punishments = new HashSet<>();
        punishments.addAll(getActiveBans(uuid));
        punishments.addAll(getActiveMutes(uuid));
        punishments.addAll(getActiveJails(uuid));
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
    
    public PlayerInfo getInfo(String name) {
        if (name == null) return null;
        FireLib fireLib = (FireLib) Bukkit.getPluginManager().getPlugin("FireLib");
        return fireLib.getPlayerManager().getPlayerInfo(name);
    }
    
    public Set<BanPunishment> getActiveBans() {
        Set<BanPunishment> bans = new HashSet<>();
        for (Punishment punishment : this.punishments.values().stream().filter(punishment -> punishment instanceof BanPunishment).collect(Collectors.toSet())) {
            if (punishment.isActive()) {
                bans.add((BanPunishment) punishment);
            }
        }
        
        return bans;
    }
    
    public Set<MutePunishment> getActiveMutes() {
        Set<MutePunishment> mutes = new HashSet<>();
        for (Punishment punishment : this.punishments.values().stream().filter(punishment -> punishment instanceof MutePunishment).collect(Collectors.toSet())) {
            if (punishment.isActive()) {
                mutes.add((MutePunishment) punishment);
            }
        }
        
        return mutes;
    }
    
    public Set<JailPunishment> getActiveJails() {
        Set<JailPunishment> jails = new HashSet<>();
        for (Punishment punishment : this.punishments.values().stream().filter(punishment -> punishment instanceof JailPunishment).collect(Collectors.toSet())) {
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
    
    public Prison getPrison(int id) {
        return this.prisons.get(id);
    }
    
    public Set<Prison> getPrisons() {
        return new HashSet<>(this.prisons.values());
    }
    
    public void addPrison(Prison prison) {
        if (prison.getId() == -1) {
            prison.setId(this.prisons.size());
        }
        this.prisons.put(prison.getId(), prison);
    }
    
    public Prison getPrison(UUID uuid) {
        for (Prison prison : this.prisons.values()) {
            if (prison.isInhabitant(uuid)) {
                return prison;
            }
        }
        return null;
    }
    
    public void removePrison(int id) {
        this.prisons.remove(id);
    }
    
    public boolean isUsingDisplayNames() {
        return usingDisplayNames;
    }
    
    public void setUsingDisplayNames(boolean usingDisplayNames) {
        this.usingDisplayNames = usingDisplayNames;
    }
    
    public Prison getPrisonFromString(Player player, String i) {
        int jailId = -1;
        try {
            jailId = Integer.parseInt(i);
        } catch (NumberFormatException e) {
            for (Prison prison : plugin.getDataManager().getPrisons()) {
                if (prison.getName() != null) {
                    if (prison.getName().equalsIgnoreCase(i)) {
                        jailId = prison.getId();
                    }
                }
            }
            if (jailId == -1) {
                player.sendMessage(Utils.color("&cA prison could not be found with that name or id."));
                return null;
            }
        }
        
        Prison prison = plugin.getDataManager().getPrison(jailId);
        if (prison == null) {
            player.sendMessage(Utils.color("&cA prison could not be found with that id."));
            return null;
        }
        return prison;
    }
    
    public Set<Punishment> getPunishments() {
        return new HashSet<>(punishments.values());
    }
    
    public boolean isTrainingMode() {
        return trainingMode;
    }
    
    public void setTrainingMode(boolean trainingMode) {
        this.trainingMode = trainingMode;
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    public String getServerName() {
        return serverName;
    }
    
    public Set<Prison> getPrisonsWithOverflow() {
        Set<Prison> prisons = new HashSet<>();
        for (Prison prison : getPrisons()) {
            if (prison.getInhabitants().size() > prison.getMaxPlayers()) {
                prisons.add(prison);
            }
        }
        
        return prisons;
    }
    
    public Set<Rule> getRules() {
        return new TreeSet<>(this.rules.values());
    }
    
    public Rule getRule(String ruleString) {
        Rule rule = null;
        try {
            int id = Integer.parseInt(ruleString);
            rule = this.rules.get(id);
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
    
    public void removeRule(int id) {
        this.rules.remove(id);
    }
    
    public Set<Punishment> getPunishmentsByRule(UUID target, Rule rule, boolean trainingMode) {
        Set<Punishment> punishments = new HashSet<>();
        
        for (Punishment punishment : getPunishments()) {
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
    
    public Entry<Integer, Integer> getNextOffense(UUID target, Rule rule) {
        Set<Punishment> punishments = getPunishmentsByRule(target, rule, this.trainingMode);
        if (punishments.isEmpty()) return new SimpleEntry<>(1, 1);
        else {
            int offense = punishments.size() + 1;
            if (offense > rule.getOffenses().size()) {
                return new SimpleEntry<>(rule.getOffenses().size(), offense);
            }
            return new SimpleEntry<>(punishments.size() + 1, punishments.size() + 1);
        }
    }
}