package com.firestar311.enforcer.modules.punishments.type.abstraction;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.modules.punishments.*;
import com.firestar311.enforcer.modules.punishments.actor.Actor;
import com.firestar311.enforcer.modules.punishments.actor.PlayerActor;
import com.firestar311.enforcer.modules.punishments.target.*;
import com.firestar311.enforcer.modules.punishments.type.PunishmentType;
import com.firestar311.enforcer.modules.punishments.type.interfaces.Acknowledgeable;
import com.firestar311.enforcer.modules.punishments.type.interfaces.Expireable;
import com.firestar311.enforcer.util.*;
import com.firestar311.enforcer.util.evidence.Evidence;
import com.firestar311.lib.pagination.IElement;
import com.firestar311.lib.player.PlayerInfo;
import com.firestar311.lib.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

import static com.firestar311.enforcer.modules.punishments.Colors.BAN;
import static com.firestar311.enforcer.modules.punishments.Colors.MUTE;
import static com.firestar311.enforcer.util.Variables.*;

public abstract class Punishment implements IElement, Comparable<Punishment>, ConfigurationSerializable {
    
    protected boolean active, purgatory, offline = false, trainingMode = false;
    protected long date, removedDate;
    protected Evidence evidence;
    protected int id, ruleId = -1, offenseNumber = -1;
    protected String localPunisherName, localTargetName, removerName;
    protected Actor punisher;
    protected String reason, removedReason = "";
    protected Actor remover;
    protected String server;
    protected Target target;
    protected PunishmentType type;
    protected Visibility visibility, pardonVisibility = Visibility.NORMAL;
    
    public Punishment(PunishmentType type, String server, Actor punisher, Target target, String reason, long date) {
        this(-1, type, server, punisher, target, reason, date, true, false, Visibility.NORMAL);
    }
    
    public Punishment(int id, PunishmentType type, String server, Actor punisher, Target target, String reason, long date, boolean active, boolean purgatory, Visibility visibility) {
        this.id = id;
        this.type = type;
        this.server = server;
        this.punisher = punisher;
        this.reason = reason;
        this.active = active;
        this.target = target;
        this.date = date;
        this.visibility = visibility;
        this.purgatory = purgatory;
    }
    
    public Punishment(PunishmentType type, String server, Actor punisher, Target target, String reason, long date, Visibility visibility) {
        this(-1, type, server, punisher, target, reason, date, true, false, visibility);
    }
    
    protected Map<String, Object> serializeBase() {
        Map<String, Object> serialized = new HashMap<>();
        serialized.put("active", this.active);
        serialized.put("purgatory", this.purgatory);
        serialized.put("offline", this.offline);
        serialized.put("trainingMode", this.trainingMode);
        serialized.put("date", this.date + "");
        serialized.put("removedDate", this.removedDate + "");
        serialized.put("id", this.id + "");
        serialized.put("ruleId", this.ruleId + "");
        serialized.put("offenseNumber", this.offenseNumber + "");
        serialized.put("punisher", this.punisher);
        serialized.put("reason", this.reason);
        serialized.put("removedReason", this.removedReason);
        serialized.put("remover", this.remover);
        serialized.put("server", this.server);
        serialized.put("target", this.target);
        serialized.put("type", this.type.name());
        serialized.put("visibility", this.visibility.name());
        serialized.put("pardonVisibility", this.pardonVisibility.name());
        serialized.put("evidence", this.evidence);
        return serialized;
    }
    
    protected static PunishmentBuilder deserializeBase(Map<String, Object> serialized) {
        PunishmentBuilder puBuilder = new PunishmentBuilder();
        boolean active = (boolean) serialized.get("active");
        boolean purgatory = (boolean) serialized.get("purgatory");
        boolean offline = (boolean) serialized.get("offline");
        boolean trainingMode = (boolean) serialized.get("trainingMode");
        long date = Long.parseLong((String) serialized.get("date"));
        long removedDate = Long.parseLong((String) serialized.get("removedDate"));
        Evidence evidence = (Evidence) serialized.get("evidence");
        int id = Integer.parseInt((String) serialized.get("id"));
        int ruleId = Integer.parseInt((String) serialized.get("ruleId"));
        int offenseNumber = Integer.parseInt((String) serialized.get("offenseNumber"));
        Actor punisher = (Actor) serialized.get("punisher");
        String reason = (String) serialized.get("reason");
        String removedReason = (String) serialized.get("removedReason");
        Actor remover = (Actor) serialized.get("remover");
        String server = (String) serialized.get("server");
        Target target = (Target) serialized.get("target");
        PunishmentType type = PunishmentType.valueOf((String) serialized.get("type"));
        Visibility visibility = Visibility.valueOf((String) serialized.get("visibility"));
        Visibility pardonVisibility = Visibility.valueOf((String) serialized.get("pardonVisibility"));
        puBuilder.setActive(active).setPurgatory(purgatory).setOffline(offline).setTrainingMode(trainingMode).setDate(date).setRemovedDate(removedDate)
        .setEvidence(evidence).setId(id).setRuleId(ruleId).setOffenseNumber(offenseNumber).setPunisher(punisher).setReason(reason).setRemovedReason(removedReason)
        .setRemover(remover).setServer(server).setTarget(target).setType(type).setVisibility(visibility).setPardonVisibility(pardonVisibility);
        return puBuilder;
    }
    
