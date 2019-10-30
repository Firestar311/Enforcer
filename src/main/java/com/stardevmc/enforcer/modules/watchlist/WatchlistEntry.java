package com.stardevmc.enforcer.modules.watchlist;

import com.firestar311.lib.pagination.IElement;
import com.firestar311.lib.util.Utils;
import com.stardevmc.enforcer.modules.base.Priority;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.*;

public class WatchlistEntry implements ConfigurationSerializable, IElement {
    private UUID target, staff;
    private String reason;
    private List<WatchlistNote> notes = new ArrayList<>();
    private Priority priority = Priority.NORMAL;
    
    public WatchlistEntry(UUID target, UUID staff, String reason) {
        this.target = target;
        this.staff = staff;
        this.reason = reason;
    }
    
    public Map<String, Object> serialize() {
        Map<String, Object> serialized = new HashMap<>();
        serialized.put("target", this.target.toString());
        serialized.put("staff", this.target.toString());
        serialized.put("reason", reason);
        serialized.put("priority", this.priority.toString());
        serialized.put("noteAmount", this.notes.size() + "");
        for (int i = 0; i < notes.size(); i++) {
            serialized.put("notes" + i, notes.get(i));
        }
        return serialized;
    }
    
    public static WatchlistEntry deserialize(Map<String, Object> serialized) {
        UUID target = UUID.fromString((String) serialized.get("target"));
        UUID staff = UUID.fromString((String) serialized.get("staff"));
        String reason = (String) serialized.get("reason");
        int noteAmount = Integer.parseInt((String) serialized.get("noteAmount"));
        Priority priority = Priority.valueOf((String) serialized.get("priority"));
        List<WatchlistNote> notes = new ArrayList<>();
        for (int i = 0; i < noteAmount; i++) {
            WatchlistNote note = (WatchlistNote) serialized.get("notes" + i);
            notes.add(note);
        }
        
        WatchlistEntry watchlistEntry = new WatchlistEntry(target, staff, reason);
        watchlistEntry.notes = notes;
        watchlistEntry.priority = priority;
        return watchlistEntry;
    }
    
    public UUID getTarget() {
        return target;
    }
    
    public UUID getStaff() {
        return staff;
    }
    
    public String getReason() {
        return reason;
    }
    
    public List<WatchlistNote> getNotes() {
        return notes;
    }
    
    public void addNote(WatchlistNote note) {
        this.notes.add(note);
    }
    
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        WatchlistEntry entry = (WatchlistEntry) o;
        return Objects.equals(target, entry.target);
    }
    
    public int hashCode() {
        return Objects.hash(target);
    }
    
    public Priority getPriority() {
        return priority;
    }
    
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    
    public String formatLine(String... args) {
        return " &8- &a" + Utils.convertUUIDToName(this.target, "") + " added by " + Utils.convertUUIDToName(this.staff, "") + " for " + this.reason;
    }
}