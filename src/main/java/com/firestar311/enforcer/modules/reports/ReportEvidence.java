package com.firestar311.enforcer.modules.reports;

import com.firestar311.enforcer.modules.reports.enums.EvidenceType;

import java.util.*;

public class ReportEvidence {
    
    private int id;
    private UUID submitter;
    private String link;
    private EvidenceType type;
    
    public ReportEvidence(int id, UUID submitter, EvidenceType type, String link) {
        this.id = id;
        this.submitter = submitter;
        this.link = link;
        this.type = type;
    }
    
    public ReportEvidence(Map<String, Object> serialized) {
        if (serialized.containsKey("id")) {
            this.id = (int) serialized.get("id");
        }
        if (serialized.containsKey("submitter")) {
            this.submitter = UUID.fromString((String) serialized.get("submitter"));
        }
        if (serialized.containsKey("link")) {
            this.link = (String) serialized.get("link");
        }
        if (serialized.containsKey("type")) {
            this.type = EvidenceType.valueOf((String) serialized.get("type"));
        }
    }
    
    public int getId() {
        return id;
    }
    
    public UUID getSubmitter() {
        return submitter;
    }
    
    public String getLink() {
        return link;
    }
    
    public EvidenceType getType() {
        return type;
    }
    
    public Map<String, Object> serialze() {
        Map<String, Object> serialized = new HashMap<>();
        serialized.put("id", this.id);
        serialized.put("submitter", submitter.toString());
        serialized.put("link", link);
        serialized.put("type", type.name());
        return serialized;
    }
}