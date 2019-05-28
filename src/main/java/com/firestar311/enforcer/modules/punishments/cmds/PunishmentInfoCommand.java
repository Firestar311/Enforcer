package com.firestar311.enforcer.modules.punishments.cmds;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.modules.punishments.type.abstraction.Punishment;
import com.firestar311.enforcer.util.Messages;
import com.firestar311.lib.util.Utils;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class PunishmentInfoCommand implements CommandExecutor {

    private Enforcer plugin;
    
    public PunishmentInfoCommand(Enforcer plugin){
        this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.color("&cOnly players may use that command"));
            return true;
        }
        
        Player player = ((Player) sender);
        
        if (!player.hasPermission("enforcer.info.punishment")) {
            player.sendMessage(Messages.noPermissionCommand("enforcer.info.punishment"));
            return true;
        }
        
        if (!(args.length > 0)) {
            player.sendMessage(Utils.color("&cYou do not have enough arguments."));
            return true;
        }
    
        int id;
        try {
            id = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(Utils.color("&cThe value for the id is not a valid number."));
            return true;
        }
    
        Punishment punishment = plugin.getPunishmentManager().getPunishment(id);
        
        if (punishment == null) {
            player.sendMessage(Utils.color("&cCould not find a punishment with that id."));
            return true;
        }
        
        
        return true;
    }
}