package com.firestar311.enforcer.modules.punishments.type.abstraction;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.modules.punishments.Colors;
import com.firestar311.enforcer.modules.punishments.Visibility;
import com.firestar311.enforcer.modules.punishments.type.PunishmentType;
import com.firestar311.enforcer.modules.punishments.type.impl.*;
import com.firestar311.enforcer.modules.punishments.type.interfaces.Acknowledgeable;
import com.firestar311.enforcer.modules.punishments.type.interfaces.Expireable;
import com.firestar311.enforcer.util.*;
import com.firestar311.lib.pagination.Paginatable;
import com.firestar311.lib.player.PlayerInfo;
import com.firestar311.lib.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;

import static com.firestar311.enforcer.modules.punishments.Colors.BAN;
import static com.firestar311.enforcer.modules.punishments.Colors.MUTE;
import static com.firestar311.enforcer.util.Variables.*;

public abstract class Punishment implements Paginatable, Comparable<Punishment> {
    
    protected int id, ruleId = -1, offenseNumber = -1;
    protected PunishmentType type;
    protected String server;
    protected UUID punisher, target, remover;
    protected String reason;
    protected long date, removedDate;
    protected boolean active, purgatory, offline = false, trainingMode = false;
    protected Visibility visibility, pardonVisibility = Visibility.NORMAL;
    protected Evidence evidence;
    
    protected String localPunisherName, localTargetName, removerName;
    
    public Punishment(PunishmentType type, String server, UUID punisher, UUID target, String reason, long date) {
        this(-1, type, server, punisher, target, reason, date, true, false, Visibility.NORMAL);
    }
    
    public Punishment(PunishmentType type, String server, UUID punisher, UUID target, String reason, long date, Visibility visibility) {
        this(-1, type, server, punisher, target, reason, date, true, false, visibility);
    }
    
