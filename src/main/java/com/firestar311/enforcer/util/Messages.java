package com.firestar311.enforcer.util;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.model.punishment.abstraction.BanPunishment;
import com.firestar311.enforcer.model.punishment.abstraction.Punishment;
import com.firestar311.enforcer.model.punishment.interfaces.Expireable;
import com.firestar311.enforcer.model.punishment.type.KickPunishment;
import com.firestar311.lib.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static com.firestar311.enforcer.util.Variables.*;

public final class Messages {
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
    public static final String TRAINING_MODE = "&6(" + PREFIX + ") &4&l[i] &b" + ACTOR + " &fchanged training mode to &b" + DISPLAY;
    public static final String PUNISHMENT_KICK = "&a{server} - {TYPE}\n\n&fStaff: &b" + ACTOR + "\n&fReason: &b" + REASON + "\n&fExpires: &c<expire>\n&f{pt} ID: &b{id}";
    public static final String PRISON_REDEFINE = "&6(" + PREFIX + ") &4&l[i] &b" + ACTOR + " &fredefined the bounds for the prison &b" + DISPLAY;
    public static final String RULE_CREATE = "&6(" + PREFIX + ") &4&l[i] &e<" + RULE_ID + "> &b" + ACTOR + " &fcreated a rule with the name &b" + RULE_NAME + " &fand the internal id &b" + RULE_INTERNALID ;
    public static final String RULE_SET_DESCRIPTION = "&6(" + PREFIX + ") &4&l[i] &b" + ACTOR + " &fset the description of the rule &b" + RULE_NAME + " &fto &b" + RULE_DESCRIPTION;
    
    
    public static void sendNotifyMessage(String message) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission(Perms.NOTIFY_PUNISHMENTS)) {
                p.sendMessage(Utils.color(message));
            }
        }
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
        format = format.replace("{server}", Enforcer.getInstance().getDataManager().getServerName());
        
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
                msg = msg.replace(ACTOR, "&lYou");
                msg = msg.replace("their", "your");
            } else {
                if (plugin.getDataManager().isUsingDisplayNames()) {
                    msg = msg.replace(ACTOR, player.getDisplayName());
                } else {
                    msg = msg.replace(ACTOR, player.getName());
                }
            }
            msg = msg.replace(PREFIX, plugin.getDataManager().getPrefix());
            pm.sendMessage(Utils.color(msg));
        }
    }
}
