package com.firestar311.enforcer.hooks;

import com.firestar311.customitems.CustomItemFactory;
import com.firestar311.customitems.CustomItems;
import com.firestar311.customitems.api.ICategory;
import com.firestar311.customitems.api.ICustomItem;
import com.firestar311.enforcer.Enforcer;
import com.firestar311.lib.builder.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class CustomItemsHook {
    
    private CustomItems customItems;
    private ICategory category;
    
    public CustomItemsHook(Enforcer plugin) {
        Plugin p = plugin.getServer().getPluginManager().getPlugin("CustomItems");
        if (p != null) {
            this.customItems = ((CustomItems) p);
        } else {
            plugin.getLogger().info("CustomItems not found, use commands to get the tools.");
            return;
        }
    
        ItemStack icon = ItemBuilder.start(Material.BREWING_STAND).withName("&eEnforcer").buildItem();
        this.category = CustomItemFactory.createCategory(plugin, "Enforcer", icon, "enforcer.items.category");
        customItems.getItemManager().addCategory(category);
        
        ItemStack inspectTool = ItemBuilder.start(Material.DIAMOND_SHOVEL).withName("&bPrison Inspect Tool").buildItem();
        ICustomItem inspectCItem = CustomItemFactory.createCustomItem(plugin, "inspecttool", inspectTool, "enforcer.items.tool.inspect");
        category.addItem(inspectCItem);
    }
    
    public CustomItems getCustomItems() {
        return customItems;
    }
}