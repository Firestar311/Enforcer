package com.stardevmc.enforcer.modules.pardon;

import com.firestar311.lib.player.User;
import com.firestar311.lib.util.Utils;
import com.stardevmc.enforcer.Enforcer;
import com.stardevmc.enforcer.modules.punishments.Visibility;
import com.stardevmc.enforcer.modules.punishments.actor.*;
import com.stardevmc.enforcer.modules.punishments.type.abstraction.Punishment;
import com.stardevmc.enforcer.util.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class PardonCommands implements CommandExecutor {
    
    private Enforcer plugin;
    
    public PardonCommands(Enforcer plugin) {
        this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Actor actor;
        if (sender instanceof ConsoleCommandSender) {
            actor = new ConsoleActor();
        } else if (sender instanceof Player) {
            actor = new PlayerActor(((Player) sender).getUniqueId());
        } else {
            sender.sendMessage(Utils.color(Messages.ONLY_PLAYERS_AND_CONSOLE_CMD));
            return true;
        }
        
        User info = plugin.getPlayerManager().getUser(args[0]);
        if (info == null) {
            sender.sendMessage(Utils.color(Messages.COULD_NOT_FIND_PLAYER));
            return true;
        }
        
        Visibility visibility = Visibility.NORMAL;
        EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);
    
        for (String arg : args) {
            Flag flag = Flag.matchFlag(arg);
            if (flag != null) {
                flags.add(flag);
            }
        }
    
        for (Flag flag : flags) {
            if (flag == Flag.PUBLIC) {
                visibility = Visibility.PUBLIC;
            }
            if (flag == Flag.SILENT) {
                visibility = Visibility.SILENT;
            }
            if (flag == Flag.NORMAL) {
                visibility = Visibility.NORMAL;
            }
        }
        
        Set<Punishment> punishments = new HashSet<>();
        if (cmd.getName().equalsIgnoreCase("unban")) {
            if (!sender.hasPermission(Perms.UNBAN)) {
                sender.sendMessage(Messages.noPermissionCommand(Perms.UNBAN));
                return true;
            }
            punishments.addAll(plugin.getPunishmentModule().getManager().getActiveBans(info.getUniqueId()));
            if (punishments.isEmpty()) {
                sender.sendMessage(Utils.color(Messages.noActivePunishment("bans")));
                return true;
            }
        } else if (cmd.getName().equalsIgnoreCase("unmute")) {
            if (!sender.hasPermission(Perms.UNMUTE)) {
                sender.sendMessage(Messages.noPermissionCommand(Perms.UNMUTE));
                return true;
            }
            punishments.addAll(plugin.getPunishmentModule().getManager().getActiveMutes(info.getUniqueId()));
            if (punishments.isEmpty()) {
                sender.sendMessage(Utils.color(Messages.noActivePunishment("mutes")));
                return true;
            }
        } else if (cmd.getName().equalsIgnoreCase("unjail")) {
            if (!sender.hasPermission(Perms.UNJAIL)) {
                sender.sendMessage(Messages.noPermissionCommand(Perms.UNJAIL));
                return true;
            }
            punishments.addAll(plugin.getPunishmentModule().getManager().getActiveJails(info.getUniqueId()));
            if (punishments.isEmpty()) {
                sender.sendMessage(Utils.color(Messages.noActivePunishment("jails")));
                return true;
            }
        } else if (cmd.getName().equalsIgnoreCase("pardon")) {
            if (!sender.hasPermission(Perms.PARDON)) {
                sender.sendMessage(Messages.noPermissionCommand(Perms.PARDON));
                return true;
            }
            
            punishments.addAll(plugin.getPunishmentModule().getManager().getActivePunishments(info.getUniqueId()));
            if (punishments.isEmpty()) {
                sender.sendMessage(Utils.color(Messages.noActivePunishment("punishments")));
                return true;
            }
        } else if (cmd.getName().equalsIgnoreCase("unblacklist")) {
            if (!sender.hasPermission(Perms.UNBLACKLIST)) {
                sender.sendMessage(Messages.noPermissionCommand(Perms.UNBLACKLIST));
                return true;
            }
            
            punishments.addAll(plugin.getPunishmentModule().getManager().getActiveBlacklists(info.getUniqueId()));
            if (punishments.isEmpty()) {
                sender.sendMessage(Utils.color(Messages.noActivePunishment("blacklists")));
                return true;
            }
        }
        for (Punishment punishment : punishments) {
            if (plugin.getTrainingModule().getManager().isTrainingMode(punishment.getPunisher())) {
                if (!punishment.isTrainingPunishment()) {
                    continue;
                }
            }
            punishment.setPardonVisibility(visibility);
            punishment.reversePunishment(actor, System.currentTimeMillis());
        }
    
        return true;
    }
}