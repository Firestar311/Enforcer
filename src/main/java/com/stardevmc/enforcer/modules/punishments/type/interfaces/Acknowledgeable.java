package com.stardevmc.enforcer.modules.punishments.type.interfaces;

public interface Acknowledgeable {
    
    boolean isAcknowledged();
    void setAcknowledged(boolean value);
    void onAcknowledge();
}