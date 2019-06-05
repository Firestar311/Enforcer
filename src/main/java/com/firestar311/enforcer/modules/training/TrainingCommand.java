package com.firestar311.enforcer.modules.training;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.util.*;
import com.firestar311.lib.player.PlayerInfo;
import com.firestar311.lib.util.Utils;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class TrainingCommand implements CommandExecutor {
    
    private Enforcer plugin;
    
    public TrainingCommand(Enforcer plugin) {
        this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.color("&cOnly players may use that command."));
            return true;
        }
        
        Player player = ((Player) sender);
        
        TrainingManager trainingManager = plugin.getTrainingModule().getManager();
    
        if (!player.hasPermission(Perms.SETTINGS_TRAINING_MODE)) {
            player.sendMessage(Utils.color("&cYou do not have permission to toggle training mode."));
            return true;
        }
    
            if (Utils.checkCmdAliases(args, 0, "global", "g")) {
                if (!player.hasPermission(Perms.SETTINGS_TRAINING_MODE_GLOBAL)) {
                    player.sendMessage(Utils.color("&cYou cannot change global training mode status."));
                    return true;
                }
                trainingManager.setGlobalTrainingMode(!trainingManager.getGlobalTrainingMode());
                String message = Messages.TRAINING_MODE_GLOBAL;
            
                message = message.replace(Variables.DISPLAY, trainingManager.getGlobalTrainingMode() + "");
                Messages.sendOutputMessage(player, message, plugin);
            } else {
                if (!player.hasPermission(Perms.SETTINGS_TRAINING_MODE_INDIVIDUAL)) {
                    player.sendMessage("&cYou cannot change the training mode for individual players");
                    return true;
                }
                if (args.length > 1) {
                    PlayerInfo target = plugin.getPlayerManager().getPlayerInfo(args[2]);
                    if (target != null) {
                        boolean var = trainingManager.toggleTrainingMode(target.getUuid());
                        String message = Messages.TRAINING_MODE_INDIVIDUAL;
                    
                        message = message.replace(Variables.DISPLAY, var + "");
                        message = message.replace(Variables.TARGET, target.getLastName());
                        Messages.sendOutputMessage(player, message, plugin);
                    } else {
                        player.sendMessage(Utils.color("&cThe target you provided is invalid."));
                    }
                }
        }
    
        return true;
    }
}