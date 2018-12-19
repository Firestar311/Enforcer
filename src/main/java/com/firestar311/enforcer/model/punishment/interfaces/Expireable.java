package com.firestar311.enforcer.model.punishment.interfaces;

public interface Expireable {
    
    long getExpireDate();
    boolean isExpired();
    String formatExpireTime();
    void onExpire();
    void setExpireDate(long expireDate);
}