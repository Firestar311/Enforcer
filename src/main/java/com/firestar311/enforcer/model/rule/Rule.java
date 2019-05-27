package com.firestar311.enforcer.model.rule;

import com.firestar311.lib.builder.ItemBuilder;
import com.firestar311.lib.pagination.Paginatable;
import com.firestar311.lib.util.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Rule implements Paginatable, Comparable<Rule> {
    
    private int id;
    private String internalId, name, description;
    private SortedMap<Integer, RuleOffense> offenses = new TreeMap<>();
    private Material material;
    
    private ItemStack itemStack;
    
    public Rule(int id, String internalId, String name, String description) {
        this.id = id;
        this.internalId = internalId;
        this.name = name;
        this.description = description;
    }
    
    public Rule(String internalId, String name) {
        this(-1, internalId, name, "");
    }
    
    public int getId() {
        return id;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void addOffense(RuleOffense action) {
        action.setOffenseNumber(this.offenses.size() + 1);
        this.offenses.put(action.getOffenseNumber(), action);
    }
    
    public void addOffense(int offenseNumber, RuleOffense offense) {
        this.offenses.put(offenseNumber, offense);
    }
    
    public RuleOffense getOffense(int offenseCount) {
        return this.offenses.get(offenseCount);
    }
    
    public String getName() {
        return name;
    }
    
    public SortedMap<Integer, RuleOffense> getOffenses() {
        return new TreeMap<>(offenses);
    }
    
    public String getInternalId() {
        return internalId;
    }
    
    public String formatLine(String... args) {
        return " &8- &2" + name + "&8: &d" + description;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void removeOffense(int offenseNumber) {
        this.offenses.remove(offenseNumber);
    }
    
    public void setMaterial(Material material) {
        this.material = material;
        this.itemStack = getItemStack();
    }
    
    public Material getMaterial() {
        return material;
    }
    
    public ItemStack getItemStack() {
        List<String> descLore = Utils.wrapLore(45, description);
        
        if (this.itemStack == null) {
            if (material != null) this.itemStack = ItemBuilder.start(material).withName("&a&l" + getName()).withLore(descLore).buildItem();
        }
        return itemStack;
    }
    
    public int compareTo(Rule o) {
        return Integer.compare(this.id, o.id);
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setInternalId(String internalId) {
        this.internalId = internalId.toLowerCase().replace(" ", "_");
    }
    
    public void clearOffenses() {
        this.offenses.clear();
    }
    
    public String getPermission() {
        return "enforcer.rules." + internalId;
    }
    
    public boolean hasPermission(Player player) {
        return player.hasPermission("enforcer.rules.*") || player.hasPermission(getPermission());
    }
}