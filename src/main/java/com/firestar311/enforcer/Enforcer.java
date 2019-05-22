package com.firestar311.enforcer;

import com.firestar311.enforcer.command.*;
import com.firestar311.enforcer.hooks.CustomItemsHook;
import com.firestar311.enforcer.listener.*;
import com.firestar311.enforcer.manager.*;
import com.firestar311.enforcer.model.punishment.abstraction.MutePunishment;
import com.firestar311.enforcer.model.punishment.abstraction.Punishment;
import com.firestar311.enforcer.model.punishment.interfaces.Expireable;
import com.firestar311.lib.player.PlayerManager;
import com.firestar311.lib.util.Utils;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
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
    private Permission permission;
    private CustomItemsHook customItemsHook;
    
    private static Enforcer instance;
    private SettingsManager settingsManager;
    
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
        this.getCommand("enforcer").setExecutor(new EnforcerCommand(this));
        this.registerCommands(new PunishmentCommands(this), "punish", "ban", "tempban", "mute", "tempmute", "warn", "kick", "jail");
        this.registerCommands(new PardonCommands(this), "unban", "unmute", "unjail", "pardon");
        this.registerCommands(new HistoryCommands(this), "history", "staffhistory");
        this.getCommand("prison").setExecutor(new PrisonCommand(this));
        this.getCommand("punishmentinfo").setExecutor(new PunishmentInfoCommand(this));
        this.getCommand("moderatorrules").setExecutor(new RuleCommand(this));
        this.registerCommands(new ReportCommands(this), "report", "reportadmin");
        this.getServer().getPluginManager().registerEvents(new PlayerBanJoinListener(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerPrisonListener(this), this);
        this.customItemsHook = new CustomItemsHook(this);
        
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
    
    public CustomItemsHook getCustomItemsHook() {
        return customItemsHook;
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
}