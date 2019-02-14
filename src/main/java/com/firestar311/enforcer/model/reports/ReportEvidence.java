package com.firestar311.enforcer.model.reports;

import java.util.*;

public class ReportEvidence {

    private int id;
    private UUID submitter;
    private String link;
    
    public ReportEvidence(int id, UUID submitter, String link) {
        this.id = id;
        this.submitter = submitter;
        this.link = link;
    }
    
    public ReportEvidence(Map<String, Object> serialized) {
    
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
    
    public Map<String, Object> serialze() {
        Map<String, Object> serialized = new HashMap<>();
        serialized.put("id", this.id);
        serialized.put("submitter", submitter.toString());
        serialized.put("link", link);
        return serialized;
    }
}