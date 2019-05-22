package com.firestar311.enforcer.command;

import com.firestar311.enforcer.Enforcer;
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
            //TODO Print out info later
            player.sendMessage(Utils.color("&cInvalid amount of arguments"));
            return true;
        }
        
        if (Utils.checkCmdAliases(args, 0, "toggledisplaynames", "tdn")) {
            if (!player.hasPermission(Perms.TOGGLE_DISPLAY_NAMES)) {
                player.sendMessage(Utils.color("&cYou do not have permission to toggle display names."));
                return true;
            }
            plugin.getSettingsManager().setUsingDisplayNames(!plugin.getSettingsManager().isUsingDisplayNames());
            String message = Messages.USING_DISPLAYNAMES;
            message = message.replace(Variables.DISPLAY, plugin.getSettingsManager().isUsingDisplayNames() + "");
            sendOutputMessage(player, message);
        } else if (Utils.checkCmdAliases(args, 0, "trainingmode", "tm")) {
            if (!player.hasPermission(Perms.TRAINING_MODE_GLOBAL)) {
                player.sendMessage(Utils.color("&cYou do not have permission to toggle training mode."));
                return true;
            }
            
            if (args.length > 1) {
                if (Utils.checkCmdAliases(args, 1, "global", "g")) {
                    if (!player.hasPermission(Perms.TRAINING_MODE_GLOBAL)) {
                        player.sendMessage(Utils.color("&cYou cannot change global training mode status."));
                        return true;
                    }
                    plugin.getTrainingModeManager()
                          .setGlobalTrainingMode(!plugin.getTrainingModeManager().getGlobalTrainingMode());
                    String message = Messages.TRAINING_MODE_GLOBAL;
                    
                    message = message
                            .replace(Variables.DISPLAY, plugin.getTrainingModeManager().getGlobalTrainingMode() + "");
                    sendOutputMessage(player, message);
                } else {
                    if (!player.hasPermission(Perms.TRAINING_MODE_INDIVIDUAL)) {
                        player.sendMessage("&cYou cannot change the training mode for individual players");
                        return true;
                    }
                    if (args.length > 2) {
                        PlayerInfo target = plugin.getServer().getServicesManager().getRegistration(PlayerManager.class)
                                                  .getProvider().getPlayerInfo(args[2]);
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
        }
        return true;
    }
    
    private void sendOutputMessage(Player player, String message) {
        Messages.sendOutputMessage(player, message, plugin);
    }
}