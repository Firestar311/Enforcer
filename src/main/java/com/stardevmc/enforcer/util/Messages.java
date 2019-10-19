package com.stardevmc.enforcer.util;

import com.stardevmc.enforcer.Enforcer;
import com.stardevmc.enforcer.modules.punishments.Colors;
import com.stardevmc.enforcer.modules.punishments.type.abstraction.BanPunishment;
import com.stardevmc.enforcer.modules.punishments.type.abstraction.Punishment;
import com.stardevmc.enforcer.modules.punishments.type.interfaces.Expireable;
import com.stardevmc.enforcer.modules.punishments.type.impl.KickPunishment;
import com.firestar311.lib.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class Messages {
    public static final String PUNISH_FORMAT = Variables.VISIBILITY + "&6(" + Variables.PREFIX + ") &4&l[i] &e<{id}> &b" + Variables.TARGET_STATUS + Variables.TARGET + " &fwas " + Variables.COLOR + Variables.PUNISHMENT + " &fby &b" + Variables.PUNISHER + " &ffor &a" + Variables.REASON;
    public static final String LENGTH_FORMAT = "&c(" + Variables.LENGTH + ")";
    public static final String PERMANENT_FORMAT = "&c(Permanent)";
    public static final String PARDON_FORMAT = Variables.VISIBILITY + "&6(" + Variables.PREFIX + ") &4&l[i] &b" + Variables.TARGET + " &fwas " + Variables.COLOR + "un" + Variables.PUNISHMENT + " &fby &b" + Variables.REMOVER;
    public static final String PRISON_SET_SPAWN = "&6(" + Variables.PREFIX + ") &4&l[i] &b" + Variables.ACTOR + " &fchanged the spawn for the prison &b" + Variables.JAIL_ID + " &fto their location.";
    public static final String PRISON_CREATE = "&6(" + Variables.PREFIX + ") &4&l[i] &b" + Variables.ACTOR + " &fcreated a prison with id &b" + Variables.JAIL_ID + " &fat their location.";
    public static final String PRISON_SET_MAX_PLAYERS = "&6(" + Variables.PREFIX + ") &4&l[i] &b" + Variables.ACTOR + " &fchanged the max players for the prison &b" + Variables.JAIL_ID + " &fto &b" + Variables.MAX_PLAYERS;
    public static final String PRISON_REMOVE = "&6(" + Variables.PREFIX + ") &4&l[i] &b" + Variables.ACTOR + " &fremoved the prison with id &b" + Variables.JAIL_ID;
    public static final String PRISON_SET_NAME = "&6(" + Variables.PREFIX + ") &4&l[i] &b" + Variables.ACTOR + " &fset the name of the prison &b" + Variables.JAIL_ID + " &fto &b" + Variables.DISPLAY;
    public static final String USING_DISPLAYNAMES = "&6(" + Variables.PREFIX + ") &4&l[i] &b" + Variables.ACTOR + " &fchanged using display names to &b" + Variables.DISPLAY;
    public static final String TRAINING_MODE_GLOBAL = "&6(" + Variables.PREFIX + ") &4&l[i] &b" + Variables.ACTOR + " &fchanged global training mode to &b" + Variables.DISPLAY;
    public static final String SETTING_CONFIRMPUNISHMENTS = "&6(" + Variables.PREFIX + ") &4&l[i] &b" + Variables.ACTOR + " &fchanged confirming punishments to &b" + Variables.DISPLAY;
    public static final String TRAINING_MODE_INDIVIDUAL = "&6(" + Variables.PREFIX + ") &4&l[i] &b" + Variables.ACTOR + " &fchanged training mode for &e" + Variables.TARGET + " &fto &b" + Variables.DISPLAY;
    public static final String PUNISHMENT_KICK = "&a{server} - {TYPE}\n\n&fStaff: &b" + Variables.ACTOR + "\n&fReason: &b" + Variables.REASON + "\n&fExpires: &c<expire>\n&f{pt} ID: &b{id}";
    public static final String PRISON_REDEFINE = "&6(" + Variables.PREFIX + ") &4&l[i] &b" + Variables.ACTOR + " &fredefined the bounds for the prison &b" + Variables.DISPLAY;
    public static final String RULE_CREATE = "&6(" + Variables.PREFIX + ") &4&l[i] &e<" + Variables.RULE_ID + "> &b" + Variables.ACTOR + " &fcreated a rule with the name &b" + Variables.RULE_NAME + " &fand the internal id &b" + Variables.RULE_INTERNALID ;
    public static final String RULE_SET_DESCRIPTION = "&6(" + Variables.PREFIX + ") &4&l[i] &b" + Variables.ACTOR + " &fset the description of the rule &b" + Variables.RULE_NAME + " &fto &b" + Variables.RULE_DESCRIPTION;
    public static final String REPORT_CREATE = "&4(REPORT) &d<{id}> &e" + Variables.TARGET + " &cwas reported for &e" + Variables.REASON + " &cby &e" + Variables.ACTOR;
    public static final String REPORT_CANCEL = "&4(REPORT} &d<{id}> &e" + Variables.ACTOR + " &ecancelled their report against &e" + Variables.TARGET;
    
    public static void sendNotifyMessage(String message) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission(Perms.NOTIFY_PUNISHMENTS)) {
                p.sendMessage(Utils.color(message));
            }
        }
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
        format = format.replace(Variables.ACTOR, punishment.getPunisherName());
        format = format.replace(Variables.REASON, punishment.getReason());
        format = format.replace("{id}", punishment.getId() + "");
        format = format.replace("{server}", Enforcer.getInstance().getSettingsManager().getServerName());
        
        return format;
    }
    
    private static Messages instance = new Messages();
    public static Messages getInstance() { return instance; }
    private Messages() {}
    
    
    public static void sendOutputMessage(Player player, String message, Enforcer plugin) {
        for (Player pm : Bukkit.getOnlinePlayers()) {
            if (!pm.hasPermission(Perms.NOTIFY_PUNISHMENTS)) {
                continue;
            }
            String msg = message;
            if (pm.getUniqueId().equals(player.getUniqueId())) {
                msg = msg.replace(Variables.ACTOR, "&lYou");
                msg = msg.replace("their", "your");
            } else {
                if (plugin.getSettingsManager().isUsingDisplayNames()) {
                    msg = msg.replace(Variables.ACTOR, player.getDisplayName());
                } else {
                    msg = msg.replace(Variables.ACTOR, player.getName());
                }
            }
            msg = msg.replace(Variables.PREFIX, plugin.getSettingsManager().getPrefix());
            pm.sendMessage(Utils.color(msg));
        }
    }
}