    public final boolean isActive() {
        return active;
    }
    
    public final void setActive(boolean active) {
        this.active = active;
    }
    
    public abstract void executePunishment();
    
    public abstract void reversePunishment(Actor remover, long removedDate);
    
    public void sendPunishMessage() {
        String message = formatMessage();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!canSeeMessages(p, visibility)) { continue; }
            String msg = message;
            
            if (punisher instanceof PlayerActor) {
                PlayerActor playerActor = ((PlayerActor) punisher);
                if (p.getUniqueId().equals(playerActor.getUniqueId())) {
                    msg = msg.replace(PUNISHER, "&lYou");
                } else {
                    Player player = Bukkit.getPlayer(playerActor.getUniqueId());
                    if (Enforcer.getInstance().getSettingsManager().isUsingDisplayNames()) {
                        msg = msg.replace(Variables.PUNISHER, player.getDisplayName());
                    } else {
                        msg = msg.replace(Variables.PUNISHER, getPunisherName());
                    }
                }
            } else {
                msg = msg.replace(PUNISHER, "&4&lConsole");
            }
            
            p.sendMessage(Utils.color(msg));
        }
    }
    
    public final String formatMessage() {
        String message = Messages.PUNISH_FORMAT;
        message = message.replace(VISIBILITY, visibility.getPrefix());
        message = message.replace(PREFIX, server.toUpperCase());
        if (target instanceof PlayerTarget) {
            message = message.replace(TARGET, getTargetName());
        } else {
            PlayerInfo ipInfo = null;
            playerLoop:
            for (PlayerInfo info : Enforcer.getInstance().getPlayerManager().getPlayers().values()) {
                if (target instanceof IPTarget) {
                    String ip = ((IPTarget) target).getIpAddress();
                    if (info.getIpAddresses().contains(ip)) {
                        ipInfo = info;
                        break;
                    }
                } else if(target instanceof IPListTarget) {
                    for (String ip : ((IPListTarget) target).getIpAddresses()) {
                        if (info.getIpAddresses().contains(ip)) {
                            ipInfo = info;
                            break playerLoop;
                        }
                    }
                }
            }
            
            message = message.replace(TARGET, ipInfo.getLastName());
        }
        message = message.replace(REASON, reason);
        if (target instanceof PlayerTarget) {
            PlayerTarget playerTarget = ((PlayerTarget) this.target);
            Player t = Bukkit.getPlayer(playerTarget.getUniqueId());
            if (t != null) {
                message = message.replace(TARGET_STATUS, "&2");
            } else {
                message = message.replace(TARGET_STATUS, "&c");
            }
        } else {
            message = message.replace(TARGET_STATUS, "");
        }
        message = message.replace("{id}", id + "");
        message = replaceExpireVariables(message);
        message = replacePunishmentVariables(message);
        return Utils.color(message);
    }
    
    public boolean canSeeMessages(Player p, Visibility visibility) {
        if (visibility.equals(Visibility.NORMAL)) {
            return p.hasPermission(Perms.NOTIFY_PUNISHMENTS);
        }
        if (visibility.equals(Visibility.SILENT)) {
            try {
                net.milkbowl.vault.permission.Permission perms = Enforcer.getInstance().getPermission();
                if (perms != null) {
                    String groupName = perms.getPrimaryGroup(p).toLowerCase();
                    return p.hasPermission("enforcer.punishments.notify." + groupName);
                }
                return p.hasPermission(Perms.NOTIFY_PUNISHMENTS);
            } catch (Exception ignored) {
            
            }
        }
        return true;
    }
    
    public String getPunisherName() {
        if (localPunisherName == null) {
            if (punisher instanceof PlayerActor) {
                this.localPunisherName = ((PlayerActor) punisher).getPlayerInfo().getLastName();
            } else {
                this.localPunisherName = "Console";
            }
        }
        return localPunisherName;
    }
    
    public void setPunisherName(String punisherName) {
        this.localPunisherName = punisherName;
    }
    
    public String getTargetName() {
        if (localTargetName == null) {
            localTargetName = target.getName();
        }
        return localTargetName;
    }
    
    public void setTargetName(String targetName) {
        this.localTargetName = targetName;
    }
    
    private String replaceExpireVariables(String message) {
        if (this instanceof Expireable) {
            Expireable expireable = (Expireable) this;
            if (type.equals(PunishmentType.TEMPORARY_BAN)) {
                message = (message + " " + Messages.LENGTH_FORMAT);
                message = message.replace(LENGTH, expireable.formatExpireTime());
                message = message.replace(COLOR, BAN);
                return message.replace(PUNISHMENT, "banned");
            }
            if (type.equals(PunishmentType.TEMPORARY_MUTE)) {
                message = (message + " " + Messages.LENGTH_FORMAT);
                message = message.replace(LENGTH, expireable.formatExpireTime());
                message = message.replace(COLOR, MUTE);
                return message.replace(PUNISHMENT, "muted");
            }
        }
        return message;
    }
    
    private String replacePunishmentVariables(String message) {
        if (type.equals(PunishmentType.PERMANENT_BAN)) {
            message = (message + " " + Messages.PERMANENT_FORMAT);
            message = message.replace(COLOR, BAN);
            return message.replace(PUNISHMENT, "banned");
        }
        if (type.equals(PunishmentType.PERMANENT_MUTE)) {
            message = (message + " " + Messages.PERMANENT_FORMAT);
            message = message.replace(COLOR, MUTE);
            return message.replace(PUNISHMENT, "muted");
        }
        if (type.equals(PunishmentType.JAIL)) {
            message = message.replace(COLOR, Colors.JAIL);
            return message.replace(PUNISHMENT, "jailed");
        }
        if (type.equals(PunishmentType.KICK)) {
            message = message.replace(COLOR, Colors.KICK);
            return message.replace(PUNISHMENT, "kicked");
        }
        if (type.equals(PunishmentType.WARN)) {
            message = message.replace(COLOR, Colors.WARN);
            return message.replace(PUNISHMENT, "warned");
        }
        if (type.equals(PunishmentType.BLACKLIST)) {
            message = message.replace(COLOR, Colors.BLACKLIST);
            return message.replace(PUNISHMENT, "blacklisted");
        }
        return message;
    }
    
    public void sendRemovalMessage() {
        String message = formatRemoveMessage(this.server);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!canSeeMessages(p, pardonVisibility)) { continue; }
            String msg = message;
            if (remover instanceof PlayerActor) {
                PlayerActor playerActor = ((PlayerActor) punisher);
                if (p.getUniqueId().equals(playerActor.getUniqueId())) {
                    msg = msg.replace(REMOVER, "&lYou");
                } else {
                    Player player = Bukkit.getPlayer(playerActor.getUniqueId());
                    if (Enforcer.getInstance().getSettingsManager().isUsingDisplayNames()) {
                        msg = msg.replace(Variables.REMOVER, player.getDisplayName());
                    } else {
                        msg = msg.replace(Variables.REMOVER, getPunisherName());
                    }
                }
            } else {
                msg = msg.replace(PUNISHER, "&4&lConsole");
            }
            p.sendMessage(Utils.color(msg));
        }
    }
    
    public final String formatRemoveMessage(String server) {
        String message = Messages.PARDON_FORMAT;
        message = message.replace(VISIBILITY, pardonVisibility.getPrefix());
        message = message.replace(PREFIX, server.toUpperCase());
        message = message.replace(TARGET, getTargetName());
        if (type.equals(PunishmentType.PERMANENT_BAN) || type.equals(PunishmentType.TEMPORARY_BAN)) {
            message = message.replace(COLOR, Colors.BAN);
            message = message.replace(PUNISHMENT, "banned");
        } else if (type.equals(PunishmentType.PERMANENT_MUTE) || type.equals(PunishmentType.TEMPORARY_MUTE)) {
            message = message.replace(COLOR, MUTE);
            message = message.replace(PUNISHMENT, "muted");
        } else if (type.equals(PunishmentType.JAIL)) {
            message = message.replace(COLOR, Colors.JAIL);
            message = message.replace(PUNISHMENT, "jailed");
        }
        
        return Utils.color(message);
    }
    
    public String formatLine(String[] args) {
        if (args == null) {
            return "";
        }
        String message = "";
        if (args[0].equalsIgnoreCase("history")) {
            message += "&5[" + PUNISHMENT_STATUS + "] &e{id} &b" + TARGET + " &fwas " + COLOR + PUNISHMENT + " &fby &b" + PUNISHER + " &ffor &b" + REASON;
        } else if (args[0].equalsIgnoreCase("staffhistory")) {
            message += "&5[" + PUNISHMENT_STATUS + "] &e{id} &b" + PUNISHER + " " + COLOR + PUNISHMENT + " &b" + TARGET + " &ffor &b" + REASON;
        }
        message = message.replace(TARGET, getTargetName());
        message = message.replace(REASON, reason);
        message = message.replace("{id}", id + "");
        message = message.replace(PUNISHER, getPunisherName());
        if (this instanceof Expireable) {
            Expireable expireable = (Expireable) this;
            message = (message + " " + Messages.LENGTH_FORMAT);
            message = message.replace(LENGTH, Utils.formatTime(Math.abs(this.getDate() - expireable.getExpireDate())));
            if (!expireable.isExpired() && remover == null) {
                message = message + "\n    &8- &9Expires in: " + expireable.formatExpireTime();
            }
            if (type.equals(PunishmentType.TEMPORARY_BAN)) {
                message = message.replace(COLOR, BAN);
                message = message.replace(PUNISHMENT, "banned");
            } else if (type.equals(PunishmentType.TEMPORARY_MUTE)) {
                message = message.replace(COLOR, MUTE);
                message = message.replace(PUNISHMENT, "muted");
            }
        }
        message = replacePunishmentVariables(message);
        if (this.trainingMode) {
            message = message.replace(PUNISHMENT_STATUS, "Training Mode");
        } else if (this.remover != null) {
            message = message.replace(PUNISHMENT_STATUS, "Removed");
        } else if (this instanceof Expireable) {
            Expireable expireable = (Expireable) this;
            if (expireable.isExpired()) {
                message = message.replace(PUNISHMENT_STATUS, "Expired");
            } else {
                message = message.replace(PUNISHMENT_STATUS, "Active");
            }
        } else if (this instanceof Acknowledgeable) {
            Acknowledgeable acknowledgeable = ((Acknowledgeable) this);
            if (acknowledgeable.isAcknowledged()) {
                message = message.replace(PUNISHMENT_STATUS, "Acknowledged");
            } else {
                message = message.replace(PUNISHMENT_STATUS, "Active");
            }
        } else {
            message = message.replace(PUNISHMENT_STATUS, "Active");
        }
        return Utils.color(message);
    }
    
    public long getDate() {
        return date;
    }
    
    public boolean wasOffline() {
        return offline;
    }
    
    public boolean isTrainingPunishment() {
        return trainingMode;
    }
    
    public int compareTo(Punishment o) {
        return Long.compare(this.getDate(), o.getDate());
    }
    
    public boolean isPurgatory() {
        return purgatory;
    }
    
    public final int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getServer() {
        return server;
    }
    
    public Target getTarget() {
        return target;
    }
    
    public Actor getPunisher() {
        return punisher;
    }
    
    public final String getReason() {
        return reason;
    }
    
    public final PunishmentType getType() {
        return type;
    }
    
    public Evidence getEvidence() {
        return evidence;
    }
    
    public void setEvidence(Evidence evidence) {
        this.evidence = evidence;
    }
    
    public Actor getRemover() {
        return remover;
    }
    
    public void setRemover(Actor remover) {
        this.remover = remover;
    }
    
    public String getRemoverName() {
        if (removerName == null) {
            removerName = remover.getName();
        }
        return removerName;
    }
    
    public void setRemoverName(String name) {
        this.removerName = name;
    }
    
    public long getRemovedDate() {
        return removedDate;
    }
    
    public void setRemovedDate(long removedDate) {
        this.removedDate = removedDate;
    }
    
    public Visibility getVisibility() {
        return visibility;
    }
    
    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }
    
    public Visibility getPardonVisibility() {
        return pardonVisibility;
    }
    
    public void setPardonVisibility(Visibility pardonVisibility) {
        this.pardonVisibility = pardonVisibility;
    }
    
    public int getRuleId() {
        return ruleId;
    }
    
    public void setRuleId(int ruleId) {
        this.ruleId = ruleId;
    }
    
    public int getOffenseNumber() {
        return offenseNumber;
    }
    
    public void setOffenseNumber(int offenseNumber) {
        this.offenseNumber = offenseNumber;
    }
    
    public String getRemovedReason() {
        return removedReason;
    }
    
    public void setRemovedReason(String removedReason) {
        this.removedReason = removedReason;
    }
    
    public void setOffline(boolean offline) {
        this.offline = offline;
    }
    
    public void setTrainingMode(boolean trainingMode) {
        this.trainingMode = trainingMode;
    }
    
    public void setPurgatory(boolean purgatory) {
        this.purgatory = purgatory;
    }
}