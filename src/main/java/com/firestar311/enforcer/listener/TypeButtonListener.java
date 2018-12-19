package com.firestar311.enforcer.listener;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.model.enums.PunishmentType;
import com.firestar311.lib.gui.ButtonListener;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class TypeButtonListener implements ButtonListener {
    
    private Enforcer plugin;
    private PunishmentType type;
    private String display;
    private Player player;
    
    public TypeButtonListener(Enforcer plugin, Player player, PunishmentType type, String display) {
        this.type = type;
        this.display = display;
        this.plugin = plugin;
        this.player = player;
    }
    
    public void onClick(InventoryClickEvent event) {
    
    }
}
