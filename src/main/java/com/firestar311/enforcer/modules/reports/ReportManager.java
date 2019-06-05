package com.firestar311.enforcer.modules.reports;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.modules.base.Manager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.Map.Entry;

public class ReportManager extends Manager implements Listener {

    private SortedMap<Integer, Report> reports = new TreeMap<>();
    
    public ReportManager(Enforcer plugin) {
        super(plugin, "reports");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public void saveData() {
        FileConfiguration reportsConfig = this.configManager.getConfig();
        for (Entry<Integer, Report> entry : reports.entrySet()) {
             for (Entry<String, Object> serializedEntry : entry.getValue().serialize().entrySet()) {
                 reportsConfig.set("reports." + entry.getKey() + "." + serializedEntry.getKey(), serializedEntry.getValue());
             }
        }
    
        this.configManager.saveConfig();
    }
    
    public void loadData() {
        FileConfiguration reportsConfig = this.configManager.getConfig();
        ConfigurationSection reportsSection = reportsConfig.getConfigurationSection("reports");
        if (reportsSection == null) {
            return;
        }
        
        for (String k : reportsSection.getKeys(false)) {
            Map<String, Object> serialized = new HashMap<>();
            for (String sK : reportsConfig.getConfigurationSection("reports." + k).getKeys(false)) {
                serialized.put(sK, reportsConfig.get("reports." + k + "." + sK));
            }
            
            if (!serialized.isEmpty()) {
                Report report = new Report(serialized);
                this.reports.put(report.getId(), report);
            } else {
                plugin.getLogger().severe("Invalid report loaded");
            }
        }
    }
    
    public void addReport(Report report) {
        if (report.getId() == -1) {
            int id = this.reports.isEmpty() ? 0 : this.reports.lastKey() + 1;
            report.setId(id);
            this.reports.put(id, report);
        } else {
            this.reports.put(report.getId(), report);
        }
    }
    
    public SortedMap<Integer, Report> getReports() {
        return new TreeMap<>(reports);
    }
    
    public Report getReport(int id) {
        return this.reports.get(id);
    }
    
    public List<Report> getReportsByReporter(UUID uuid) {
        List<Report> reports = new ArrayList<>();
        for (Report report : this.reports.values()) {
            if (report.getReporter().equals(uuid)) {
                reports.add(report);
            }
        }
        
        return reports;
    }
}