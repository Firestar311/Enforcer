package com.stardevmc.enforcer.modules.base;

import com.firestar311.lib.util.Utils;
import org.bukkit.command.*;

public class NotEnabledCommand implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        sender.sendMessage(Utils.color("&cWe are sorry but that command is not enabled."));
        return false;
    }
}