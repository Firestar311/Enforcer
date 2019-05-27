package com.firestar311.enforcer;

import com.firestar311.enforcer.util.*;
import com.firestar311.lib.player.PlayerInfo;
import com.firestar311.lib.player.PlayerManager;
import com.firestar311.lib.util.Utils;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class EnforcerCommand implements CommandExecutor {
    
    private Enforcer plugin;
    
    public EnforcerCommand(Enforcer plugin) {
        this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.color("&cOnly players may use that command."));
            return true;
        }
        
        Player player = ((Player) sender);
        
        if (!player.hasPermission(Perms.ENFORCER_ADMIN)) {
            player.sendMessage(Utils.color("&cInsufficient permission"));
            return true;
        }
        
        if (args.length == 0) {
            player.sendMessage(Utils.color("&aEnforcer Information"));
            player.sendMessage(Utils.color("&7Version: &e" + plugin.getDescription().getVersion()));
            player.sendMessage(Utils.color("&7Author: &eFirestar311"));
            player.sendMessage(Utils.color("&7---Settings---"));
            player.sendMessage(Utils.color("&7Using Display Names: &e" + plugin.getSettingsManager().isUsingDisplayNames()));
            player.sendMessage(Utils.color("&7Must Confirm Punishments: &e" + plugin.getSettingsManager().mustConfirmPunishments()));
            player.sendMessage(Utils.color("&7Prefix: &e" + plugin.getSettingsManager().getPrefix()));
            player.sendMessage(Utils.color("&7Server Name : &e" + plugin.getSettingsManager().getServerName()));
            return true;
        }
        
        if (Utils.checkCmdAliases(args, 0, "settings", "s")) {
            if (Utils.checkCmdAliases(args, 1, "toggledisplaynames", "tdn")) {
                if (!player.hasPermission(Perms.SETTINGS_DISPLAYNAMES)) {
                    player.sendMessage(Utils.color("&cYou do not have permission to toggle display names."));
                    return true;
                }
                plugin.getSettingsManager().setUsingDisplayNames(!plugin.getSettingsManager().isUsingDisplayNames());
                String message = Messages.USING_DISPLAYNAMES;
                message = message.replace(Variables.DISPLAY, plugin.getSettingsManager().isUsingDisplayNames() + "");
                sendOutputMessage(player, message);
            } else if (Utils.checkCmdAliases(args, 1, "trainingmode", "tm")) {
                if (!player.hasPermission(Perms.SETTINGS_TRAINING_MODE)) {
                    player.sendMessage(Utils.color("&cYou do not have permission to toggle training mode."));
                    return true;
                }
        
                if (args.length > 1) {
                    if (Utils.checkCmdAliases(args, 2, "global", "g")) {
                        if (!player.hasPermission(Perms.SETTINGS_TRAINING_MODE_GLOBAL)) {
                            player.sendMessage(Utils.color("&cYou cannot change global training mode status."));
                            return true;
                        }
                        plugin.getTrainingModeManager().setGlobalTrainingMode(!plugin.getTrainingModeManager().getGlobalTrainingMode());
                        String message = Messages.TRAINING_MODE_GLOBAL;
                
                        message = message.replace(Variables.DISPLAY, plugin.getTrainingModeManager().getGlobalTrainingMode() + "");
                        sendOutputMessage(player, message);
                    } else {
                        if (!player.hasPermission(Perms.SETTINGS_TRAINING_MODE_INDIVIDUAL)) {
                            player.sendMessage("&cYou cannot change the training mode for individual players");
                            return true;
                        }
                        if (args.length > 2) {
                            PlayerInfo target = plugin.getServer().getServicesManager().getRegistration(PlayerManager.class).getProvider().getPlayerInfo(args[2]);
                            if (target != null) {
                                boolean var = plugin.getTrainingModeManager().toggleTrainingMode(target.getUuid());
                                String message = Messages.TRAINING_MODE_INDIVIDUAL;
                        
                                message = message.replace(Variables.DISPLAY, var + "");
                                message = message.replace(Variables.TARGET, target.getLastName());
                                sendOutputMessage(player, message);
                            } else {
                                player.sendMessage(Utils.color("&cThe target you provided is invalid."));
                            }
                        }
                    }
                }
            } else if (Utils.checkCmdAliases(args, 1, "confirmpunishments", "cp")) {
                if (!player.hasPermission(Perms.SETTINGS_CONFIRM_PUNISHMENTS)) {
                    player.sendMessage(Utils.color("&cYou cannot change the confirm punishments setting."));
                    return true;
                }
                plugin.getSettingsManager().setConfirmPunishments(!plugin.getSettingsManager().mustConfirmPunishments());
                String message = Messages.SETTING_CONFIRMPUNISHMENTS;
                message = message.replace(Variables.DISPLAY, plugin.getSettingsManager().mustConfirmPunishments() + "");
                sendOutputMessage(player, message);
            } else if (Utils.checkCmdAliases(args, 1, "prefix")) {
                if (!player.hasPermission(Perms.SETTINGS_PREFIX)) {
                    player.sendMessage(Utils.color("&cYou cannot change the prefix."));
                    return true;
                }
                
                if (!(args.length > 0)) {
                    player.sendMessage(Utils.color("&cYou must provide a prefix to set."));
                    return true;
                }
                
                plugin.getSettingsManager().setPrefix(args[2]);
                player.sendMessage(Utils.color("&aYou set the prefix to " + plugin.getSettingsManager().getPrefix()));
            } else if (Utils.checkCmdAliases(args, 1, "server")) {
                if (!player.hasPermission(Perms.SETTINGS_SERVER)) {
                    player.sendMessage(Utils.color("&cYou cannot change the server."));
                    return true;
                }
    
                if (!(args.length > 0)) {
                    player.sendMessage(Utils.color("&cYou must provide a server name to set."));
                    return true;
                }
    
                plugin.getSettingsManager().setServerName(args[2]);
                player.sendMessage(Utils.color("&aYou set the server name to " + plugin.getSettingsManager().getServerName()));
            }
        }
        return true;
    }
    
    private void sendOutputMessage(Player player, String message) {
        Messages.sendOutputMessage(player, message, plugin);
    }
}