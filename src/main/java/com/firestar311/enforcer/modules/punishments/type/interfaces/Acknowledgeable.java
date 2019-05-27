package com.firestar311.enforcer.modules.punishments.type.interfaces;

public interface Acknowledgeable {
    
    boolean isAcknowledged();
    void setAcknowledged(boolean value);
    void onAcknowledge();
}