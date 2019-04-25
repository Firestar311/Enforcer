package com.firestar311.enforcer.guis;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.model.enums.Visibility;
import com.firestar311.enforcer.model.punishment.abstraction.Punishment;
import com.firestar311.enforcer.model.rule.*;
import com.firestar311.enforcer.util.EnforcerUtils;
import com.firestar311.lib.builder.ItemBuilder;
import com.firestar311.lib.gui.*;
import com.firestar311.lib.player.PlayerInfo;
import com.firestar311.lib.util.Utils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.Map.Entry;
import java.util.UUID;

public class PunishGUI extends PaginatedGUI {
    
    private GUIButton PUBLIC_BUTTON = new GUIButton(ItemBuilder.start(Material.DIAMOND).withName("&9PUBLIC")
            .withLore(Utils.wrapLore(35, "Make this punishment a public punishment, where all players can see the notification")).buildItem());
    private GUIButton NORMAL_BUTTON = new GUIButton(ItemBuilder.start(Material.QUARTZ).withName("&9NORMAL")
            .withLore(Utils.wrapLore(35, "Make this punishment a normal punishment to where only staff members with the notify permission" +
                    "will be able to see the notification. This is also the default.")).withEnchantment(Enchantment.ARROW_DAMAGE, 1)
            .withItemFlags(ItemFlag.HIDE_ENCHANTS).buildItem());
    private GUIButton SILENT_BUTTON = new GUIButton(ItemBuilder.start(Material.REDSTONE).withName("&9SILENT")
            .withLore(Utils.wrapLore(35, "Make this punishment a silent punishment to where only staff members with the permission for " +
                    "your group or higher (Permission inheritance is key) will be able to see the notification.")).buildItem());
    
    static {
        PaginatedGUI.prepare(Enforcer.getInstance());
    }
    
   
    public PunishGUI(Enforcer plugin, PlayerInfo t) {
        super(plugin, "Punish > " + t.getLastName(), false, 54);
    
        ButtonListener listener = event -> {
            ItemStack itemStack = event.getCurrentItem();
            if (itemStack.hasItemMeta()) {
                if (itemStack.getItemMeta().getDisplayName() != null) {
                    if (itemStack.getItemMeta().getDisplayName().contains("PUBLIC")) {
                        PUBLIC_BUTTON.setItem(new ItemBuilder(PUBLIC_BUTTON.getItem()).clearEnchants()
                                .withEnchantment(Enchantment.ARROW_DAMAGE, 1).withItemFlags(ItemFlag.HIDE_ENCHANTS).buildItem());
                        NORMAL_BUTTON.setItem(new ItemBuilder(NORMAL_BUTTON.getItem()).clearEnchants().buildItem());
                        SILENT_BUTTON.setItem(new ItemBuilder(SILENT_BUTTON.getItem()).clearEnchants().buildItem());
                    } else if (itemStack.getItemMeta().getDisplayName().contains("NORMAL")) {
                        PUBLIC_BUTTON.setItem(new ItemBuilder(PUBLIC_BUTTON.getItem()).clearEnchants().buildItem());
                        NORMAL_BUTTON.setItem(new ItemBuilder(NORMAL_BUTTON.getItem()).clearEnchants()
                                .withEnchantment(Enchantment.ARROW_DAMAGE, 1).withItemFlags(ItemFlag.HIDE_ENCHANTS).buildItem());
                        SILENT_BUTTON.setItem(new ItemBuilder(SILENT_BUTTON.getItem()).clearEnchants().buildItem());
                    } else if (itemStack.getItemMeta().getDisplayName().contains("SILENT")) {
                        PUBLIC_BUTTON.setItem(new ItemBuilder(PUBLIC_BUTTON.getItem()).clearEnchants().buildItem());
                        NORMAL_BUTTON.setItem(new ItemBuilder(NORMAL_BUTTON.getItem()).clearEnchants().buildItem());
                        SILENT_BUTTON.setItem(new ItemBuilder(SILENT_BUTTON.getItem()).clearEnchants()
                                .withEnchantment(Enchantment.ARROW_DAMAGE, 1).withItemFlags(ItemFlag.HIDE_ENCHANTS).buildItem());
                    }
                    
                    event.getClickedInventory().setItem(47, PUBLIC_BUTTON.getItem());
                    event.getClickedInventory().setItem(49, NORMAL_BUTTON.getItem());
                    event.getClickedInventory().setItem(51, SILENT_BUTTON.getItem());
                }
            }
        };
    
        PUBLIC_BUTTON.setListener(listener);
        NORMAL_BUTTON.setListener(listener);
        SILENT_BUTTON.setListener(listener);
        setToolbarItem(2, PUBLIC_BUTTON);
        setToolbarItem(4, NORMAL_BUTTON);
        setToolbarItem(6, SILENT_BUTTON);
        
        for (Rule r : plugin.getDataManager().getRules()) {
            if (r.getMaterial() != null) {
                GUIButton button = new GUIButton(r.getItemStack());
                
                button.setListener(e -> {
                    final UUID target = t.getUuid();
                    
                    Player player = ((Player) e.getWhoClicked());
                    
                    Entry<Integer, Integer> offenseNumbers = plugin.getDataManager().getNextOffense(target, r);
                    
                    RuleOffense offense = r.getOffense(offenseNumbers.getKey());
                    if (offense == null) {
                        player.sendMessage(Utils.color("&cThere was a severe problem getting the next offense, use a manual punishment if an emergency, otherwise, contact the plugin developer"));
                        return;
                    }
                    
                    String server = plugin.getDataManager().getPrefix();
                    long currentTime = System.currentTimeMillis();
                    UUID punisher = player.getUniqueId();
                    String reason = r.getName() + " Offense #" + offenseNumbers.getValue();
                    for (RulePunishment rulePunishment : offense.getPunishments().values()) {
                        Punishment punishment = EnforcerUtils.getPunishmentFromRule(plugin, target, server, currentTime, punisher, reason, rulePunishment);
    
                        Visibility visibility = Visibility.NORMAL;
                        if (PUBLIC_BUTTON.getItem().getItemMeta().hasEnchant(Enchantment.ARROW_DAMAGE)) {
                            visibility = Visibility.PUBLIC;
                        } else if (SILENT_BUTTON.getItem().getItemMeta().hasEnchant(Enchantment.ARROW_DAMAGE)) {
                            visibility = Visibility.SILENT;
                        }
                        
                        punishment.setVisibility(visibility);
                        punishment.setRuleId(r.getId());
                        punishment.setOffenseNumber(offenseNumbers.getValue());
                        plugin.getDataManager().addPunishment(punishment);
                        punishment.executePunishment();
                    }
                    player.closeInventory();
                });
                addButton(button);
            }
        }
    }
    
    
}