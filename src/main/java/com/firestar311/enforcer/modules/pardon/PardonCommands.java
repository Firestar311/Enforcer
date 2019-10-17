package com.firestar311.enforcer.modules.pardon;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.modules.punishments.Visibility;
import com.firestar311.enforcer.modules.punishments.actor.PlayerActor;
import com.firestar311.enforcer.modules.punishments.type.abstraction.Punishment;
import com.firestar311.enforcer.util.Messages;
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
            sender.sendMessage(Utils.color("&cOnly players may use pardon commands."));
            return true;
        }
        
        Player player = ((Player) sender);
        
        PlayerInfo info = plugin.getPlayerManager().getPlayerInfo(args[0]);
        if (info == null) {
            player.sendMessage(Utils.color("&cCould not find a player by that name."));
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
                player.sendMessage(Messages.noPermissionCommand(Perms.UNBAN));
                return true;
            }
            punishments.addAll(plugin.getPunishmentModule().getManager().getActiveBans(info.getUuid()));
            if (punishments.isEmpty()) {
                player.sendMessage(Utils.color("&cThere are no active bans against that player."));
                return true;
            }
        } else if (cmd.getName().equalsIgnoreCase("unmute")) {
            if (!player.hasPermission(Perms.UNMUTE)) {
                player.sendMessage(Messages.noPermissionCommand(Perms.UNMUTE));
                return true;
            }
            punishments.addAll(plugin.getPunishmentModule().getManager().getActiveMutes(info.getUuid()));
            if (punishments.isEmpty()) {
                player.sendMessage(Utils.color("&cThere are no active mutes against that player."));
                return true;
            }
        } else if (cmd.getName().equalsIgnoreCase("unjail")) {
            if (!player.hasPermission(Perms.UNJAIL)) {
                player.sendMessage(Messages.noPermissionCommand(Perms.UNJAIL));
                return true;
            }
            punishments.addAll(plugin.getPunishmentModule().getManager().getActiveJails(info.getUuid()));
            if (punishments.isEmpty()) {
                player.sendMessage(Utils.color("&cThere are no active jails against that player."));
                return true;
            }
        } else if (cmd.getName().equalsIgnoreCase("pardon")) {
            if (!player.hasPermission(Perms.PARDON)) {
                player.sendMessage(Messages.noPermissionCommand(Perms.PARDON));
                return true;
            }
            
            punishments.addAll(plugin.getPunishmentModule().getManager().getActivePunishments(info.getUuid()));
            if (punishments.isEmpty()) {
                player.sendMessage(Utils.color("&cThere are no active punishments against that player."));
                return true;
            }
        }
        Visibility finalVisibility = visibility;
        for (Punishment punishment : punishments) {
            if (plugin.getTrainingModule().getManager().isTrainingMode(punishment.getPunisher())) {
                if (!punishment.isTrainingPunishment()) {
                    continue;
                }
            }
            punishment.setPardonVisibility(finalVisibility);
            punishment.reversePunishment(new PlayerActor(player.getUniqueId()), System.currentTimeMillis()); //TODO Temporary
        }
    
        return true;
    }
}