package com.firestar311.enforcer;

import com.firestar311.enforcer.manager.SettingsManager;
import com.firestar311.enforcer.manager.TrainingModeManager;
import com.firestar311.enforcer.modules.prison.*;
import com.firestar311.enforcer.modules.punishments.PunishmentManager;
import com.firestar311.enforcer.modules.punishments.cmds.*;
import com.firestar311.enforcer.modules.punishments.listeners.PlayerChatListener;
import com.firestar311.enforcer.modules.punishments.listeners.PlayerJoinListener;
import com.firestar311.enforcer.modules.punishments.type.abstraction.MutePunishment;
import com.firestar311.enforcer.modules.punishments.type.abstraction.Punishment;
import com.firestar311.enforcer.modules.punishments.type.interfaces.Expireable;
import com.firestar311.enforcer.modules.reports.ReportCommands;
import com.firestar311.enforcer.modules.reports.ReportManager;
import com.firestar311.enforcer.modules.rules.RuleCommand;
import com.firestar311.enforcer.modules.rules.RuleManager;
import com.firestar311.enforcer.util.*;
import com.firestar311.lib.player.PlayerInfo;
import com.firestar311.lib.player.PlayerManager;
import com.firestar311.lib.util.Utils;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public final class Enforcer extends JavaPlugin {

    private PunishmentManager punishmentManager;
    private PrisonManager prisonManager;
    private RuleManager ruleManager;
    private TrainingModeManager trainingModeManager;
    private ReportManager reportManager;
    private SettingsManager settingsManager;
    private Permission permission;
    
    private static Enforcer instance;
    
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        this.settingsManager = new SettingsManager(this);
        this.punishmentManager = new PunishmentManager(this);
        this.punishmentManager.loadPunishmentData();
        this.prisonManager = new PrisonManager(this);
        this.prisonManager.loadPrisonData();
        this.ruleManager = new RuleManager(this);
        this.ruleManager.loadRuleData();
        this.trainingModeManager = new TrainingModeManager(this);
        this.trainingModeManager.loadTrainingData();
        this.reportManager = new ReportManager(this);
        this.reportManager.loadReports();
        this.registerCommands(new PunishmentCommands(this), "punish", "ban", "tempban", "mute", "tempmute", "warn", "kick", "jail", "punishment");
        this.registerCommands(new PardonCommands(this), "unban", "unmute", "unjail", "pardon");
        this.registerCommands(new HistoryCommands(this), "history", "staffhistory");
        this.getCommand("prison").setExecutor(new PrisonCommand(this));
        this.getCommand("punishmentinfo").setExecutor(new PunishmentInfoCommand(this));
        this.getCommand("moderatorrules").setExecutor(new RuleCommand(this));
        this.registerCommands(new ReportCommands(this), "report", "reportadmin");
        this.getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerPrisonListener(this), this);
        
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (rsp != null) {
            this.permission = rsp.getProvider();
        } else {
            getLogger().severe("Could not find a Vault permissions provider, defaulting to regular permissions.");
        }
        
        new BukkitRunnable() {
            public void run() {
                Set<Punishment> punishments = getPunishmentManager().getActivePunishments();
                for (Punishment punishment : punishments) {
                    if (punishment instanceof Expireable) {
                        Expireable expireable = ((Expireable) punishment);
                        if (expireable.isExpired()) {
                            expireable.onExpire();
                            
                            if (punishment instanceof MutePunishment) {
                                Player p = Bukkit.getPlayer(punishment.getTarget());
                                if (p != null) {
                                    p.sendMessage(Utils.color("&aYour temporary mute has expired."));
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(this, 1200, 20);
    }

    public void onDisable() {
        this.punishmentManager.savePunishmentData();
        this.prisonManager.savePrisonData();
        this.ruleManager.saveRuleData();
        this.trainingModeManager.saveTrainingData();
        this.reportManager.saveReports();
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.color("&cOnly players may use that command."));
            return true;
        }
    
        Player player = ((Player) sender);
    
        if (!player.hasPermission(Perms.ENFORCER_ADMIN)) {
            player.sendMessage(Utils.color("&cInsufficient permission"));
            return true;
        }
    
        if (args.length == 0) {
            player.sendMessage(Utils.color("&aEnforcer Information"));
            player.sendMessage(Utils.color("&7Version: &e" + this.getDescription().getVersion()));
            player.sendMessage(Utils.color("&7Author: &eFirestar311"));
            player.sendMessage(Utils.color("&7---Settings---"));
            player.sendMessage(Utils.color("&7Using Display Names: &e" + getSettingsManager().isUsingDisplayNames()));
            player.sendMessage(Utils.color("&7Must Confirm Punishments: &e" + getSettingsManager().mustConfirmPunishments()));
            player.sendMessage(Utils.color("&7Prefix: &e" + getSettingsManager().getPrefix()));
            player.sendMessage(Utils.color("&7Server Name : &e" + getSettingsManager().getServerName()));
            return true;
        }
    
        if (Utils.checkCmdAliases(args, 0, "settings", "s")) {
            if (Utils.checkCmdAliases(args, 1, "toggledisplaynames", "tdn")) {
                if (!player.hasPermission(Perms.SETTINGS_DISPLAYNAMES)) {
                    player.sendMessage(Utils.color("&cYou do not have permission to toggle display names."));
                    return true;
                }
                getSettingsManager().setUsingDisplayNames(!getSettingsManager().isUsingDisplayNames());
                String message = Messages.USING_DISPLAYNAMES;
                message = message.replace(Variables.DISPLAY, getSettingsManager().isUsingDisplayNames() + "");
                sendOutputMessage(player, message);
            } else if (Utils.checkCmdAliases(args, 1, "trainingmode", "tm")) {
                if (!player.hasPermission(Perms.SETTINGS_TRAINING_MODE)) {
                    player.sendMessage(Utils.color("&cYou do not have permission to toggle training mode."));
                    return true;
                }
            
                if (args.length > 1) {
                    if (Utils.checkCmdAliases(args, 2, "global", "g")) {
                        if (!player.hasPermission(Perms.SETTINGS_TRAINING_MODE_GLOBAL)) {
                            player.sendMessage(Utils.color("&cYou cannot change global training mode status."));
                            return true;
                        }
                        getTrainingModeManager().setGlobalTrainingMode(!getTrainingModeManager().getGlobalTrainingMode());
                        String message = Messages.TRAINING_MODE_GLOBAL;
                    
                        message = message.replace(Variables.DISPLAY, getTrainingModeManager().getGlobalTrainingMode() + "");
                        sendOutputMessage(player, message);
                    } else {
                        if (!player.hasPermission(Perms.SETTINGS_TRAINING_MODE_INDIVIDUAL)) {
                            player.sendMessage("&cYou cannot change the training mode for individual players");
                            return true;
                        }
                        if (args.length > 2) {
                            PlayerInfo target = getServer().getServicesManager().getRegistration(PlayerManager.class).getProvider().getPlayerInfo(args[2]);
                            if (target != null) {
                                boolean var = getTrainingModeManager().toggleTrainingMode(target.getUuid());
                                String message = Messages.TRAINING_MODE_INDIVIDUAL;
                            
                                message = message.replace(Variables.DISPLAY, var + "");
                                message = message.replace(Variables.TARGET, target.getLastName());
                                sendOutputMessage(player, message);
                            } else {
                                player.sendMessage(Utils.color("&cThe target you provided is invalid."));
                            }
                        }
                    }
                }
            } else if (Utils.checkCmdAliases(args, 1, "confirmpunishments", "cp")) {
                if (!player.hasPermission(Perms.SETTINGS_CONFIRM_PUNISHMENTS)) {
                    player.sendMessage(Utils.color("&cYou cannot change the confirm punishments setting."));
                    return true;
                }
                getSettingsManager().setConfirmPunishments(!getSettingsManager().mustConfirmPunishments());
                String message = Messages.SETTING_CONFIRMPUNISHMENTS;
                message = message.replace(Variables.DISPLAY, getSettingsManager().mustConfirmPunishments() + "");
                sendOutputMessage(player, message);
            } else if (Utils.checkCmdAliases(args, 1, "prefix")) {
                if (!player.hasPermission(Perms.SETTINGS_PREFIX)) {
                    player.sendMessage(Utils.color("&cYou cannot change the prefix."));
                    return true;
                }
            
                if (!(args.length > 0)) {
                    player.sendMessage(Utils.color("&cYou must provide a prefix to set."));
                    return true;
                }
            
                getSettingsManager().setPrefix(args[2]);
                player.sendMessage(Utils.color("&aYou set the prefix to " + getSettingsManager().getPrefix()));
            } else if (Utils.checkCmdAliases(args, 1, "server")) {
                if (!player.hasPermission(Perms.SETTINGS_SERVER)) {
                    player.sendMessage(Utils.color("&cYou cannot change the server."));
                    return true;
                }
            
                if (!(args.length > 0)) {
                    player.sendMessage(Utils.color("&cYou must provide a server name to set."));
                    return true;
                }
            
                getSettingsManager().setServerName(args[2]);
                player.sendMessage(Utils.color("&aYou set the server name to " + getSettingsManager().getServerName()));
            }
        }
        
        
        return true;
    }
    
    private void registerCommands(CommandExecutor executor, String... cmds) {
        for (String c : cmds) {
            getCommand(c).setExecutor(executor);
        }
    }
    
    public static long convertTime(String units, long rawLength) {
        if (!units.equals("")) {
            if (units.equalsIgnoreCase("seconds") || units.equalsIgnoreCase("second") || units.equalsIgnoreCase("s")) {
                return TimeUnit.SECONDS.toMillis(rawLength);
            }
            if (units.equalsIgnoreCase("minutes") || units.equalsIgnoreCase("minute") || units.equalsIgnoreCase("min")) {
                return TimeUnit.MINUTES.toMillis(rawLength);
            }
            if (units.equalsIgnoreCase("hours") || units.equalsIgnoreCase("hour") || units.equalsIgnoreCase("h")) {
                return TimeUnit.HOURS.toMillis(rawLength);
            }
            if (units.equalsIgnoreCase("days") || units.equalsIgnoreCase("day") || units.equalsIgnoreCase("d")) {
                return TimeUnit.DAYS.toMillis(rawLength);
            }
            if (units.equalsIgnoreCase("weeks") || units.equalsIgnoreCase("week") || units.equalsIgnoreCase("w")) {
                return TimeUnit.DAYS.toMillis(rawLength) * 7;
            }
            if (units.equalsIgnoreCase("months") || units.equalsIgnoreCase("month") || units.equalsIgnoreCase("m")) {
                return TimeUnit.DAYS.toMillis(rawLength) * 30;
            }
            if (units.equalsIgnoreCase("years") || units.equalsIgnoreCase("year") || units.equalsIgnoreCase("y")) {
                return TimeUnit.DAYS.toMillis(rawLength) * 365;
            }
        }
        return (long) 0;
    }
    
    public static Enforcer getInstance() {
        return instance;
    }
    
    public Permission getPermission() {
        return permission;
    }
    
    public ReportManager getReportManager() {
        return reportManager;
    }
    
    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }
    
    public PrisonManager getPrisonManager() {
        return prisonManager;
    }
    
    public RuleManager getRuleManager() {
        return ruleManager;
    }
    
    public TrainingModeManager getTrainingModeManager() {
        return trainingModeManager;
    }
    
    public SettingsManager getSettingsManager() {
        return settingsManager;
    }
    
    public PlayerManager getPlayerManager() {
        return getServer().getServicesManager().getRegistration(PlayerManager.class).getProvider();
    }
    
    private void sendOutputMessage(Player player, String message) {
        Messages.sendOutputMessage(player, message, this);
    }
}