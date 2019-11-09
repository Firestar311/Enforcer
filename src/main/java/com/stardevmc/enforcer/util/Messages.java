package com.stardevmc.enforcer.util;

import com.firestar311.lib.util.Utils;
import com.stardevmc.enforcer.Enforcer;
import com.stardevmc.enforcer.modules.base.Priority;
import com.stardevmc.enforcer.modules.punishments.Colors;
import com.stardevmc.enforcer.modules.punishments.type.abstraction.BanPunishment;
import com.stardevmc.enforcer.modules.punishments.type.abstraction.Punishment;
import com.stardevmc.enforcer.modules.punishments.type.impl.KickPunishment;
import com.stardevmc.enforcer.modules.punishments.type.interfaces.Expireable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static com.stardevmc.enforcer.util.Variables.*;

public final class Messages {
    public static final String CLEAR_SELECTION = "&aCleared your selection.";
    public static final String CREATE_PRISON_OVERFLOW = "&dYou were an overflow inhabitant of your former prison, so you were moved to a newly created prison.";
    public static final String IGNORE_PUNISH_CONFIRMATION = "&2You are ignoring the punishment confirmation for this punishment.";
    public static final String INVALID_NUMBER = "&cYou provided an invalid number.";
    public static final String INVALID_PRISON_AMOUNT = "&cYou provided an invalid number for the max players, defaulting to 5";
    public static final String INVALID_PRISON_ID = "&cYou provided an invalid number for the prison id. The id will be auto-assigned.";
    public static final String JAIL_NO_PRISONS = "&cThere are no prisons created. Jail punishments are disabled until at least 1 is created.";
    public static final String LOCATION_NOT_IN_PRISON = "&cThat location is not within the prison bounds.";
    public static final String MAX_AMOUNT_CHANGED_MOVED = "&dThe prison you were in had its max players changed to a lower amount, so you were moved to a new prison.";
    public static final String MAX_AMOUNT_SAME = "&cThe amount you provided is the same as the current max players value.";
    public static final String MOVE_PRISON_REMOVED = "&cThe prison you were a part of was removed, you have been moved to a new prison.";
    public static final String NO_REASON = "&cYou must supply a reason for the punishment.";
    private static final String NO_ACTIVE_PUNISHMENT = "&cThere are no active <type> against that player.";
    public static final String NO_HISTORY_RESULTS = "&cYou do not have history results yet, please use /history <name> first.";
    public static final String NO_SELECTION = "&cYou do not have a selection currently set.";
    public static final String NO_STAFF_RESULTS = "&cYou do not have staff history results yet, please use /staffhistory <name> first.";
    public static final String ONLY_PLAYERS_AND_CONSOLE_CMD = "&cOnly console or players may use that command.";
    public static final String PLAYER_NEVER_JOINED = "&cThat player has never joined the server.";
    public static final String PUNISH_FORMAT = VISIBILITY + "&6(" + PREFIX + ") &4&l[i] &e<{id}> &b" + TARGET_STATUS + TARGET + " &fwas " + COLOR + PUNISHMENT + " &fby &b" + PUNISHER + " &ffor &a" + REASON;
    public static final String LENGTH_FORMAT = "&c(" + LENGTH + ")";
    public static final String PERMANENT_FORMAT = "&c(Permanent)";
    public static final String PARDON_FORMAT = VISIBILITY + "&6(" + PREFIX + ") &4&l[i] &b" + TARGET + " &fwas " + COLOR + "un" + PUNISHMENT + " &fby &b" + REMOVER;
    public static final String PRISON_SET_SPAWN = "&6(" + PREFIX + ") &4&l[i] &b" + ACTOR + " &fchanged the spawn for the prison &b" + JAIL_ID + " &fto their location.";
    public static final String PRISON_CREATE = "&6(" + PREFIX + ") &4&l[i] &b" + ACTOR + " &fcreated a prison with id &b" + JAIL_ID + " &fat their location.";
    public static final String PRISON_SET_MAX_PLAYERS = "&6(" + PREFIX + ") &4&l[i] &b" + ACTOR + " &fchanged the max players for the prison &b" + JAIL_ID + " &fto &b" + MAX_PLAYERS;
    public static final String PRISON_REMOVE = "&6(" + PREFIX + ") &4&l[i] &b" + ACTOR + " &fremoved the prison with id &b" + JAIL_ID;
    public static final String PRISON_SET_NAME = "&6(" + PREFIX + ") &4&l[i] &b" + ACTOR + " &fset the name of the prison &b" + JAIL_ID + " &fto &b" + DISPLAY;
    public static final String USING_DISPLAYNAMES = "&6(" + PREFIX + ") &4&l[i] &b" + ACTOR + " &fchanged using display names to &b" + DISPLAY;
    public static final String TRAINING_MODE_GLOBAL = "&6(" + PREFIX + ") &4&l[i] &b" + ACTOR + " &fchanged global training mode to &b" + DISPLAY;
    public static final String SETTING_CONFIRMPUNISHMENTS = "&6(" + PREFIX + ") &4&l[i] &b" + ACTOR + " &fchanged confirming punishments to &b" + DISPLAY;
    public static final String TRAINING_MODE_INDIVIDUAL = "&6(" + PREFIX + ") &4&l[i] &b" + ACTOR + " &fchanged training mode for &e" + TARGET + " &fto &b" + DISPLAY;
    public static final String PUNISHMENT_KICK = "&a{server} - {TYPE}\n\n&fStaff: &b" + ACTOR + "\n&fReason: &b" + REASON + "\n&fExpires: &c<expire>\n&f{pt} ID: &b{id}";
    public static final String PRISON_REDEFINE = "&6(" + PREFIX + ") &4&l[i] &b" + ACTOR + " &fredefined the bounds for the prison &b" + DISPLAY;
    public static final String RULE_CREATE = "&6(" + PREFIX + ") &4&l[i] &e<" + RULE_ID + "> &b" + ACTOR + " &fcreated a rule with the name &b" + RULE_NAME + " &fand the internal id &b" + RULE_INTERNALID ;
    public static final String RULE_SET_DESCRIPTION = "&6(" + PREFIX + ") &4&l[i] &b" + ACTOR + " &fset the description of the rule &b" + RULE_NAME + " &fto &b" + RULE_DESCRIPTION;
    public static final String REPORT_CREATE = "&4(REPORT) &d<{id}> &e" + TARGET + " &cwas reported for &e" + REASON + " &cby &e" + ACTOR;
    public static final String REPORT_CANCEL = "&4(REPORT} &d<{id}> &e" + ACTOR + " &ecancelled their report against &e" + TARGET;
    public static final String WATCHLIST_MESSAGE = "&9(WATCHLIST) &b" + ACTOR + " &f<type> &e" + TARGET + " &f<tp> the watchlist.";
    public static final String WATCHLIST_ADD_NOTE = "&9(WATCHLIST) &b" + ACTOR + " &fadded a note to &e" + TARGET + "'s &fwatchlist entry.";
    public static final String WATCHLIST_SET_PRIORITY = "&9(WATCHLIST) &b" + ACTOR + " &fset the priority on &e" + TARGET + "'s &fwatchlist entry to <priority>.";
    public static final String REPORT_ASSIGN = "&4{REPORT) &d<{id}> &e" + ACTOR + " assigned the report to &e" + TARGET;
    public static final String ONLY_PLAYERS_CMD = "&cOnly players may use that command.";
    public static final String NOT_ENOUGH_ARGS = "&cYou did not provide enough arguments.";
    public static final String COULD_NOT_FIND_PLAYER = "&cCould not find a player by that name.";
    public static final String WATCHLIST_PLAYER_JOIN = "&9(WATCHLIST) &e" + TARGET + " &fhas joined and is on the watchlist for &e" + REASON;
    public static final String NO_NAME_PROVIDED = "&cYou must provide a name.";
    public static final String ERROR_LIST_OF_RESULTS = "&cThere was a problem getting the list of results for that player.";
    private static final String PRISON_LOCATION_CHANGED = "&dThe prison location was changed by &b{player} &dso you have been teleported to the new location.";
    private static final String REPORT_FILED_AGAINST = "&aA report has been filed against &b" + TARGET;
    private static final String TARGET_JAIL = "&cYou have been jailed by &7" + ACTOR + " &cfor the reason &7" + REASON;
    private static final String TARGET_UNJAIL = "&aYou have been unjailed by &b" + ACTOR;
    
