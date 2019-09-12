package com.firestar311.enforcer;

import com.firestar311.enforcer.manager.SettingsManager;
import com.firestar311.enforcer.modules.history.HistoryManager;
import com.firestar311.enforcer.modules.history.HistoryModule;
import com.firestar311.enforcer.modules.pardon.*;
import com.firestar311.enforcer.modules.prison.PrisonManager;
import com.firestar311.enforcer.modules.prison.PrisonModule;
import com.firestar311.enforcer.modules.punishments.PunishmentManager;
import com.firestar311.enforcer.modules.punishments.PunishmentModule;
import com.firestar311.enforcer.modules.punishments.type.abstraction.MutePunishment;
import com.firestar311.enforcer.modules.punishments.type.abstraction.Punishment;
import com.firestar311.enforcer.modules.punishments.type.interfaces.Expireable;
import com.firestar311.enforcer.modules.reports.ReportManager;
import com.firestar311.enforcer.modules.reports.ReportModule;
import com.firestar311.enforcer.modules.rules.RuleManager;
import com.firestar311.enforcer.modules.rules.RuleModule;
import com.firestar311.enforcer.modules.training.TrainingManager;
import com.firestar311.enforcer.modules.training.TrainingModule;
import com.firestar311.enforcer.util.*;
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

    private PunishmentModule punishmentModule;
    private PrisonModule prisonModule;
    private RuleModule ruleModule;
    private TrainingModule trainingModule;
    private ReportModule reportModule;
    private SettingsManager settingsManager;
    private Permission permission;
    private HistoryModule historyModule;
    private PardonModule pardonModule;
    
    private static Enforcer instance;
    
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        this.settingsManager = new SettingsManager(this);
        this.punishmentModule = new PunishmentModule(this, "punishments", new PunishmentManager(this), "punish", "ban", "tempban", "mute", "tempmute", "warn", "kick", "jail", "punishment");
        this.prisonModule = new PrisonModule(this, "prison", new PrisonManager(this), "prison");
        this.ruleModule = new RuleModule(this, "rules", new RuleManager(this), "moderatorrules");
        this.historyModule = new HistoryModule(this, "history", new HistoryManager(this), "history", "staffhistory");
        this.pardonModule = new PardonModule(this, "pardon", new PardonManager(this), "unban", "unmute", "unjail", "pardon");
        this.trainingModule = new TrainingModule(this, "training", new TrainingManager(this), "trainingmode");
        this.reportModule = new ReportModule(this, "reports", new ReportManager(this), "report", "reportadmin");
        this.punishmentModule.setup();
        this.prisonModule.setup();
        this.ruleModule.setup();
        this.reportModule.setup();
        this.trainingModule.setup();
        this.historyModule.setup();
        this.registerCommands(new PardonCommands(this), "unban", "unmute", "unjail", "pardon");
        
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (rsp != null) {
            this.permission = rsp.getProvider();
        } else {
            getLogger().severe("Could not find a Vault permissions provider, defaulting to regular permissions.");
        }
        
        new BukkitRunnable() {
            public void run() {
                Set<Punishment> punishments = getPunishmentModule().getManager().getActivePunishments();
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
        this.punishmentModule.desetup();
        this.prisonModule.desetup();
        this.ruleModule.desetup();
        this.trainingModule.desetup();
        this.reportModule.desetup();
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
        
        if (Utils.checkCmdAliases(args, 0, "testtime", "tt")) {
            if (!player.isOp()) {
                player.sendMessage(Utils.color("&cOnly operators can use that command."));
                return true;
            }
            
            if (args.length != 2) {
                player.sendMessage(Utils.color("You must provide a length of time"));
                return true;
            }
            
            long oldWayLength = -1;
            long newWayLength = -1;
            
            try {
                oldWayLength = Punishment.calculateLength(args[1]);
            } catch (Exception e) {}
            
            try {
                newWayLength = Utils.parseTime(args[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            player.sendMessage(Utils.color("&aYou provided the time argument: &b" + args[1]));
            if (oldWayLength == -1) {
                player.sendMessage(Utils.color("&aThe Old Way had an error"));
            } else {
                player.sendMessage(Utils.color("&aThe Old Way got &b" + oldWayLength + "&e(" + Utils.formatTime(oldWayLength) + ")"));
            }
            
            if (newWayLength == -1) {
                player.sendMessage(Utils.color("&aThe New Way had an error"));
            } else {
                player.sendMessage(Utils.color("&aThe New Way got &b" + newWayLength + " &e(" + Utils.formatTime(newWayLength) + ")"));
            }
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
    
    public ReportModule getReportModule() {
        return reportModule;
    }
    
    public PunishmentModule getPunishmentModule() {
        return punishmentModule;
    }
    
    public PrisonModule getPrisonModule() {
        return prisonModule;
    }
    
    public RuleModule getRuleModule() {
        return ruleModule;
    }
    
    public TrainingModule getTrainingModule() {
        return trainingModule;
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
    
    public HistoryModule getHistoryModule() {
        return historyModule;
    }
    
    public PardonModule getPardonModule() {
        return pardonModule;
    }
}