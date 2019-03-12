package com.firestar311.enforcer.command;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.guis.PunishGUI;
import com.firestar311.enforcer.model.Prison;
import com.firestar311.enforcer.model.enums.Visibility;
import com.firestar311.enforcer.model.punishment.abstraction.*;
import com.firestar311.enforcer.model.punishment.type.*;
import com.firestar311.enforcer.model.rule.*;
import com.firestar311.enforcer.util.Perms;
import com.firestar311.lib.player.PlayerInfo;
import com.firestar311.lib.util.Utils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Map.Entry;
import java.util.UUID;

@SuppressWarnings("Duplicates")
public class PunishmentCommands implements CommandExecutor {
    
    private Enforcer plugin;
    
    public PunishmentCommands(Enforcer plugin) {
        this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.color("&cOnly players may use punishment commands."));
            return true;
        }
        
        Player player = ((Player) sender);
        String prefix = plugin.getDataManager().getPrefix();
        
        PlayerInfo info = plugin.getDataManager().getInfo(args[0]);
        if (info == null) {
            player.sendMessage(Utils.color("&cCould not find a player by that name. Punishing players that have yet to join is not supported yet."));
            return true;
        }
    
        //TODO This code is used for my own server
//        UUID firestar311 = UUID.fromString("3f7891ce-5a73-4d52-a2ba-299839053fdc");
//        if (info.getUuid().equals(firestar311) && !player.getUniqueId().equals(firestar311)) {
//            player.sendMessage(Utils.color("&cYou cannot punish that player."));
//            return true;
//        }
        
        if (Bukkit.getPlayer(info.getUuid()) == null) {
            if (!player.hasPermission(Perms.OFFLINE_PUNISH)) {
                player.sendMessage(Utils.color("&cYou cannot punish offline players."));
                return true;
            }
        }
    
        try {
            net.milkbowl.vault.permission.Permission perms = Enforcer.getInstance().getPermission();
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(info.getUuid());
            String groupName = perms.getPrimaryGroup(player.getWorld().getName(), offlinePlayer).toLowerCase();
            if (groupName != null && !groupName.equals("")) {
                if (!player.hasPermission("enforcer.immunity." + groupName)) {
                    player.sendMessage(Utils.color("&cYou cannot punish that player because they are immune."));
                    return true;
                }
            }
        } catch (Exception ignored) {}
        
        if (cmd.getName().equalsIgnoreCase("punish")) {
            if (!player.hasPermission(Perms.PUNISH_COMMAND)) {
                player.sendMessage(Utils.color("&cYou do not have permission to use that command."));
                return true;
            }
            
            if (args.length == 1) {
                PunishGUI punishGUI = new PunishGUI(plugin, info);
                player.openInventory(punishGUI.getInventory());
                return true;
            }
    
            Rule rule = plugin.getDataManager().getRule(StringUtils.join(args, " ", 1, args.length));
            if (rule == null) {
                player.sendMessage(Utils.color("&cThe value you provided does not match to a valid rule."));
                return true;
            }
    
            Entry<Integer, Integer> offenseNumbers = plugin.getDataManager().getNextOffense(info.getUuid(), rule);
            
            RuleOffense offense = rule.getOffense(offenseNumbers.getKey());
            if (offense == null) {
                player.sendMessage(Utils.color("&cThere was a severe problem getting the next offense, use a manual punishment if an emergency, otherwise, contact the plugin developer"));
                return true;
            }
            
            String server = plugin.getDataManager().getPrefix();
            long currentTime = System.currentTimeMillis();
            UUID punisher = player.getUniqueId(), target = info.getUuid();
            String reason = rule.getName() + " Offense #" + offenseNumbers.getValue();
            for (RulePunishment rulePunishment : offense.getPunishments().values()) {
                Punishment punishment = null;
                long expire = currentTime + rulePunishment.getLength();
                switch(rulePunishment.getType()) {
                    case PERMANENT_BAN: punishment = new PermanentBan(server, punisher, target, reason, currentTime);
                        break;
                    case TEMPORARY_BAN: punishment = new TemporaryBan(server, punisher, target, reason, currentTime, expire);
                        break;
                    case PERMANENT_MUTE: punishment = new PermanentMute(server, punisher, target, reason, currentTime);
                        break;
                    case TEMPORARY_MUTE: punishment = new TemporaryMute(server, punisher, target, reason, currentTime, expire);
                        break;
                    case WARN: punishment = new WarnPunishment(server, punisher, target, reason, currentTime);
                        break;
                    case KICK: punishment = new KickPunishment(server, punisher, target, reason, currentTime);
                        break;
                    case JAIL:
                        Prison prison = plugin.getDataManager().findPrison();
                        punishment = new JailPunishment(server, punisher, target, reason, currentTime, prison.getId());
                        break;
                }
                punishment.setRuleId(rule.getId());
                punishment.setOffenseNumber(offenseNumbers.getValue());
                plugin.getDataManager().addPunishment(punishment);
                punishment.executePunishment();
            }
        } else {
            Visibility visibility = Visibility.NORMAL;
            boolean ignoreTraining = false;
            for (String arg : args) {
                if (arg.equalsIgnoreCase("-p")) {
                    visibility = Visibility.PUBLIC;
                    break;
                } else if (arg.equalsIgnoreCase("-s")) {
                    visibility = Visibility.SILENT;
                    break;
                } else if (arg.equalsIgnoreCase("-t")) {
                    if (!player.hasPermission(Perms.FLAG_IGNORE_TRAINING)) {
                        player.sendMessage(Utils.color("&cYou do not have permission to ignore training mode."));
                        return true;
                    }
                    ignoreTraining = true;
                }
            }
            
            long currentTime = System.currentTimeMillis();
            
            if (cmd.getName().equalsIgnoreCase("ban")) {
                if (!player.hasPermission(Perms.BAN)) {
                    player.sendMessage(Utils.color("&cYou lack the permission &7(" + Perms.BAN + ")"));
                    return true;
                }
                String reason = getReason(1, args);
                if (reason.equals("")) {
                    player.sendMessage(Utils.color("&cYou must supply a reason for the punishment."));
                    return true;
                }
                BanPunishment punishment = new PermanentBan(prefix, player.getUniqueId(), info.getUuid(), reason, currentTime, visibility);
                plugin.getDataManager().addBan(punishment);
                punishment.executePunishment();
                checkTraining(punishment, ignoreTraining);
            } else if (cmd.getName().equalsIgnoreCase("tempban")) {
                if (!player.hasPermission(Perms.TEMP_BAN)) {
                    player.sendMessage(Utils.color("&cYou lack the permission &7(" + Perms.TEMP_BAN + ")"));
                    return true;
                }
                
                long expire = extractExpire(player, currentTime, args[1]);
                
                if (!(args.length > 2)) {
                    player.sendMessage(Utils.color("&cYou must supply a reason for the punishment."));
                    return true;
                }
                
                String reason = getReason(2, args);
                BanPunishment punishment = new TemporaryBan(prefix, player.getUniqueId(), info.getUuid(), reason, currentTime, visibility, expire);
                plugin.getDataManager().addBan(punishment);
                punishment.executePunishment();
                checkTraining(punishment, ignoreTraining);
            } else if (cmd.getName().equalsIgnoreCase("mute")) {
                if (!player.hasPermission(Perms.MUTE)) {
                    player.sendMessage(Utils.color("&cYou lack the permission &7(" + Perms.MUTE + ")"));
                    return true;
                }
                
                String reason = getReason(1, args);
                if (reason.equals("")) {
                    player.sendMessage(Utils.color("&cYou must supply a reason for the punishment."));
                    return true;
                }
                MutePunishment punishment = new PermanentMute(prefix, player.getUniqueId(), info.getUuid(), reason, currentTime, visibility);
                plugin.getDataManager().addMute(punishment);
                punishment.executePunishment();
                checkTraining(punishment, ignoreTraining);
            } else if (cmd.getName().equalsIgnoreCase("tempmute")) {
                if (!player.hasPermission(Perms.TEMP_MUTE)) {
                    player.sendMessage(Utils.color("&cYou lack the permission &7(" + Perms.TEMP_MUTE + ")"));
                    return true;
                }
    
                long expire = extractExpire(player, currentTime, args[1]);
    
                if (!(args.length > 2)) {
                    player.sendMessage(Utils.color("&cYou must supply a reason for the punishment."));
                    return true;
                }
    
                String reason = getReason(2, args);
                MutePunishment punishment = new TemporaryMute(prefix, player.getUniqueId(), info.getUuid(), reason, currentTime, visibility, expire);
                plugin.getDataManager().addMute(punishment);
                punishment.executePunishment();
                checkTraining(punishment, ignoreTraining);
            } else if (cmd.getName().equalsIgnoreCase("kick")) {
                if (!player.hasPermission(Perms.KICK)) {
                    player.sendMessage(Utils.color("&cYou lack the permission &7(" + Perms.KICK + ")"));
                    return true;
                }
                
                String reason = getReason(1, args);
                if (reason.equals("")) {
                    player.sendMessage(Utils.color("&cYou must supply a reason for the punishment."));
                    return true;
                }
                KickPunishment punishment = new KickPunishment(prefix, player.getUniqueId(), info.getUuid(), reason, currentTime, visibility);
                plugin.getDataManager().addKick(punishment);
                punishment.executePunishment();
                checkTraining(punishment, ignoreTraining);
            } else if (cmd.getName().equalsIgnoreCase("warn")) {
                if (!player.hasPermission(Perms.WARN)) {
                    player.sendMessage(Utils.color("&cYou lack the permission &7(" + Perms.WARN + ")"));
                    return true;
                }
                
                String reason = getReason(1, args);
                if (reason.equals("")) {
                    player.sendMessage(Utils.color("&cYou must supply a reason for the punishment."));
                    return true;
                }
                WarnPunishment punishment = new WarnPunishment(prefix, player.getUniqueId(), info.getUuid(), reason, currentTime, visibility);
                plugin.getDataManager().addWarning(punishment);
                punishment.executePunishment();
                checkTraining(punishment, ignoreTraining);
            } else if (cmd.getName().equalsIgnoreCase("jail")) {
                if (!player.hasPermission(Perms.JAIL)) {
                    player.sendMessage(Utils.color("&cYou lack the permission &7(" + Perms.JAIL + ")"));
                    return true;
                }
                
                if (plugin.getDataManager().getPrisons().isEmpty()) {
                    player.sendMessage(Utils.color("&cThere are no prisons created. Jail punishments are disabled until at least 1 is created."));
                    return true;
                }
                
                String reason = getReason(1, args);
                if (reason.equals("")) {
                    player.sendMessage(Utils.color("&cYou must supply a reason."));
                    return true;
                }
                JailPunishment punishment = new JailPunishment(prefix, player.getUniqueId(), info.getUuid(), reason, currentTime, visibility, -1);
                plugin.getDataManager().addJailPunishment(punishment);
                punishment.executePunishment();
                checkTraining(punishment, ignoreTraining);
            }
        }
        return true;
    }
    
    private String getReason(int index, String[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = index; i < args.length; i++) {
            if (!args[i].startsWith("-")) {
                sb.append(args[i]);
                if (i != args.length - 1) {
                    sb.append(" ");
                }
            }
        }
        return sb.toString();
    }
    
    private void checkTraining(Punishment punishment, boolean ignoreTraining) {
        if (plugin.getDataManager().isTrainingMode()) {
            if (ignoreTraining) {
                punishment.setTrainingMode(false);
            }
        }
    }
    
    private long extractExpire(Player player, long currentTime, String s) {
        long expire;
        try {
            expire = Punishment.calculateExpireDate(currentTime, s);
        } catch (Exception e) {
            player.sendMessage(Utils.color("&cInvalid time format."));
            return -1;
        }
        return expire;
    }
}