    public Punishment(int id, PunishmentType type, String server, UUID punisher, UUID target, String reason, long date, boolean active, boolean purgatory, Visibility visibility) {
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
    
    public Punishment(Map<String, Object> serialized) {
        List<Field> fields = new ArrayList<>();
        searchClasses(fields, getClass());
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                if (serialized.containsKey(field.getName())) {
                    if (field.getType().isEnum()) {
                        Class<?> enumClass = field.getType();
                        Method valueOf = enumClass.getMethod("valueOf", String.class);
                        field.set(this, valueOf.invoke(null, (String) serialized.get(field.getName())));
                    } else if (field.getType().isAssignableFrom(UUID.class)) {
                        field.set(this, UUID.fromString((String) serialized.get(field.getName())));
                    } else {
                        field.set(this, serialized.get(field.getName()));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public static Map<String, Object> serialize(Punishment punishment) {
        try {
            List<Field> fields = new ArrayList<>();
            searchClasses(fields, punishment.getClass());
            
            if (fields.isEmpty()) {
                System.out.println("List of fields is empty");
                return null;
            }
            
            Map<String, Object> serialized = new HashMap<>();
            
            for (Field field : fields) {
                field.setAccessible(true);
                
                if (field.get(punishment) == null) {
                    continue;
                }
                
                if (field.getType().isAssignableFrom(UUID.class)) {
                    UUID uuid = (UUID) field.get(punishment);
                    serialized.put(field.getName(), uuid.toString());
                } else if (field.getType().isAssignableFrom(Visibility.class)) {
                    Visibility visibility = (Visibility) field.get(punishment);
                    serialized.put(field.getName(), visibility.name());
                } else if (field.getType().isAssignableFrom(PunishmentType.class)) {
                    PunishmentType type = (PunishmentType) field.get(punishment);
                    serialized.put(field.getName(), type.name());
                } else {
                    if (!field.getType().isAssignableFrom(Prompt.class)) {
                        serialized.put(field.getName(), field.get(punishment));
                    }
                }
            }
            
            return serialized;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static Punishment deserialize(Map<String, Object> serialized) {
        if (serialized.containsKey("type")) {
            PunishmentType type = PunishmentType.valueOf((String) serialized.get("type"));
            
            switch (type) {
                case PERMANENT_BAN:
                    return new PermanentBan(serialized);
                case TEMPORARY_BAN:
                    return new TemporaryBan(serialized);
                case PERMANENT_MUTE:
                    return new PermanentMute(serialized);
                case TEMPORARY_MUTE:
                    return new TemporaryMute(serialized);
                case WARN:
                    return new WarnPunishment(serialized);
                case KICK:
                    return new KickPunishment(serialized);
                case JAIL:
                    return new JailPunishment(serialized);
            }
            
            return null;
        }
        return null;
    }
    
    public void setPunisherName(String punisherName) {
        this.localPunisherName = punisherName;
    }
    
    public void setTargetName(String targetName) {
        this.localTargetName = targetName;
    }
    
    public final int getId() {
        return id;
    }
    
    public String getPunisherName() {
        if (localPunisherName == null) {
            PlayerInfo info = Enforcer.getInstance().getPlayerManager().getPlayerInfo(this.punisher);
            if (info != null) {
                localPunisherName = info.getLastName();
            } else {
                OfflinePlayer punisher = Bukkit.getOfflinePlayer(this.punisher);
                if (punisher != null) {
                    localPunisherName = punisher.getName();
                }
            }
        }
        return localPunisherName;
    }
    
    public String getTargetName() {
        if (localTargetName == null) {
            PlayerInfo info = Enforcer.getInstance().getPlayerManager().getPlayerInfo(this.target);
            if (info != null) {
                localTargetName = info.getLastName();
            }
            
            if (localTargetName == null) {
                OfflinePlayer target = Bukkit.getOfflinePlayer(this.target);
                if (target != null) {
                    localTargetName = target.getName();
                }
            }
        }
        return localTargetName;
    }
    
    public String getServer() {
        return server;
    }
    
    public UUID getTarget() {
        return target;
    }
    
    public UUID getPunisher() {
        return punisher;
    }
    
    public final String getReason() {
        return reason;
    }
    
    public final boolean isActive() {
        return active;
    }
    
    public final void setActive(boolean active) {
        this.active = active;
    }
    
    public final PunishmentType getType() {
        return type;
    }
    
    public long getDate() {
        return date;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public Evidence getEvidence() {
        return evidence;
    }
    
    public void setEvidence(Evidence evidence) {
        this.evidence = evidence;
    }
    
    public abstract void executePunishment();
    
    public abstract void reversePunishment(UUID remover, long removedDate);
    
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
    
    public void sendPunishMessage() {
        String message = formatMessage();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!canSeeMessages(p, visibility)) continue;
            String msg = message;
            if (p.getUniqueId().equals(punisher)) {
                msg = msg.replace(Variables.PUNISHER, "&lYou");
            } else {
                Player player = Bukkit.getPlayer(this.punisher);
                if (Enforcer.getInstance().getSettingsManager().isUsingDisplayNames()) {
                    msg = msg.replace(Variables.PUNISHER, player.getDisplayName());
                } else {
                    msg = msg.replace(Variables.PUNISHER, getPunisherName());
                }
            }
            p.sendMessage(Utils.color(msg));
        }
    }
    
    public void sendRemovalMessage() {
        String message = formatRemoveMessage(this.server);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!canSeeMessages(p, pardonVisibility)) continue;
            String msg = message;
            if (p.getUniqueId().equals(remover)) {
                msg = msg.replace(REMOVER, "&lYou");
            } else {
                Player player = Bukkit.getPlayer(this.remover);
                if (Enforcer.getInstance().getSettingsManager().isUsingDisplayNames()) {
                    msg = msg.replace(Variables.REMOVER, player.getDisplayName());
                } else {
                    msg = msg.replace(Variables.REMOVER, getRemoverName());
                }
            }
            p.sendMessage(Utils.color(msg));
        }
    }
    
    public final String formatMessage() {
        String message = Messages.PUNISH_FORMAT;
        message = message.replace(VISIBILITY, visibility.getPrefix());
        message = message.replace(PREFIX, server.toUpperCase());
        message = message.replace(TARGET, getTargetName());
        message = message.replace(REASON, reason);
        Player t = Bukkit.getPlayer(this.target);
        if (t != null) {
            message = message.replace(TARGET_STATUS, "&2");
        } else {
            message = message.replace(TARGET_STATUS, "&c");
        }
        message = message.replace("{id}", id + "");
        message = replaceExpireVariables(message);
        message = replacePunishmentVariables(message);
        return Utils.color(message);
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
    
    public void setRemover(UUID remover) {
        this.remover = remover;
    }
    
    public UUID getRemover() {
        return remover;
    }
    
    public void setRemoverName(String name) {
        this.removerName = name;
    }
    
    public String getRemoverName() {
        if (removerName == null) {
            PlayerInfo info = Enforcer.getInstance().getPlayerManager().getPlayerInfo(this.remover);
            if (info != null) {
                removerName = info.getLastName();
            }
            
            if (removerName == null) {
                OfflinePlayer target = Bukkit.getOfflinePlayer(this.remover);
                if (target != null) {
                    removerName = target.getName();
                }
            }
        }
        return removerName;
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
    
    private static void searchClasses(List<Field> list, Class<? extends Punishment> clazz) {
        if (clazz.getSuperclass() != null) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass.getName().toLowerCase().contains("punishment")) {
                searchClasses(list, (Class<? extends Punishment>) clazz.getSuperclass());
            }
        }
        
        list.addAll(Arrays.asList(clazz.getDeclaredFields()));
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
            if (!expireable.isExpired()) {
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
        return message;
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
    
    public boolean wasOffline() {
        return offline;
    }
    
    public void setOffline(boolean offline) {
        this.offline = offline;
    }
    
    public boolean isTrainingPunishment() {
        return trainingMode;
    }
    
    public void setTrainingMode(boolean trainingMode) {
        this.trainingMode = trainingMode;
    }
    
    public static long calculateLength(String rawText) {
        String expireTime = "P";
        String time = rawText.toUpperCase();
        String[] a = time.split("D");
    
        if (a.length == 1) {
            expireTime += a[0].contains("H") || a[0].contains("M") || a[0].contains("S") ? "T" + a[0] : a[0] + "d";
        } else if (a.length == 2) {
            expireTime = a[0] + "dT" + a[1];
        }
    
        return Duration.parse(expireTime).toMillis();
    }
    
    public static long calculateExpireDate(long currentDate, String rawText) {
        return calculateLength(rawText) + currentDate;
    }
    
    public int compareTo(Punishment o) {
        return Long.compare(this.getDate(), o.getDate());
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
    
    public boolean isPurgatory() {
        return purgatory;
    }
}