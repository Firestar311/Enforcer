package com.firestar311.enforcer.model.punishment.interfaces;

public interface Acknowledgeable {
    
    boolean isAcknowledged();
    void setAcknowledged(boolean value);
    void onAcknowledge();
}