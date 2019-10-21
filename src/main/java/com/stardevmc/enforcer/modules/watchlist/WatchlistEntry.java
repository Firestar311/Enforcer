package com.stardevmc.enforcer.modules.watchlist;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.*;

public class WatchlistEntry implements ConfigurationSerializable {
    private UUID target, staff;
    private Set<String> reasons;
    private List<WatchlistNote> notes = new ArrayList<>();
    
    public WatchlistEntry(UUID target, UUID staff, Set<String> reasons) {
        this.target = target;
        this.staff = staff;
        this.reasons = reasons;
    }
    
    public Map<String, Object> serialize() {
        Map<String, Object> serialized = new HashMap<>();
        serialized.put("target", this.target.toString());
        serialized.put("staff", this.target.toString());
        serialized.put("reasons", new HashSet<>(this.reasons));
        serialized.put("noteAmount", this.notes.size() + "");
        for (int i = 0; i < notes.size(); i++) {
            serialized.put("notes" + i, notes.get(i));
        }
        return serialized;
    }
    
    public static WatchlistEntry deserialize(Map<String, Object> serialized) {
        UUID target = UUID.fromString((String) serialized.get("target"));
        UUID staff = UUID.fromString((String) serialized.get("staff"));
        List<String> reasons = (List<String>) serialized.get("reasons");
        int noteAmount = Integer.parseInt((String) serialized.get("noteAmount"));
        List<WatchlistNote> notes = new ArrayList<>();
        for (int i = 0; i < noteAmount; i++) {
            WatchlistNote note = (WatchlistNote) serialized.get("notes" + i);
        }
        
        WatchlistEntry watchlistEntry = new WatchlistEntry(target, staff, new HashSet<>(reasons));
        watchlistEntry.notes = notes;
        return watchlistEntry;
    }
    
    public UUID getTarget() {
        return target;
    }
    
    public UUID getStaff() {
        return staff;
    }
    
    public Set<String> getReasons() {
        return reasons;
    }
    
    public void addReason(String reason) {
        this.reasons.add(reason);
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
}