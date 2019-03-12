package com.firestar311.enforcer.model.enums;

public enum ReportStatus {
    
    OPEN("&a"), CLOSED("&c"), INVESTIGATING("&6"), CANCELLED("&4");
    
    private String color;
    
    ReportStatus(String color) {
        this.color = color;
    }
    
    public String getColor() {
        return color;
    }
}