package com.firestar311.enforcer.modules.punishments.type.interfaces;

public interface Expireable {
    
    long getExpireDate();
    boolean isExpired();
    String formatExpireTime();
    void onExpire();
    void setExpireDate(long expireDate);
}