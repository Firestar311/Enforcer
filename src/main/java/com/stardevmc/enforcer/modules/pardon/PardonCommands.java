package com.stardevmc.enforcer.modules.pardon;

import com.firestar311.lib.player.PlayerInfo;
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
            sender.sendMessage(Utils.color("&cOnly console or players may use that command."));
            return true;
        }
        
        PlayerInfo info = plugin.getPlayerManager().getPlayerInfo(args[0]);
        if (info == null) {
            sender.sendMessage(Utils.color("&cCould not find a player by that name."));
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
        }
        
        Set<Punishment> punishments = new HashSet<>();
        if (cmd.getName().equalsIgnoreCase("unban")) {
            if (!sender.hasPermission(Perms.UNBAN)) {
                sender.sendMessage(Messages.noPermissionCommand(Perms.UNBAN));
                return true;
            }
            punishments.addAll(plugin.getPunishmentModule().getManager().getActiveBans(info.getUuid()));
            if (punishments.isEmpty()) {
                sender.sendMessage(Utils.color("&cThere are no active bans against that player."));
                return true;
            }
        } else if (cmd.getName().equalsIgnoreCase("unmute")) {
            if (!sender.hasPermission(Perms.UNMUTE)) {
                sender.sendMessage(Messages.noPermissionCommand(Perms.UNMUTE));
                return true;
            }
            punishments.addAll(plugin.getPunishmentModule().getManager().getActiveMutes(info.getUuid()));
            if (punishments.isEmpty()) {
                sender.sendMessage(Utils.color("&cThere are no active mutes against that player."));
                return true;
            }
        } else if (cmd.getName().equalsIgnoreCase("unjail")) {
            if (!sender.hasPermission(Perms.UNJAIL)) {
                sender.sendMessage(Messages.noPermissionCommand(Perms.UNJAIL));
                return true;
            }
            punishments.addAll(plugin.getPunishmentModule().getManager().getActiveJails(info.getUuid()));
            if (punishments.isEmpty()) {
                sender.sendMessage(Utils.color("&cThere are no active jails against that player."));
                return true;
            }
        } else if (cmd.getName().equalsIgnoreCase("pardon")) {
            if (!sender.hasPermission(Perms.PARDON)) {
                sender.sendMessage(Messages.noPermissionCommand(Perms.PARDON));
                return true;
            }
            
            punishments.addAll(plugin.getPunishmentModule().getManager().getActivePunishments(info.getUuid()));
            if (punishments.isEmpty()) {
                sender.sendMessage(Utils.color("&cThere are no active punishments against that player."));
                return true;
            }
        } else if (cmd.getName().equalsIgnoreCase("unblacklist")) {
            if (!sender.hasPermission(Perms.UNBLACKLIST)) {
                sender.sendMessage(Messages.noPermissionCommand(Perms.UNBLACKLIST));
                return true;
            }
            
            punishments.addAll(plugin.getPunishmentModule().getManager().getActiveBlacklists(info.getUuid()));
            if (punishments.isEmpty()) {
                sender.sendMessage(Utils.color("&cThere are no active blacklists against that player."));
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