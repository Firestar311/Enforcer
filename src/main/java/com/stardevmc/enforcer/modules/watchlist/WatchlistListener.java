package com.stardevmc.enforcer.modules.watchlist;

import com.stardevmc.enforcer.modules.base.ModuleListener;
import com.stardevmc.enforcer.util.Messages;
import com.stardevmc.enforcer.util.Variables;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class WatchlistListener extends ModuleListener {
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = (Player) e.getPlayer();
        WatchlistManager watchlistManager = plugin.getWatchlistModule().getManager();
        if (watchlistManager.isWatchedPlayer(player.getUniqueId())) {
            WatchlistEntry entry = watchlistManager.getEntry(player.getUniqueId());
            Messages.sendNotifyMessage(Messages.WATCHLIST_PLAYER_JOIN.replace(Variables.TARGET, player.getName()).replace(Variables.REASON, entry.getReason()));
        }
    }
}