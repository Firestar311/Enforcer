package com.stardevmc.enforcer.util;

import com.firestar311.lib.pagination.Paginator;
import com.firestar311.lib.pagination.PaginatorFactory;
import com.firestar311.lib.player.PlayerInfo;
import com.firestar311.lib.util.Utils;
import com.stardevmc.enforcer.Enforcer;
import com.stardevmc.enforcer.modules.punishments.PunishmentBuilder;
import com.stardevmc.enforcer.modules.punishments.target.*;
import com.stardevmc.enforcer.modules.punishments.type.PunishmentType;
import com.stardevmc.enforcer.modules.punishments.type.abstraction.Punishment;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class EnforcerUtils {
    public static Paginator<Punishment> generatePaginatedPunishmentList(List<Punishment> punishments, String header, String footer) {
        Collections.sort(punishments);
        PaginatorFactory<Punishment> factory = new PaginatorFactory<>();
        factory.setMaxElements(7).setHeader(header).setFooter(footer);
        punishments.forEach(factory::addElement);
        return factory.build();
    }
    
    public static Target getTarget(String targetArg) {
        Target target;
        PlayerInfo info = Enforcer.getInstance().getPlayerManager().getPlayerInfo(targetArg);
        if (info != null) {
            target = new PlayerTarget(info.getUuid());
        } else {
            targetArg = targetArg.toLowerCase();
            if (targetArg.startsWith("ip:")) {
                String[] ipArr = targetArg.split(":");
                PlayerInfo ipPlayer = Enforcer.getInstance().getPlayerManager().getPlayerInfo(ipArr[1]);
                if (ipPlayer == null) {
                    return null;
                }
            
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(ipPlayer.getUuid());
                if (offlinePlayer.isOnline()) {
                    Player player = offlinePlayer.getPlayer();
                    String ip = player.getAddress().getAddress().toString().split(":")[0].replace("/", "");
                    target = new IPTarget(ip);
                } else {
                    if (ipPlayer.getIpAddresses().size() == 1) {
                        target = new IPTarget(ipPlayer.getIpAddresses().get(0));
                    } else {
                        target = new IPListTarget(ipPlayer.getIpAddresses());
                    }
                }
            } else {
                String[] rawIpArr = targetArg.split("\\.");
                if (rawIpArr.length != 4) {
                    return null;
                } else {
                    for (String rawPart : rawIpArr) {
                        try {
                            Integer.parseInt(rawPart);
                        } catch (NumberFormatException e) {
                            //if (!rawPart.equalsIgnoreCase("*")) {
                            return null;
                            //}
                        }
                    }
                
                    target = new IPTarget(targetArg);
                }
            }
        }
        return target;
    }
    
    public static String getPunishString(PunishmentBuilder puBuilder) {
        return getPunishString(puBuilder.getType(), puBuilder.getLength());
    }
    
    public static String getPunishString(PunishmentType type, long length) {
        String punishmentString = type.getColor();
        switch (type) {
            case PERMANENT_BAN:
                punishmentString += "Permanent Ban";
                break;
            case TEMPORARY_BAN:
                punishmentString += "Ban for " + Utils.formatTime(length);
                break;
            case PERMANENT_MUTE:
                punishmentString += "Permanent Mute";
                break;
            case TEMPORARY_MUTE:
                punishmentString += "Mute for " + Utils.formatTime(length);
                break;
            case WARN:
                punishmentString += "Warning";
                break;
            case KICK:
                punishmentString += "Kick";
                break;
            case JAIL:
                punishmentString += "Jail";
                break;
        }
        return punishmentString;
    }
}