    public static void sendNotifyMessage(String message) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission(Perms.NOTIFY_PUNISHMENTS)) {
                p.sendMessage(Utils.color(message));
            }
        }
    }
    
    public static String watchlistMessage(String target, String staff, String type) {
        String format = WATCHLIST_MESSAGE;
        if (type.equalsIgnoreCase("removed")) {
            format = format.replace("<tp>", "from");
        } else if (type.equalsIgnoreCase("added")) {
            format = format.replace("<tp>", "to");
        }
        
        format = format.replace("<type>", type);
        format = format.replace(ACTOR, staff);
        format = format.replace(TARGET, target);
        return Utils.color(format);
    }
    
    public static String noPermissionCommand(String permission) {
        return Utils.color("&cYou must have the permission &7(" + permission + ") &cto use that command.");
    }
    
    public static String noPermissionAction(String permission) {
        return Utils.color("&cYou must have the permission &7(" + permission + ") &cto perform that action.");
    }
    
    public static String formatPunishKick(Punishment punishment) {
        String format = PUNISHMENT_KICK;
        if (punishment instanceof BanPunishment) {
            format = format.replace("{TYPE}", Colors.BAN + "&lBANNED");
            format = format.replace("{pt}", "Ban");
            if (punishment instanceof Expireable) {
                format = format.replace("<expire>", ((Expireable) punishment).formatExpireTime());
            } else {
                format = format.replace("<expire>", "Permanent");
            }
        } else if (punishment instanceof KickPunishment) {
            format = format.replace("{TYPE}", Colors.KICK + "&lKICKED");
            format = format.replace("{pt}", "Kick");
            format = format.replace("<expire>", "N/A");
        }
        format = format.replace(ACTOR, punishment.getPunisherName());
        format = format.replace(REASON, punishment.getReason());
        format = format.replace("{id}", punishment.getId() + "");
        format = format.replace("{server}", Enforcer.getInstance().getSettingsManager().getServerName());
        
        return format;
    }
    
    public static String watchlistAddNoteMessage(String targetName, String actorName) {
        String format = WATCHLIST_ADD_NOTE;
        format = format.replace(ACTOR, actorName);
        format = format.replace(TARGET, targetName);
        return Utils.color(format);
    }
    
    public static String watchlistSetPriority(String targetName, String actorName, Priority priority) {
        String format = WATCHLIST_SET_PRIORITY;
        format = format.replace("<priority>", priority.name());
        format = format.replace(TARGET, targetName);
        format = format.replace(ACTOR, actorName);
        return Utils.color(format);
    }
    
    private Messages() {}
    
    
    public static void sendOutputMessage(Player player, String message, Enforcer plugin) {
        for (Player pm : Bukkit.getOnlinePlayers()) {
            if (!pm.hasPermission(Perms.NOTIFY_PUNISHMENTS)) {
                continue;
            }
            String msg = message;
            if (pm.getUniqueId().equals(player.getUniqueId())) {
                if (plugin.getSettingsManager().getReplaceActorName()) {
                    msg = msg.replace(ACTOR, "&lYou");
                    msg = msg.replace("their", "your");
                }
            } else {
                if (plugin.getSettingsManager().isUsingDisplayNames()) {
                    msg = msg.replace(ACTOR, player.getDisplayName());
                } else {
                    msg = msg.replace(ACTOR, player.getName());
                }
            }
            msg = msg.replace(PREFIX, plugin.getSettingsManager().getPrefix());
            pm.sendMessage(Utils.color(msg));
        }
    }
    
    public static String noActivePunishment(String punishment) {
        return NO_ACTIVE_PUNISHMENT.replace("<type>", punishment);
    }
    
    public static String prisonLocationChanged(String name) {
        return PRISON_LOCATION_CHANGED.replace("{player}", name);
    }
    
    public static String targetJail(String actor, String reason) {
        return TARGET_JAIL.replace(ACTOR, actor).replace(REASON, reason);
    }
    
    public static String playerUnjailed(String removerName) {
        return TARGET_UNJAIL.replace(ACTOR,removerName);
    }
    
    public static String reportFiledAgainst(String name) {
        return REPORT_FILED_AGAINST.replace(TARGET, name);
    }
}
