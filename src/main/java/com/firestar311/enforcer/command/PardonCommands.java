package com.firestar311.enforcer.command;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.model.enums.Visibility;
import com.firestar311.enforcer.model.punishment.abstraction.Punishment;
import com.firestar311.enforcer.util.Perms;
import com.firestar311.lib.player.PlayerInfo;
import com.firestar311.lib.util.Utils;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class PardonCommands implements CommandExecutor {
    
    private Enforcer plugin;
    
    public PardonCommands(Enforcer plugin) {
        this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.color("&cOnly players may use punishment commands."));
            return true;
        }
        
        Player player = ((Player) sender);
        
        PlayerInfo info = plugin.getPlayerManager().getPlayerInfo(args[0]);
        if (info == null) {
            player.sendMessage(Utils.color("&cCould not find a player by that name. Pardoning players that have yet to join is not supported yet."));
            return true;
        }
        
        Visibility visibility = Visibility.NORMAL;
        for (String arg : args) {
            if (arg.equalsIgnoreCase("-p")) {
                visibility = Visibility.PUBLIC;
                break;
            }
            if (arg.equalsIgnoreCase("-s")) {
                visibility = Visibility.SILENT;
                break;
            }
        }
        
        Set<Punishment> punishments = new HashSet<>();
        if (cmd.getName().equalsIgnoreCase("unban")) {
            if (!player.hasPermission(Perms.UNBAN)) {
                player.sendMessage(Utils.color("&cYou lack the permission &7(" + Perms.UNBAN + ")"));
                return true;
            }
            punishments.addAll(plugin.getPunishmentManager().getActiveBans(info.getUuid()));
            if (punishments.isEmpty()) {
                player.sendMessage(Utils.color("&cThere are no active bans against that player."));
                return true;
            }
        } else if (cmd.getName().equalsIgnoreCase("unmute")) {
            if (!player.hasPermission(Perms.UNMUTE)) {
                player.sendMessage(Utils.color("&cYou lack the permission &7(" + Perms.UNMUTE + ")"));
                return true;
            }
            punishments.addAll(plugin.getPunishmentManager().getActiveMutes(info.getUuid()));
            if (punishments.isEmpty()) {
                player.sendMessage(Utils.color("&cThere are no active mutes against that player."));
                return true;
            }
        } else if (cmd.getName().equalsIgnoreCase("unjail")) {
            if (!player.hasPermission(Perms.UNJAIL)) {
                player.sendMessage(Utils.color("&cYou lack the permission &7(" + Perms.UNJAIL + ")"));
                return true;
            }
            punishments.addAll(plugin.getPunishmentManager().getActiveJails(info.getUuid()));
            if (punishments.isEmpty()) {
                player.sendMessage(Utils.color("&cThere are no active jails against that player."));
                return true;
            }
        } else if (cmd.getName().equalsIgnoreCase("pardon")) {
            if (!player.hasPermission(Perms.PARDON)) {
                player.sendMessage(Utils.color("&cYou lack the permission &7(" + Perms.UNJAIL + ")"));
                return true;
            }
            
            punishments.addAll(plugin.getPunishmentManager().getActivePunishments(info.getUuid()));
            if (punishments.isEmpty()) {
                player.sendMessage(Utils.color("&cThere are no active punishments against that player."));
                return true;
            }
        }
        Visibility finalVisibility = visibility;
        for (Punishment punishment : punishments) {
            if (plugin.getTrainingModeManager().isTrainingMode(punishment.getPunisher())) {
                if (!punishment.isTrainingPunishment()) {
                    continue;
                }
            }
            punishment.setPardonVisibility(finalVisibility);
            punishment.reversePunishment(player.getUniqueId(), System.currentTimeMillis());
        }
    
        return true;
    }
}