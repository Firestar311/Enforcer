package com.firestar311.enforcer.command;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.util.*;
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
            plugin.getDataManager().setUsingDisplayNames(!plugin.getDataManager().isUsingDisplayNames());
            String message = Messages.USING_DISPLAYNAMES;
            message = message.replace(Variables.DISPLAY, plugin.getDataManager().isUsingDisplayNames() + "");
            sendOutputMessage(player, message);
        } else if (Utils.checkCmdAliases(args, 0, "trainingmode", "tm")) {
            if (!player.hasPermission(Perms.TRAINING_MODE)) {
                player.sendMessage(Utils.color("&cYou do not have permission to toggle training mode."));
                return true;
            }
    
            plugin.getDataManager().setTrainingMode(!plugin.getDataManager().isTrainingMode());
            String message = Messages.TRAINING_MODE;
            
            message = message.replace(Variables.DISPLAY, plugin.getDataManager().isTrainingMode() + "");
            sendOutputMessage(player, message);
        }
        return true;
    }
    
    private void sendOutputMessage(Player player, String message) {
        Messages.sendOutputMessage(player, message, plugin);
    }
}