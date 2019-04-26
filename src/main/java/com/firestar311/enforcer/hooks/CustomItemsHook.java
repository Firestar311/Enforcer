package com.firestar311.enforcer.hooks;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.lib.builder.ItemBuilder;
import com.firestar311.lib.customitems.CustomItemFactory;
import com.firestar311.lib.customitems.api.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

public class CustomItemsHook {
    
    private ICategory category;
    private IItemManager itemManager;
    
    public CustomItemsHook(Enforcer plugin) {
        RegisteredServiceProvider<IItemManager> rsp = plugin.getServer().getServicesManager().getRegistration(IItemManager.class);
        if (rsp == null) return;
        itemManager = rsp.getProvider();
        ItemStack icon = ItemBuilder.start(Material.BREWING_STAND).withName("&eEnforcer").buildItem();
        this.category = CustomItemFactory.createCategory(plugin, "Enforcer", icon, "enforcer.items.category");
        itemManager.addCategory(category);
        
        ItemStack inspectTool = ItemBuilder.start(Material.DIAMOND_SHOVEL).withName("&bPrison Inspect Tool").buildItem();
        ICustomItem inspectCItem = CustomItemFactory.createCustomItem(plugin, "inspecttool", inspectTool, "enforcer.items.tool.inspect");
        category.addItem(inspectCItem);
    }
    
    public IItemManager getItemManager() {
        return itemManager;
    }
}