package com.firestar311.enforcer.command;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.model.enums.PunishmentType;
import com.firestar311.enforcer.model.rule.*;
import com.firestar311.enforcer.util.*;
import com.firestar311.lib.pagination.Paginator;
import com.firestar311.lib.pagination.PaginatorFactory;
import com.firestar311.lib.util.Utils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class RuleCommand implements CommandExecutor {
    
    public Enforcer plugin;
    
    private Map<UUID, Paginator<?>> paginators = new HashMap<>();
    
    public RuleCommand(Enforcer plugin) {
        this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.color("&cOnly players may use that command."));
            return true;
        }
        
        Player player = ((Player) sender);
        
        if (!player.hasPermission(Perms.MRULES_MAIN)) {
            player.sendMessage(Utils.color("&cYou do not have permission to use that command."));
            return true;
        }
        
        if (args.length == 0) {
            PaginatorFactory<Rule> factory = new PaginatorFactory<>();
            factory.setMaxElements(7).setHeader("&7-=Moderator Rules=- &e({pagenumber}/{totalpages})").setFooter("&7Type /mrules page {nextpage} for more");
            plugin.getDataManager().getRules().forEach(factory::addElement);
            Paginator<Rule> paginator = factory.build();
            paginator.display(player, 1);
            this.paginators.put(player.getUniqueId(), paginator);
            return true;
        }
        
        if (Utils.checkCmdAliases(args, 0, "page", "p")) {
            if (!this.paginators.containsKey(player.getUniqueId())) {
                player.sendMessage(Utils.color("&cYou do not have any results"));
                return true;
            }
            
            int page;
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(Utils.color("&cThe value for the page number is not a valid number."));
                return true;
            }
            
            Paginator<?> paginator = this.paginators.get(player.getUniqueId());
            paginator.display(player, page);
        } else if (Utils.checkCmdAliases(args, 0, "create", "c")) {
            if (!(args.length > 1)) {
                player.sendMessage(Utils.color("&cUsage: /mrules create|c <name>"));
                return true;
            }
            
            if (!player.hasPermission(Perms.MRULES_CREATE)) {
                player.sendMessage(Utils.color("&cYou do not have permission to use that command."));
                return true;
            }
            
            String name = StringUtils.join(args, ' ', 1, args.length);
            String internalId = name.toLowerCase().replace(" ", "_");
            
            Rule existing = plugin.getDataManager().getRule(internalId);
            if (existing != null) {
                player.sendMessage(Utils.color("&cA rule with that name already exists."));
                return true;
            }
            
            Rule rule = new Rule(internalId, name);
            plugin.getDataManager().addRule(rule);
            String message = Messages.RULE_CREATE;
            message = message.replace(Variables.RULE_NAME, rule.getName());
            message = message.replace(Variables.RULE_ID, rule.getId() + "");
            message = message.replace(Variables.RULE_INTERNALID, rule.getInternalId());
            Messages.sendOutputMessage(player, message, plugin);
            return true;
        }
        
        Rule rule = plugin.getDataManager().getRule(args[0]);
        if (rule == null) {
            player.sendMessage(Utils.color("&cCould not find a rule with that identifier."));
            return true;
        }
        
        if (Utils.checkCmdAliases(args, 1, "setdescription", "sd")) {
            if (!(args.length > 2)) {
                player.sendMessage(Utils.color("&cUsage: /mrules <rule> setdescription|sd <description>"));
                return true;
            }
            
            if (!player.hasPermission(Perms.MRULES_SET_DESCRIPTION)) {
                player.sendMessage(Utils.color("&cYou do not have permission to use that command."));
                return true;
            }
            
            String description = StringUtils.join(args, " ", 2, args.length);
            rule.setDescription(description);
            
            String message = Messages.RULE_SET_DESCRIPTION;
            message = message.replace(Variables.RULE_NAME, rule.getName());
            message = message.replace(Variables.RULE_DESCRIPTION, rule.getDescription());
            Messages.sendOutputMessage(player, message, plugin);
        } else if (Utils.checkCmdAliases(args, 1, "remove", "r")) {
            if (!(args.length > 1)) {
                player.sendMessage(Utils.color("&cUsage: /mrules <id> remove|r"));
                return true;
            }
    
            if (!player.hasPermission(Perms.MRULES_REMOVE)) {
                player.sendMessage(Utils.color("&cYou do not have permission to use that command."));
                return true;
            }
            
            plugin.getDataManager().removeRule(rule.getId());
            player.sendMessage(Utils.color("&aRemoved the rule &b" + rule.getInternalId()));
        } else if (Utils.checkCmdAliases(args, 1, "setmaterial", "sm")) {
            if (!(args.length > 1)) {
                player.sendMessage(Utils.color("&cUsage: /mrules <id> setmaterial|sr"));
                return true;
            }
    
            if (!player.hasPermission(Perms.MRULES_SET_MATERIAL)) {
                player.sendMessage(Utils.color("&cYou do not have permission to use that command."));
                return true;
            }
            
            Material material;
            try {
                material = Material.valueOf(args[2].toUpperCase());
            } catch (Exception e) {
                player.sendMessage(Utils.color("&cThe value that you provided is not a valid material."));
                return true;
            }
            
            rule.setMaterial(material);
            player.sendMessage(Utils.color("&aSet the material of the rule &b" + rule.getInternalId() + " &ato &b" + material.name()));
        } else if (Utils.checkCmdAliases(args, 1, "view", "v")) {
            if (!player.hasPermission(Perms.MRULES_VIEW)) {
                player.sendMessage(Utils.color("&cYou do not have permission to use that command."));
                return true;
            }
            
            player.sendMessage(Utils.color("&aViewing information for rule " + rule.getName() + "\n" +
                    " &8- &7Rule ID: &e" + rule.getId() + "\n" +
                    " &8- &7Rule Internal ID: &e" + rule.getInternalId() + "\n" +
                    " &8- &7Rule Name: &e" + rule.getName()+ "\n" +
                    " &8- &7Rule Description: &e" + rule.getDescription()+ "\n" +
                    " &8- &7Rule Material: &e" + rule.getMaterial() + "\n" +
                    " &8- &7Offense Count: &e" + rule.getOffenses().size()));
        } else if (Utils.checkCmdAliases(args, 1, "setname", "sn")) {
            if (!player.hasPermission(Perms.MRULES_SET_NAME)) {
                player.sendMessage(Utils.color("&cYou do not have permission to use that command."));
                return true;
            }
    
            String name = StringUtils.join(args, ' ', 2, args.length);
            
            Rule existing = plugin.getDataManager().getRule(name);
            if (existing != null) {
                player.sendMessage(Utils.color("&cA rule with that name already exists."));
                return true;
            }
            
            rule.setName(name);
            rule.setInternalId(name);
            player.sendMessage(Utils.color("&aSet the name of rule &b" + rule.getId() + " &ato &b" + rule.getName() + " &aand the internal id to &b" + rule.getInternalId()));
        } else if (Utils.checkCmdAliases(args, 1, "offenses", "of")) {
            if (!(args.length > 2)) {
                player.sendMessage(Utils.color("&cUsage: /mrules <rule> offenses|of <list|create|offenseId> <options...>"));
                return true;
            }
    
            if (!player.hasPermission(Perms.MRULES_OFFENSES_MAIN)) {
                player.sendMessage(Utils.color("&cYou do not have permission to use that command."));
                return true;
            }
            
            if (Utils.checkCmdAliases(args, 2, "list", "l")) {
                if (!player.hasPermission(Perms.MRULES_OFFENSES_LIST)) {
                    player.sendMessage(Utils.color("&cYou do not have permission to use that command."));
                    return true;
                }
                PaginatorFactory<RuleOffense> factory = new PaginatorFactory<>();
                factory.setMaxElements(7).setHeader("&7-=Offenses for " + rule.getName() + "=- &e({pagenumber}/{totalpages})").setFooter("&7Type /mrules page {nextpage} for more");
                rule.getOffenses().forEach((id, offense) -> factory.addElement(offense));
                Paginator<RuleOffense> paginator = factory.build();
                paginator.display(player, 1);
                this.paginators.put(player.getUniqueId(), paginator);
                return true;
            } else if (Utils.checkCmdAliases(args, 2, "create", "c")) {
                if (!player.hasPermission(Perms.MRULES_OFFESNES_CREATE)) {
                    player.sendMessage(Utils.color("&cYou do not have permission to use that command."));
                    return true;
                }
                
                RuleOffense offense = new RuleOffense();
                rule.addOffense(offense);
                player.sendMessage(Utils.color("&aCreated a new offense for the rule &b" + rule.getInternalId() + " &awith the offense number &b" + offense.getOffenseNumber()));
                return true;
            } else if (Utils.checkCmdAliases(args, 2, "clear")) {
                if (!player.hasPermission(Perms.MRULES_OFFENSES_CLEAR)) {
                    player.sendMessage(Utils.color("&cYou do not have permission to use that command."));
                    return true;
                }
                
                rule.clearOffenses();
                player.sendMessage(Utils.color("&aCleared the offenses of the rule &b" + rule.getName()));
                return true;
            }
            
            int offenseNumber;
            try {
                offenseNumber = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage(Utils.color("&cThe value you provided for the offense number is not a valid number"));
                return true;
            }
            
            RuleOffense offense = rule.getOffense(offenseNumber);
            
            if (offense == null) {
                offense = new RuleOffense();
                rule.addOffense(offense);
                player.sendMessage(Utils.color("&aCreated a new offense for the rule &b" + rule.getInternalId() + " &awith the offense number &b" + offense.getOffenseNumber()));
            }
    
            if (Utils.checkCmdAliases(args, 3, "list", "l")) {
                if (!player.hasPermission(Perms.MRULES_OFFESNES_PUNISHMENTS_LIST)) {
                    player.sendMessage(Utils.color("&cYou do not have permission to use that command."));
                    return true;
                }
                PaginatorFactory<RulePunishment> factory = new PaginatorFactory<>();
                factory.setMaxElements(7).setHeader("&7-=Punishments for " + rule.getName() + ":" + offenseNumber + "=- &e({pagenumber}/{totalpages})").setFooter("&7Type /mrules page {nextpage} for more");
                offense.getPunishments().forEach((id, punishment) -> factory.addElement(punishment));
                Paginator<RulePunishment> paginator = factory.build();
                paginator.display(player, 1);
                this.paginators.put(player.getUniqueId(), paginator);
                return true;
            } else if (Utils.checkCmdAliases(args, 3, "remove")) {
                if (!player.hasPermission(Perms.MRULES_OFFENSES_REMOVE)) {
                    player.sendMessage(Utils.color("&cYou do not have permission to remove rules."));
                    return true;
                }
                rule.removeOffense(offense.getOffenseNumber());
                player.sendMessage(Utils.color("&cRemoved the offense " + offense.getOffenseNumber() + " from the rule " + rule.getInternalId()));
            } else if (Utils.checkCmdAliases(args, 3, "addpunishment", "ap")) {
                if (!player.hasPermission(Perms.MRULES_OFFENSES_PUNISHMENTS_ADD)) {
                    player.sendMessage(Utils.color("&cYou do not have permission to use that command."));
                    return true;
                }
                if (!(args.length > 4)) {
                    player.sendMessage(Utils.color("&cUsage: /mrules <rule> offenses <id> addpunishment <type> [length] [units]"));
                    return true;
                }
                
                PunishmentType type = PunishmentType.getType(args[4].toUpperCase());
                int length = -1;
                String units = "";
                
                String message;
                
                if (type.equals(PunishmentType.TEMPORARY_BAN) || type.equals(PunishmentType.TEMPORARY_MUTE)) {
                    length = Integer.valueOf(args[5]);
                    units = args[6];
                    message = "&e[<id>] &aAdded a punishment with the type " + type.getDisplayName() + " &aand the length &b" + length + " " + units + " &ato offense &b" + offense.getOffenseNumber() + " &aof the rule &b" + rule.getInternalId();
                } else {
                    message = "&e[<id>] &aAdded a punishment with the type " + type.getDisplayName() + " &ato offense &b" + offense.getOffenseNumber() + " &aof the rule &b" + rule.getInternalId();
                }
                
                RulePunishment punishment = new RulePunishment(type, length, units);
                offense.addPunishment(punishment);
                message = message.replace("<id>", punishment.getId() + "");
                player.sendMessage(Utils.color(message));
            } else if (Utils.checkCmdAliases(args, 3, "removepunishment", "rp")) {
                if (!player.hasPermission(Perms.MRULES_OFFENSES_PUNISHMENTS_REMOVE)) {
                    player.sendMessage(Utils.color("&cYou do not have permission to use that command."));
                    return true;
                }
                
                if (!(args.length > 4)) {
                    player.sendMessage(Utils.color("&cUsage: /mrules <rule> offenses <id> removepunishment <id>"));
                    return true;
                }
                
                int id;
                try {
                    id = Integer.parseInt(args[4]);
                } catch (NumberFormatException e) {
                    player.sendMessage(Utils.color("&cYou provided an invalid number for the punishment id."));
                    return true;
                }
                
                offense.removePunishment(id);
                player.sendMessage(Utils.color("&aRemoved the punishment &b" + id + " &afrom the offense number &b" + offense.getOffenseNumber() + " &aof the rule &b" + rule.getInternalId()));
            } else if (Utils.checkCmdAliases(args, 3, "clearpunishments", "cp")) {
                if (!player.hasPermission(Perms.MRULES_OFFENSES_PUNISHMENTS_CLEAR)) {
                    player.sendMessage(Utils.color("&cYou do not have permission to use that command."));
                    return true;
                }
                
                offense.clearPunishments();
                player.sendMessage(Utils.color("&aCleared all punishments of the offense number &b" + offense.getOffenseNumber() + " &afor the rule &b" + rule.getName()));
            }
        }
        
        return true;
    }
}
