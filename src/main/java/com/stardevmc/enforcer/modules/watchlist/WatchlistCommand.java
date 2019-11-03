package com.stardevmc.enforcer.modules.watchlist;

import com.firestar311.lib.pagination.*;
import com.firestar311.lib.player.PlayerInfo;
import com.firestar311.lib.util.Utils;
import com.stardevmc.enforcer.Enforcer;
import com.stardevmc.enforcer.modules.base.Priority;
import com.stardevmc.enforcer.util.Messages;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class WatchlistCommand implements CommandExecutor {
    
    private Enforcer plugin = Enforcer.getInstance();
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!Utils.isPlayer(sender)) {
            sender.sendMessage("&cOnly players may use that command.");
            return true;
        }
    
        Player player = ((Player) sender);
    
        if (cmd.getName().equalsIgnoreCase("watchlist")) {
            if (!(args.length > 0)) {
                player.sendMessage(Utils.color("&cYou must provide a sub command."));
                return true;
            }
            
            WatchlistManager watchlistManager = plugin.getWatchlistModule().getManager();
            
            if (Utils.checkCmdAliases(args, 0, "list", "l")) {
                player.sendMessage(Utils.color("&6Watchlist Information"));
                player.sendMessage(Utils.color("&aPrimary Focus: &e" + Utils.convertUUIDToName(watchlistManager.getPrimaryFocus(player.getUniqueId()), "&cNone")));
                Paginator<WatchlistEntry> paginator = PaginatorFactory.generatePaginator(7, watchlistManager.getEntries(), new HashMap() {{
                        put(DefaultVariables.COMMAND, label + " list ");
                        put(DefaultVariables.TYPE, "Watchlist Entries");
                    }});
                if (args.length == 2) {
                    paginator.display(player, args[1]);
                } else {
                    paginator.display(player, 1);
                }
            } else if (Utils.checkCmdAliases(args, 0, "add", "a")) {
                if (!(args.length > 1)) {
                    player.sendMessage(Utils.color("&cYou must provide a name and a reason."));
                    return true;
                }
                
                PlayerInfo playerInfo = plugin.getPlayerManager().getPlayerInfo(args[1]);
                if (playerInfo == null) {
                    player.sendMessage(Utils.color("&cThe name you provided is not a valid player."));
                    return true;
                }
                
                String reason = StringUtils.join(args, " ", 2, args.length);
                if (StringUtils.isEmpty(reason)) {
                    player.sendMessage(Utils.color("&cYou must provide a reason."));
                    return true;
                }
                
                if (watchlistManager.isWatchedPlayer(playerInfo.getUuid())) {
                    player.sendMessage(Utils.color("&cThat player is already on the watchlist."));
                    return true;
                }
                
                watchlistManager.addEntry(new WatchlistEntry(playerInfo.getUuid(), player.getUniqueId(), reason));
                Messages.sendNotifyMessage(Messages.watchlistMessage(playerInfo.getLastName(), player.getName(), "added"));
            } else if (Utils.checkCmdAliases(args, 0, "remove", "r")) {
                if (!(args.length > 1)) {
                    player.sendMessage(Utils.color("&cYou must provide a name."));
                    return true;
                }
                
                PlayerInfo info = plugin.getPlayerManager().getPlayerInfo(args[1]);
                if (info == null) {
                    player.sendMessage(Utils.color("&cCould not find a player with that name."));
                    return true;
                }
                
                WatchlistEntry entry = plugin.getWatchlistModule().getManager().getEntry(info.getUuid());
                if (entry == null) {
                    player.sendMessage(Utils.color("&cCould not find that player on the watchlist."));
                    return true;
                }
                plugin.getWatchlistModule().getManager().removeEntry(entry);
                Messages.sendNotifyMessage(Messages.watchlistMessage(info.getLastName(), player.getName(), "removed"));
            } else if (Utils.checkCmdAliases(args, 0, "addnote", "an")) {
                if (!(args.length > 1)) {
                    player.sendMessage(Utils.color("&cYou must provide a name."));
                    return true;
                }
    
                PlayerInfo info = plugin.getPlayerManager().getPlayerInfo(args[1]);
                if (info == null) {
                    player.sendMessage(Utils.color("&cCould not find a player with that name."));
                    return true;
                }
                
                WatchlistEntry entry = plugin.getWatchlistModule().getManager().getEntry(info.getUuid());
                if (entry == null) {
                    player.sendMessage(Utils.color("&cCould not find that player on the watchlist."));
                    return true;
                }
                
                if (!(args.length > 2)) {
                    player.sendMessage(Utils.color("&cYou must provide a message."));
                    return true;
                }
                
                String text = StringUtils.join(args, " ", 2, args.length);
                if (StringUtils.isEmpty(text)) {
                    player.sendMessage(Utils.color("&cYou must provide a message."));
                    return true;
                }
                
                entry.addNote(new WatchlistNote(player.getUniqueId(), text));
                Messages.sendNotifyMessage(Messages.watchlistAddNoteMessage(info.getLastName(), player.getName()));
            } else if (Utils.checkCmdAliases(args, 0, "setpriority", "sp")) {
                if (!(args.length > 1)) {
                    player.sendMessage(Utils.color("&cYou must provide a name."));
                    return true;
                }
    
                PlayerInfo info = plugin.getPlayerManager().getPlayerInfo(args[1]);
                if (info == null) {
                    player.sendMessage(Utils.color("&cCould not find a player with that name."));
                    return true;
                }
    
                WatchlistEntry entry = plugin.getWatchlistModule().getManager().getEntry(info.getUuid());
                if (entry == null) {
                    player.sendMessage(Utils.color("&cCould not find that player on the watchlist."));
                    return true;
                }
    
                if (!(args.length > 2)) {
                    player.sendMessage(Utils.color("&cYou must provide a priority."));
                    return true;
                }
    
                Priority priority;
                try {
                    priority = Priority.valueOf(args[2].toUpperCase());
                } catch (IllegalArgumentException e) {
                    player.sendMessage(Utils.color("&cInvalid priority value."));
                    return true;
                }
                
                if (entry.getPriority() == priority) {
                    player.sendMessage(Utils.color("&cThe priority you provided is the same as current priority."));
                    return true;
                }
    
                entry.setPriority(priority);
                Messages.sendNotifyMessage(Messages.watchlistSetPriority(info.getLastName(), player.getName(), priority));
            } else if (Utils.checkCmdAliases(args, 0, "setfocus", "sf")) {
                if (!(args.length > 0)) {
                    player.sendMessage(Utils.color("&cYou must provide a player name."));
                    return true;
                }
    
                PlayerInfo info = plugin.getPlayerManager().getPlayerInfo(args[1]);
                if (info == null) {
                    player.sendMessage(Utils.color("&cCould not find a player with that name."));
                    return true;
                }
                
                plugin.getWatchlistModule().getManager().setPrimaryFocus(player.getUniqueId(), info.getUuid());
                player.sendMessage(Utils.color("&aSet your focus to " + info.getLastName()));
            } else if (Utils.checkCmdAliases(args, 0, "clearfocus", "cf")) {
                UUID focus = watchlistManager.getPrimaryFocus(player.getUniqueId());
                if (focus == null) {
                    player.sendMessage(Utils.color("&cYou do not have a primary focus."));
                    return true;
                }
                
                watchlistManager.clearFocus(player.getUniqueId());
            }
        } else if (cmd.getName().equalsIgnoreCase("quickteleport")) {
            UUID primaryFocus = plugin.getWatchlistModule().getManager().getPrimaryFocus(player.getUniqueId());
            if (primaryFocus == null) {
                player.sendMessage(Utils.color("&cYou do not have a primary focus."));
                return true;
            }
            
            Player focus = Bukkit.getPlayer(primaryFocus);
            if (focus == null) {
                player.sendMessage(Utils.color("&cYour primary focus is offline."));
                return true;
            }
            
            player.teleport(focus);
            player.sendMessage(Utils.color("&aTeleported you to your primary focus."));
        }
        
        return true;
    }
}