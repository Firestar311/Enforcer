package com.firestar311.enforcer.model.enums;

public enum Visibility {
    NORMAL(""), PUBLIC("&9[PUBLIC] "), SILENT("&9[SILENT] ");
    
    private String prefix;
    Visibility(String prefix) {
        this.prefix = prefix;
    }
    
    public String getPrefix() {
        return prefix;
    }
}