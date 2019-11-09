package com.stardevmc.enforcer.util;

import com.firestar311.lib.pagination.Paginator;
import com.firestar311.lib.pagination.PaginatorFactory;
import com.firestar311.lib.player.PlayerInfo;
import com.firestar311.lib.util.Unit;
import com.firestar311.lib.util.Utils;
import com.stardevmc.enforcer.Enforcer;
import com.stardevmc.enforcer.modules.punishments.PunishmentBuilder;
import com.stardevmc.enforcer.modules.punishments.target.*;
import com.stardevmc.enforcer.modules.punishments.type.PunishmentType;
import com.stardevmc.enforcer.modules.punishments.type.abstraction.Punishment;
import com.stardevmc.enforcer.modules.rules.rule.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

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
    
    public static SortedMap<Integer, Rule> getOldRules(FileConfiguration config) {
        SortedMap<Integer, Rule> rules = new TreeMap<>();
        
        for (String r : config.getConfigurationSection("rules").getKeys(false)) {
            Rule rule = new Rule(config.getInt("rules." + r + ".id"), r, config.getString("rules." + r + ".name"), config.getString("rules." + r + ".description"));
            if (config.contains("rules." + r + ".material")) {
                rule.setMaterial(Material.valueOf(config.getString("rules." + r + ".material").toUpperCase()));
            } else {
                rule.setMaterial(Material.STONE);
            }
            if (config.contains("rules." + r + ".offenses")) {
                for (String o : config.getConfigurationSection("rules." + r + ".offenses").getKeys(false)) {
                    int offenseNumber = Integer.parseInt(o);
                    RuleOffense action = new RuleOffense(rule, offenseNumber);
                    int actionLength = 0;
                    if (config.contains("rules." + r + ".offenses." + o + ".length")) {
                        actionLength = config.getInt("rules." + r + ".offenses." + o + ".length");
                    }
                    action.setLength(actionLength);
                    for (String a : config.getConfigurationSection("rules." + r + ".offenses." + o + ".actions").getKeys(false)) {
                        int aN = Integer.parseInt(a);
                        PunishmentType type = PunishmentType.getType(config.getString("rules." + r + ".offenses." + o + ".actions." + a + ".punishment").toUpperCase());
                        int rawLength = -1;
                        String units = "";
                        int id = -1;
                        if (config.contains("rules." + r + ".offenses." + o + ".actions." + a + ".length")) {
                            rawLength = config.getInt("rules." + r + ".offenses." + o + ".actions." + a + ".length");
                        }
                        
                        if (config.contains("rules." + r + ".offenses." + o + ".actions." + a + ".unit")) {
                            units = config.getString("rules." + r + ".offenses." + o + ".actions." + a + ".unit");
                        }
                        
                        if (config.contains("rules." + r + ".offenses." + o + ".actions." + a + ".id")) {
                            id = config.getInt("rules." + r + ".offenses." + o + ".actions." + a + ".id");
                        }
                        
                        long length = -1;
                        if (!StringUtils.isEmpty(units)) {
                            Unit unit = Unit.matchUnit(units);
                            length = unit.convertTime(rawLength);
                        }
                        
                        RulePunishment punishment = new RulePunishment(type, length, rawLength, units);
                        punishment.setId(id);
                        action.addPunishment(aN, punishment);
                    }
                    rule.addOffense(offenseNumber, action);
                }
            }
            rules.put(rule.getId(), rule);
        }
        return rules;
    }
}
