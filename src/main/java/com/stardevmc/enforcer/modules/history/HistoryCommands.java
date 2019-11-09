package com.stardevmc.enforcer.modules.history;

import com.stardevmc.enforcer.Enforcer;
import com.stardevmc.enforcer.modules.punishments.type.abstraction.Punishment;
import com.stardevmc.enforcer.util.Messages;
import com.stardevmc.enforcer.util.Perms;
import com.firestar311.lib.pagination.Paginator;
import com.firestar311.lib.player.PlayerInfo;
import com.firestar311.lib.util.Utils;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class HistoryCommands implements CommandExecutor {
    
    private Enforcer plugin;
    
    public HistoryCommands(Enforcer plugin) {
        this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.color(Messages.ONLY_PLAYERS_CMD));
            return true;
        }
        
        Player player = ((Player) sender);
        
        if (!(args.length > 0)) {
            player.sendMessage(Utils.color(Messages.NO_NAME_PROVIDED));
            return true;
        }
    
        HistoryManager historyManager = plugin.getHistoryModule().getManager();
        
        if (cmd.getName().equalsIgnoreCase("history")) {
            if (!player.hasPermission(Perms.PLAYER_HISTORY)) {
                player.sendMessage(Messages.noPermissionCommand(Perms.PLAYER_HISTORY));
                return true;
            }
            
            
            if (args.length == 1) {
                Paginator<Punishment> paginator = historyManager.generateHistoryPaginator(player.getUniqueId(), args[0]);
                if (paginator == null) {
                    player.sendMessage(Utils.color(Messages.ERROR_LIST_OF_RESULTS));
                    return true;
                }
                
                paginator.display(player, 1, "history");
            } else if (args.length == 2) {
                if (Utils.checkCmdAliases(args, 0, "page", "p")) {
                    
                    int page = getPage(player, args[1]);
                    if (page == -1) return true;
            
                    if (!historyManager.hasLookupRegularHistory(player.getUniqueId())) {
                        player.sendMessage(Utils.color(Messages.NO_HISTORY_RESULTS));
                        return true;
                    }
            
                    historyManager.getRegularResults(player.getUniqueId()).display(player, page, "history");
                }
            }
        } else if (cmd.getName().equalsIgnoreCase("staffhistory")) {
            if (!player.hasPermission(Perms.STAFF_HISTORY)) {
                player.sendMessage(Messages.noPermissionCommand(Perms.STAFF_HISTORY));
                return true;
            }
            if (args.length == 1) {
                Paginator<Punishment> paginator = historyManager.generateStaffHistoryPaginator(player.getUniqueId());
                if (paginator == null) {
                    player.sendMessage(Utils.color(Messages.ERROR_LIST_OF_RESULTS));
                    return true;
                }
    
                paginator.display(player, 1, "staffhistory");
            } else if (args.length == 2) {
                if (Utils.checkCmdAliases(args, 0, "page", "p")) {
                    
                    int page = getPage(player, args[1]);
                    if (page == -1) return true;
            
                    if (!historyManager.hasLookupStaffHistory(player.getUniqueId())) {
                        player.sendMessage(Utils.color(Messages.NO_STAFF_RESULTS));
                        return true;
                    }
            
                    historyManager.getStaffResults(player.getUniqueId()).display(player, page, "staffhistory");
                }
        
            }
        }
        
        return true;
    }
    
    private int getPage(Player player, String stringPage) {
        try {
            return Integer.parseInt(stringPage);
        } catch (NumberFormatException e) {
            player.sendMessage(Utils.color(Messages.INVALID_NUMBER));
            return -1;
        }
    }
    
    private PlayerInfo getPlayerInfo(String string, Player player) {
        PlayerInfo info = plugin.getPlayerManager().getPlayerInfo(string);
        if (info == null) {
            player.sendMessage(Utils.color(Messages.PLAYER_NEVER_JOINED));
            return null;
        }
        return info;
    }
}
