package com.stardevmc.enforcer.modules.watchlist;

import com.stardevmc.enforcer.Enforcer;
import org.bukkit.command.*;

public class WatchlistCommand implements CommandExecutor {
    
    private Enforcer plugin = Enforcer.getInstance();
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return true;
    }
}