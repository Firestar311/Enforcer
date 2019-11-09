package com.stardevmc.enforcer;

import com.firestar311.lib.player.PlayerManager;
import com.firestar311.lib.util.Utils;
import com.stardevmc.enforcer.manager.SettingsManager;
import com.stardevmc.enforcer.modules.history.HistoryModule;
import com.stardevmc.enforcer.modules.pardon.PardonModule;
import com.stardevmc.enforcer.modules.prison.Prison;
import com.stardevmc.enforcer.modules.prison.PrisonModule;
import com.stardevmc.enforcer.modules.punishments.PunishmentModule;
import com.stardevmc.enforcer.modules.punishments.actor.ConsoleActor;
import com.stardevmc.enforcer.modules.punishments.actor.PlayerActor;
import com.stardevmc.enforcer.modules.punishments.target.*;
import com.stardevmc.enforcer.modules.punishments.type.abstraction.MutePunishment;
import com.stardevmc.enforcer.modules.punishments.type.abstraction.Punishment;
import com.stardevmc.enforcer.modules.punishments.type.impl.*;
import com.stardevmc.enforcer.modules.punishments.type.interfaces.Expireable;
import com.stardevmc.enforcer.modules.reports.*;
import com.stardevmc.enforcer.modules.rules.RuleModule;
import com.stardevmc.enforcer.modules.rules.rule.*;
import com.stardevmc.enforcer.modules.training.TrainingModule;
import com.stardevmc.enforcer.modules.watchlist.*;
import com.stardevmc.enforcer.util.*;
import com.stardevmc.enforcer.util.evidence.Evidence;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public final class Enforcer extends JavaPlugin {
    
    private static Enforcer instance;

    static {
        Utils.registerConfigClasses(Report.class, Prison.class, ConsoleActor.class, PlayerActor.class, PlayerTarget.class, IPTarget.class, IPListTarget.class, BlacklistPunishment.class, JailPunishment.class, KickPunishment.class, PermanentBan.class, PermanentMute.class, WarnPunishment.class, Rule.class, RuleOffense.class, RulePunishment.class, Evidence.class, WatchlistEntry.class, WatchlistNote.class);
    }

    private HistoryModule historyModule;
    private PardonModule pardonModule;
    private Permission permission;
    private PrisonModule prisonModule;
    private PunishmentModule punishmentModule;
    private ReportModule reportModule;
    private RuleModule ruleModule;
    private SettingsManager settingsManager;
    private TrainingModule trainingModule;
    private WatchlistModule watchlistModule;
    
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
            } else if (Utils.checkCmdAliases(args, 1, "replaceactorname", "ran")) {
                if (!player.hasPermission(Perms.SETTINGS_ACTOR_NAME)) {
                    player.sendMessage("&cYou cannot change that setting.");
                    return true;
                }
                
                getSettingsManager().setReplaceActorName(!getSettingsManager().getReplaceActorName());
                player.sendMessage("&aYou changed the setting &breplace actor names &ato &e" + getSettingsManager().getReplaceActorName()); //TODO Proper message
            }
        }
        
        
        return true;
    }
    
    public void onDisable() {
        this.punishmentModule.desetup();
        this.prisonModule.desetup();
        this.ruleModule.desetup();
        this.trainingModule.desetup();
        this.reportModule.desetup();
    }
    
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        this.settingsManager = new SettingsManager(this);
        this.punishmentModule = new PunishmentModule(this, "punish", "ban", "tempban", "mute", "tempmute", "warn", "kick", "jail", "punishment", "blacklist");
        this.prisonModule = new PrisonModule(this, "prison");
        this.ruleModule = new RuleModule(this, "moderatorrules");
        this.historyModule = new HistoryModule(this, "history", "staffhistory");
        this.pardonModule = new PardonModule(this, "unban", "unmute", "unjail", "pardon", "unblacklist");
        this.trainingModule = new TrainingModule(this, "trainingmode");
        this.reportModule = new ReportModule(this, "report", "reportadmin");
        this.watchlistModule = new WatchlistModule(this, "watchlist", "quickteleport");
        this.punishmentModule.setup();
        this.prisonModule.setup();
        this.ruleModule.setup();
        this.reportModule.setup();
        this.trainingModule.setup();
        this.historyModule.setup();
        this.watchlistModule.setup();
        
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
                                Player p = punishment.getTarget().getPlayer();
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
    
    public PunishmentModule getPunishmentModule() {
        return punishmentModule;
    }
    
    public SettingsManager getSettingsManager() {
        return settingsManager;
    }
    
    private void sendOutputMessage(Player player, String message) {
        Messages.sendOutputMessage(player, message, this);
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
    
    public PrisonModule getPrisonModule() {
        return prisonModule;
    }
    
    public RuleModule getRuleModule() {
        return ruleModule;
    }
    
    public TrainingModule getTrainingModule() {
        return trainingModule;
    }
    
    public PlayerManager getPlayerManager() {
        return getServer().getServicesManager().getRegistration(PlayerManager.class).getProvider();
    }
    
    public HistoryModule getHistoryModule() {
        return historyModule;
    }
    
    public PardonModule getPardonModule() {
        return pardonModule;
    }
    
    public WatchlistModule getWatchlistModule() {
        return watchlistModule;
    }
}