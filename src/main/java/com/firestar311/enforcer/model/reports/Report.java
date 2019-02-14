package com.firestar311.enforcer.model.reports;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.model.enums.ReportOutcome;
import com.firestar311.enforcer.model.enums.ReportStatus;
import com.firestar311.lib.pagination.Paginatable;
import com.firestar311.lib.util.Utils;
import org.bukkit.Location;

import java.util.*;

public class Report implements Paginatable, Comparable<Report> {
    
    private int id;
    private UUID reporter, target, assignee;
    private ReportStatus status;
    private ReportOutcome outcome;
    private List<Integer> punishments;
    private ReportEvidence evidence;
    private Location location;
    private long date;
    private String reason;
    
    public Report(Map<String, Object> serialized) {
        if (serialized.containsKey("id")) {
            this.id = (int) serialized.get("id");
        } else if (serialized.containsKey("reporter")) {
            this.reporter = UUID.fromString((String) serialized.get("reporter"));
        } else if (serialized.containsKey("target")) {
            this.target = UUID.fromString((String) serialized.get("target"));
        } else if (serialized.containsKey("assignee")) {
            this.assignee = UUID.fromString((String) serialized.get("assignee"));
        } else if (serialized.containsKey("status")) {
            this.status = ReportStatus.valueOf((String) serialized.get("status"));
        } else if (serialized.containsKey("outcome")) {
            this.outcome = ReportOutcome.valueOf((String) serialized.get("outcome"));
        } else if (serialized.containsKey("punishments")) {
            this.punishments = (List<Integer>) serialized.get("punishments");
        } else if (serialized.containsKey("evidence")) {
            this.evidence = new ReportEvidence((Map<String, Object>) serialized.get("evidence"));
        } else if (serialized.containsKey("location")) {
            this.location = Utils.getLocationFromString((String) serialized.get("location"));
        } else if (serialized.containsKey("date")) {
            this.date = (long) serialized.get("date");
        } else if (serialized.containsKey("reason")) {
            this.reason = (String) serialized.get("reason");
        }
    }
    
    public Report(UUID reporter, UUID target, Location location, String reason) {
        this.reporter = reporter;
        this.target = target;
        this.location = location;
        this.id = -1;
        this.assignee = null;
        this.punishments = new ArrayList<>();
        this.date = System.currentTimeMillis();
        this.status = ReportStatus.OPEN;
        this.outcome = ReportOutcome.UNDECIDED;
        this.reason = reason;
    }
    
    public String formatLine(String... strings) {
        String reporterName, targetName;
        reporterName = Enforcer.getInstance().getDataManager().getInfo(reporter).getLastName();
        targetName = Enforcer.getInstance().getDataManager().getInfo(target).getLastName();
        return "&8 - &7" + this.id + " " + reporterName + " -> " + targetName + ": " + this.reason;
    }
    
    public int getId() {
        return id;
    }
    
    public UUID getReporter() {
        return reporter;
    }
    
    public UUID getTarget() {
        return target;
    }
    
    public UUID getAssignee() {
        return assignee;
    }
    
    public ReportStatus getStatus() {
        return status;
    }
    
    public ReportOutcome getOutcome() {
        return outcome;
    }
    
    public List<Integer> getPunishments() {
        return punishments;
    }
    
    public ReportEvidence getEvidence() {
        return evidence;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public long getDate() {
        return date;
    }
    
    public Report setId(int id) {
        this.id = id;
        return this;
    }
    
    public Report setReporter(UUID reporter) {
        this.reporter = reporter;
        return this;
    }
    
    public Report setTarget(UUID target) {
        this.target = target;
        return this;
    }
    
    public Report setAssignee(UUID assignee) {
        this.assignee = assignee;
        return this;
    }
    
    public Report setStatus(ReportStatus status) {
        this.status = status;
        return this;
    }
    
    public Report setOutcome(ReportOutcome outcome) {
        this.outcome = outcome;
        return this;
    }
    
    public Report setPunishments(List<Integer> punishments) {
        this.punishments = punishments;
        return this;
    }
    
    public Report setEvidence(ReportEvidence evidence) {
        this.evidence = evidence;
        return this;
    }
    
    public Report setLocation(Location location) {
        this.location = location;
        return this;
    }
    
    public Report setDate(long date) {
        this.date = date;
        return this;
    }
    
    public String getReason() {
        return reason;
    }
    
    public Map<String, Object> serialize() {
        Map<String, Object> serialized = new HashMap<>();
        serialized.put("id", this.id);
        serialized.put("reporter", this.reporter.toString());
        serialized.put("target", this.target.toString());
        if (assignee != null) serialized.put("assignee", this.assignee.toString());
        if (status != null) serialized.put("status", this.status.name());
        if (outcome != null) serialized.put("outcome", this.outcome.name());
        if (evidence != null) serialized.put("evidence", this.evidence.serialze());
        if (location != null) serialized.put("location", Utils.convertLocationToString(this.location));
        serialized.put("date", this.date);
        serialized.put("reason", this.reason);
        return serialized;
    }
    
    public int compareTo(Report o) {
        return Integer.compare(this.id, o.id);
    }
}