package com.firestar311.enforcer.util;

import com.firestar311.lib.builder.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import static com.firestar311.enforcer.model.enums.PunishmentType.*;

public final class Items {
    
    private static Items instance = new Items();
    
    public static ItemStack permanentBanItem, temporaryBanItem, permanentMuteItem, temporaryMuteItem, warnItem, kickItem, jailItem;
    
    private Items() {
        permanentBanItem = ItemBuilder.start(Material.DIAMOND_AXE).withName(PERMANENT_BAN.getDisplayName()).withEnchantment(Enchantment.DAMAGE_ALL, 1).buildItem();
        temporaryBanItem = ItemBuilder.start(Material.IRON_AXE).withName(TEMPORARY_BAN.getDisplayName()).buildItem();
        permanentMuteItem = ItemBuilder.start(Material.TNT).withName(PERMANENT_MUTE.getDisplayName()).buildItem();
        temporaryMuteItem = ItemBuilder.start(Material.FLINT_AND_STEEL).withName(TEMPORARY_MUTE.getDisplayName()).buildItem();
        warnItem = ItemBuilder.start(Material.PAPER).withName(WARN.getDisplayName()).buildItem();
        kickItem = ItemBuilder.start(Material.FISHING_ROD).withName(KICK.getDisplayName()).buildItem();
        jailItem = ItemBuilder.start(Material.IRON_BARS).withName(JAIL.getDisplayName()).buildItem();
    }
    
    public static Items getInstance() {
        return instance;
    }
}