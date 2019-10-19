package com.stardevmc.enforcer.modules.reports;

import com.stardevmc.enforcer.Enforcer;
import com.stardevmc.enforcer.modules.base.Manager;
import com.stardevmc.enforcer.modules.punishments.actor.PlayerActor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.Map.Entry;

public class ReportManager extends Manager {

    private SortedMap<Integer, Report> reports = new TreeMap<>();
    
    public ReportManager(Enforcer plugin) {
        super(plugin, "reports");
    }
    
    public void saveData() {
        FileConfiguration reportsConfig = this.configManager.getConfig();
        for (Entry<Integer, Report> entry : reports.entrySet()) {
             reportsConfig.set("reports." + entry.getKey(), entry.getValue());
        }
    
        this.configManager.saveConfig();
    }
    
    public void loadData() {
        FileConfiguration reportsConfig = this.configManager.getConfig();
        ConfigurationSection reportsSection = reportsConfig.getConfigurationSection("reports");
        if (reportsSection == null) { return; }
        for (String r : reportsSection.getKeys(false)) {
            Report report = (Report) reportsSection.get(r);
            this.reports.put(report.getId(), report);
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
            if (report.getReporter() instanceof PlayerActor) {
                if (((PlayerActor) report.getReporter()).getUniqueId().equals(uuid)) {
                    reports.add(report);
                }
            }
        }
        
        return reports;
    }
}