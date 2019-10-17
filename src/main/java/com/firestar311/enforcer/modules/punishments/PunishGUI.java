package com.firestar311.enforcer.modules.punishments;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.modules.punishments.target.PlayerTarget;
import com.firestar311.enforcer.modules.punishments.target.Target;
import com.firestar311.enforcer.modules.punishments.type.abstraction.Punishment;
import com.firestar311.enforcer.modules.rules.RuleManager;
import com.firestar311.enforcer.modules.rules.rule.*;
import com.firestar311.enforcer.util.EnforcerUtils;
import com.firestar311.lib.builder.ItemBuilder;
import com.firestar311.lib.gui.*;
import com.firestar311.lib.util.Utils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

public class PunishGUI extends PaginatedGUI {
    
    static {
        PaginatedGUI.prepare(Enforcer.getInstance());
    }
    
    public PunishGUI(Enforcer plugin, Player pu, Target t) {
        super(plugin, "Punish > " + t.getName(), false, 54);
        
        GUIButton PUBLIC_BUTTON = new GUIButton(ItemBuilder.start(Material.DIAMOND).withName("&9PUBLIC").withLore(Utils.wrapLore(35, "Make this punishment a public punishment, where all players can see the notification")).buildItem());
        GUIButton NORMAL_BUTTON = new GUIButton(ItemBuilder.start(Material.QUARTZ).withName("&9NORMAL").withLore(Utils.wrapLore(35, "Make this punishment a normal punishment to where only staff members with the notify permission" + " will be able to see the notification. This is also the default.")).withEnchantment(Enchantment.ARROW_DAMAGE, 1).withItemFlags(ItemFlag.HIDE_ENCHANTS).buildItem());
        GUIButton SILENT_BUTTON = new GUIButton(ItemBuilder.start(Material.REDSTONE).withName("&9SILENT").withLore(Utils.wrapLore(35, "Make this punishment a silent punishment to where only staff members with the permission for " + "your group or higher (Permission inheritance is key) will be able to see the notification.")).buildItem());
        
        ButtonListener listener = event -> {
            ItemStack itemStack = event.getCurrentItem();
            if (itemStack.hasItemMeta()) {
                if (itemStack.getItemMeta().getDisplayName() != null) {
                    if (itemStack.getItemMeta().getDisplayName().contains("PUBLIC")) {
                        PUBLIC_BUTTON.setItem(new ItemBuilder(PUBLIC_BUTTON.getItem()).clearEnchants().withEnchantment(Enchantment.ARROW_DAMAGE, 1).withItemFlags(ItemFlag.HIDE_ENCHANTS).buildItem());
                        NORMAL_BUTTON.setItem(new ItemBuilder(NORMAL_BUTTON.getItem()).clearEnchants().buildItem());
                        SILENT_BUTTON.setItem(new ItemBuilder(SILENT_BUTTON.getItem()).clearEnchants().buildItem());
                    } else if (itemStack.getItemMeta().getDisplayName().contains("NORMAL")) {
                        PUBLIC_BUTTON.setItem(new ItemBuilder(PUBLIC_BUTTON.getItem()).clearEnchants().buildItem());
                        NORMAL_BUTTON.setItem(new ItemBuilder(NORMAL_BUTTON.getItem()).clearEnchants().withEnchantment(Enchantment.ARROW_DAMAGE, 1).withItemFlags(ItemFlag.HIDE_ENCHANTS).buildItem());
                        SILENT_BUTTON.setItem(new ItemBuilder(SILENT_BUTTON.getItem()).clearEnchants().buildItem());
                    } else if (itemStack.getItemMeta().getDisplayName().contains("SILENT")) {
                        PUBLIC_BUTTON.setItem(new ItemBuilder(PUBLIC_BUTTON.getItem()).clearEnchants().buildItem());
                        NORMAL_BUTTON.setItem(new ItemBuilder(NORMAL_BUTTON.getItem()).clearEnchants().buildItem());
                        SILENT_BUTTON.setItem(new ItemBuilder(SILENT_BUTTON.getItem()).clearEnchants().withEnchantment(Enchantment.ARROW_DAMAGE, 1).withItemFlags(ItemFlag.HIDE_ENCHANTS).buildItem());
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
    
        RuleManager ruleManager = plugin.getRuleModule().getManager();
        for (Rule r : ruleManager.getRules()) {
            if (r.hasPermission(pu)) {
                if (r.getMaterial() != null) {
                    if (!(t instanceof PlayerTarget)) {
                        continue;
                    }
                    
                    PlayerTarget target = (PlayerTarget) t;
                    Entry<Integer, Integer> oN = ruleManager.getNextOffense(pu.getUniqueId(), target.getUniqueId(), r);
        
                    final RuleOffense off = r.getOffense(oN.getKey());
                    if (off == null) {
                        return;
                    }
        
                    List<String> lore = r.getItemStack().getItemMeta().getLore();
                    lore.add("");
                    if (off.hasPermission(pu)) {
                        lore.add("&fThe next punishment for &b" + t.getName());
                        lore.add("&fWill result in the following");
                        lore.add("&fReason: &e" + r.getName() + " Offense #" + off.getOffenseNumber());
                        for (RulePunishment rP : off.getPunishments().values()) {
                            lore.add(" &8- " + EnforcerUtils.getPunishString(rP.getType(), rP.getLength()));
                        }
                    } else {
                        lore.add("&4You do not have permission to punish on the next offense");
                    }
        
                    ItemStack itemStack = ItemBuilder.start(r.getItemStack()).clearLore().withLore(lore).buildItem();
                    GUIButton button = new GUIButton(itemStack);
        
                    button.setListener(e -> {
                        Player player = ((Player) e.getWhoClicked());
                        Entry<Integer, Integer> offenseNumbers = ruleManager.getNextOffense(player.getUniqueId(), target.getUniqueId(), r);
            
                        RuleOffense offense = r.getOffense(offenseNumbers.getKey());
                        if (offense == null) {
                            player.sendMessage(Utils.color("&cThere was a severe problem getting the next offense, use a manual punishment if an emergency, otherwise, contact the plugin developer"));
                            return;
                        }
                        
                        if (offense.hasPermission(player)) {
                            String server = plugin.getSettingsManager().getPrefix();
                            long currentTime = System.currentTimeMillis();
                            UUID punisher = player.getUniqueId();
                            String reason = r.getName() + " Offense #" + offenseNumbers.getValue();
                            for (RulePunishment rulePunishment : offense.getPunishments().values()) {
                                PunishmentBuilder puBuilder = new PunishmentBuilder(target);
                                puBuilder.setType(rulePunishment.getType());
                                puBuilder.setReason(reason).setPunisher(punisher).setServer(server).setDate(currentTime).setLength(rulePunishment.getLength());
                                puBuilder.setRuleId(r.getId());
                                puBuilder.setOffenseNumber(offenseNumbers.getValue());
        
                                Visibility visibility = Visibility.NORMAL;
                                if (PUBLIC_BUTTON.getItem().getItemMeta().hasEnchant(Enchantment.ARROW_DAMAGE)) {
                                    visibility = Visibility.PUBLIC;
                                } else if (SILENT_BUTTON.getItem().getItemMeta().hasEnchant(Enchantment.ARROW_DAMAGE)) {
                                    visibility = Visibility.SILENT;
                                }
        
                                puBuilder.setVisibility(visibility);
                                Punishment punishment = puBuilder.build();
                                plugin.getPunishmentModule().getManager().addPunishment(punishment);
                                punishment.executePunishment();
                            }
                            player.closeInventory();
                        } else {
                            ItemStack cache = button.getItem();
                            ItemStack warning = ItemBuilder.start(Material.BARRIER).withName("&4You cannot perform that action.").withLore("&cYou are not allowed to punish", "&cthat player because you do not", "&chave the Offense Permission.").buildItem();
                        
                            e.getClickedInventory().setItem(e.getSlot(), warning);
                            new BukkitRunnable() {
                                public void run() {
                                    try {
                                        e.getClickedInventory().setItem(e.getSlot(), cache);
                                    } catch (Exception ex) {}
                                }
                            }.runTaskLater(plugin, 120L);
                        }
                    });
                    addButton(button);
                }
            }
        }
    }
    
    
}