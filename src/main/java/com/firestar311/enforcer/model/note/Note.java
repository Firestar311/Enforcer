package com.firestar311.enforcer.model.note;

import java.util.UUID;

public class Note {
    
    private UUID target, noter, remover;
    private long date, removedDate;
    private String message;
    private boolean removed = false;
    
    public Note(UUID target, UUID noter, long date, String message) {
        this.target = target;
        this.noter = noter;
        this.date = date;
        this.message = message;
    }
    
    public UUID getTarget() {
        return target;
    }
    
    public UUID getNoter() {
        return noter;
    }
    
    public long getDate() {
        return date;
    }
    
    public long getRemovedDate() {
        return removedDate;
    }
    
    public String getMessage() {
        return message;
    }
    
    public boolean isRemoved() {
        return removed;
    }
    
    public void setRemovedDate(long removedDate) {
        this.removedDate = removedDate;
    }
    
    public void setRemoved(boolean removed) {
        this.removed = removed;
    }
    
    public UUID getRemover() {
        return remover;
    }
    
    public void setRemover(UUID remover) {
        this.remover = remover;
    }
}