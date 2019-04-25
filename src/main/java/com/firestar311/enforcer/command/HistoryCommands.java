package com.firestar311.enforcer.command;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.model.punishment.abstraction.Punishment;
import com.firestar311.enforcer.util.EnforcerUtils;
import com.firestar311.enforcer.util.Perms;
import com.firestar311.lib.pagination.Paginator;
import com.firestar311.lib.player.PlayerInfo;
import com.firestar311.lib.util.Utils;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class HistoryCommands implements CommandExecutor {
    
    private Enforcer plugin;
    
    private Map<UUID, Paginator<Punishment>> historyPaginators = new HashMap<>();
    private Map<UUID, Paginator<Punishment>> staffHistoryPaginators = new HashMap<>();
    
    public HistoryCommands(Enforcer plugin) {
        this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.color("&cOnly players may use that command."));
            return true;
        }
        
        Player player = ((Player) sender);
        
        if (!(args.length > 0)) {
            player.sendMessage(Utils.color("&cYou must provide a name to view."));
            return true;
        }
        
        if (cmd.getName().equalsIgnoreCase("history")) {
            if (!player.hasPermission(Perms.PLAYER_HISTORY)) {
                player.sendMessage(Utils.color("&cYou do not have permission to view player history."));
                return true;
            }
            if (args.length == 1) {
                PlayerInfo info = getPlayerInfo(args[0], player);
                if (info == null) return true;
                UUID target = info.getUuid();
                List<Punishment> playerPunishments = new LinkedList<>();
                playerPunishments.addAll(plugin.getDataManager().getBans(target));
                playerPunishments.addAll(plugin.getDataManager().getMutes(target));
                playerPunishments.addAll(plugin.getDataManager().getJailPunishments(target));
                playerPunishments.addAll(plugin.getDataManager().getKicks(target));
                playerPunishments.addAll(plugin.getDataManager().getWarnings(target));
                Paginator<Punishment> paginator = EnforcerUtils.generatePaginatedPunishmentList(playerPunishments, "&7-=History of " + info.getLastName() + "=- &e({pagenumber}/{totalpages})", "&7Type /staffhistory page {nextpage} for more");
                paginator.display(player, 1, "history");
                this.historyPaginators.put(player.getUniqueId(), paginator);
            } else if (args.length == 2) {
                if (Utils.checkCmdAliases(args, 0, "page", "p")) {
                    
                    int page = getPage(player, args[1]);
                    if (page == -1) return true;
            
                    if (!this.historyPaginators.containsKey(player.getUniqueId())) {
                        player.sendMessage(Utils.color("&cYou do not have history results yet, please use /history <name> first"));
                        return true;
                    }
            
                    this.historyPaginators.get(player.getUniqueId()).display(player, page, "history");
                }
            }
        } else if (cmd.getName().equalsIgnoreCase("staffhistory")) {
            if (!player.hasPermission(Perms.STAFF_HISTORY)) {
                player.sendMessage(Utils.color("&cYou do not have permission to view staff history."));
                return true;
            }
            if (args.length == 1) {
                PlayerInfo info = getPlayerInfo(args[0], player);
                if (info == null) return true;
                
                List<Punishment> allPunishments = new ArrayList<>(plugin.getDataManager().getPunishments());
                List<Punishment> staffPunishments = new LinkedList<>();
                allPunishments.forEach(punishment -> {
                    if (punishment.getPunisher().equals(info.getUuid())) {
                        staffPunishments.add(punishment);
                    }
                });
                
                Paginator<Punishment> paginator = EnforcerUtils.generatePaginatedPunishmentList(staffPunishments, "&7-=Staff History of " + info.getLastName() + "=- &e({pagenumber}/{totalpages})", "&7Type /staffhistory page {nextpage} for more");
                paginator.display(player, 1, "staffhistory");
                this.staffHistoryPaginators.put(player.getUniqueId(), paginator);
            } else if (args.length == 2) {
                if (Utils.checkCmdAliases(args, 0, "page", "p")) {
                    
                    int page = getPage(player, args[1]);
                    if (page == -1) return true;
            
                    if (!this.staffHistoryPaginators.containsKey(player.getUniqueId())) {
                        player.sendMessage(Utils.color("&cYou do not have staff history results yet, please use /staffhistory <name> first"));
                        return true;
                    }
            
                    this.staffHistoryPaginators.get(player.getUniqueId()).display(player, page, "staffhistory");
                }
        
            }
        }
        
        return true;
    }
    
    private int getPage(Player player, String stringPage) {
        try {
            return Integer.parseInt(stringPage);
        } catch (NumberFormatException e) {
            player.sendMessage(Utils.color("&cYou provided an invalid number."));
            return -1;
        }
    }
    
    private PlayerInfo getPlayerInfo(String string, Player player) {
        PlayerInfo info = plugin.getDataManager().getInfo(string);
        if (info == null) {
            player.sendMessage(Utils.color("&cThat player has never joined the server, they do not have a history."));
            return null;
        }
        return info;
    }
}
