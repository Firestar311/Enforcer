package com.firestar311.enforcer.manager;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.model.reports.Report;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class ReportManager implements Listener {

    private Enforcer plugin;
    
    private File reportsFile;
    private FileConfiguration reportsConfig;
    
    private SortedMap<Integer, Report> reports = new TreeMap<>();
    
    public ReportManager(Enforcer plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        reportsFile = new File(plugin.getDataFolder() +  File.separator + "reports.yml");
        if (!reportsFile.exists()) {
            try {
                reportsFile.createNewFile();
                reportsConfig = YamlConfiguration.loadConfiguration(reportsFile);
            } catch (Exception e) {
                plugin.getLogger().severe("Could not create reports.yml file.");
            }
        } else {
            reportsConfig = YamlConfiguration.loadConfiguration(reportsFile);
        }
    }
    
    public void saveReports() {
        for (Entry<Integer, Report> entry : reports.entrySet()) {
             for (Entry<String, Object> serializedEntry : entry.getValue().serialize().entrySet()) {
                 reportsConfig.set("reports." + entry.getKey() + "." + serializedEntry.getKey(), serializedEntry.getValue());
             }
        }
    
        try {
            reportsConfig.save(reportsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save the reports.yml file!");
        }
    }
    
    public void loadReports() {
        ConfigurationSection reportsSection = this.reportsConfig.getConfigurationSection("reports");
        if (reportsSection == null) {
            plugin.getLogger().info("No reports were found that need to be loaded.");
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
        plugin.getLogger().info("Report data loading finished.");
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