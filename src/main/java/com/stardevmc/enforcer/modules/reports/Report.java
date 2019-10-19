package com.stardevmc.enforcer.modules.reports;

import com.stardevmc.enforcer.modules.punishments.actor.Actor;
import com.stardevmc.enforcer.modules.punishments.target.Target;
import com.stardevmc.enforcer.modules.punishments.type.abstraction.Punishment;
import com.stardevmc.enforcer.modules.reports.enums.ReportOutcome;
import com.stardevmc.enforcer.modules.reports.enums.ReportStatus;
import com.stardevmc.enforcer.util.evidence.Evidence;
import com.firestar311.lib.pagination.IElement;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.*;

public class Report implements IElement, Comparable<Report>, ConfigurationSerializable {
    
    private int id;
    private ReportStatus status;
    private ReportOutcome outcome;
    private List<Integer> punishments;
    private Evidence evidence;
    private Location location;
    private long date;
    private String reason;
    private Actor reporter, assignee;
    private Target target;
    
    public Map<String, Object> serialize() {
        Map<String, Object> serialized = new HashMap<>();
        serialized.put("id", id + "");
        serialized.put("status", this.status.name());
        serialized.put("outcome", this.outcome.name());
        serialized.put("punishments", StringUtils.join(this.punishments, ","));
        serialized.put("evidence", evidence);
        serialized.put("location", this.location);
        serialized.put("date", this.date + "");
        serialized.put("reason", this.reason);
        serialized.put("reporter", this.reporter);
        serialized.put("assignee", this.assignee);
        serialized.put("target", this.target);
        return serialized;
    }
    
    public static Report deserialize(Map<String, Object> serialized) {
        int id = Integer.parseInt((String) serialized.get("id"));
        ReportStatus status = ReportStatus.valueOf((String) serialized.get("status"));
        ReportOutcome outcome = ReportOutcome.valueOf((String) serialized.get("outcome"));
        List<Integer> punishments = new ArrayList<>();
        String[] rawPunishments = ((String) serialized.get("punishments")).split(",");
        for (String r : rawPunishments) {
            punishments.add(Integer.parseInt(r));
        }
        Evidence evidence = (Evidence) serialized.get("evidence");
        Location location = (Location) serialized.get("location");
        long date = Long.parseLong((String) serialized.get("date"));
        String reason = (String) serialized.get("reason");
        Actor reporter = (Actor) serialized.get("reporter");
        Actor assignee = (Actor) serialized.get("assignee");
        Target target = (Target) serialized.get("target");
        return new Report(id, status, outcome, punishments, evidence, location, date, reason, reporter, assignee, target);
    }
    
    public Report(int id, ReportStatus status, ReportOutcome outcome, List<Integer> punishments, Evidence evidence, Location location, long date, String reason, Actor reporter, Actor assignee, Target target) {
        this.id = id;
        this.status = status;
        this.outcome = outcome;
        this.punishments = punishments;
        this.evidence = evidence;
        this.location = location;
        this.date = date;
        this.reason = reason;
        this.reporter = reporter;
        this.assignee = assignee;
        this.target = target;
    }
    
    public Report(Actor reporter, Target target, Location location, String reason) {
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
        String reporterName = reporter.getName();
        String targetName = target.getName();
        return "&8 - &2<" + this.id + "> " + this.status.getColor() + "(" + this.status.name() + ") "
                + this.outcome.getColor() + "[" + this.outcome.name() + "] "
                + "&b" + reporterName + " &7-> &3" + targetName + ": &9" + this.reason;
    }
    
    public int getId() {
        return id;
    }
    
    public Actor getReporter() {
        return reporter;
    }
    
    public Target getTarget() {
        return target;
    }
    
    public Actor getAssignee() {
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
    
    public Evidence getEvidence() {
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
    
    public Report setReporter(Actor reporter) {
        this.reporter = reporter;
        return this;
    }
    
    public Report setTarget(Target target) {
        this.target = target;
        return this;
    }
    
    public Report setAssignee(Actor assignee) {
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
    
    public Report setEvidence(Evidence evidence) {
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
    
    public void addPunishment(Punishment punishment) {
        this.punishments.add(punishment.getId());
    }
    
    public int compareTo(Report o) {
        return Integer.compare(this.id, o.id);
    }
}