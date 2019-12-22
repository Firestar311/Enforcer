package com.stardevmc.enforcer.modules.reports;

import com.firestar311.lib.pagination.Paginator;
import com.firestar311.lib.pagination.PaginatorFactory;
import com.firestar311.lib.player.User;
import com.firestar311.lib.util.Utils;
import com.stardevmc.enforcer.Enforcer;
import com.stardevmc.enforcer.modules.punishments.actor.Actor;
import com.stardevmc.enforcer.modules.punishments.actor.PlayerActor;
import com.stardevmc.enforcer.modules.punishments.target.PlayerTarget;
import com.stardevmc.enforcer.modules.punishments.target.Target;
import com.stardevmc.enforcer.modules.punishments.type.abstraction.Punishment;
import com.stardevmc.enforcer.modules.reports.enums.ReportOutcome;
import com.stardevmc.enforcer.modules.reports.enums.ReportStatus;
import com.stardevmc.enforcer.modules.rules.rule.Rule;
import com.stardevmc.enforcer.util.*;
import com.stardevmc.enforcer.util.evidence.Evidence;
import com.stardevmc.enforcer.util.evidence.EvidenceType;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public class ReportCommands implements CommandExecutor {
    
    private Enforcer plugin;
    
    public ReportCommands(Enforcer plugin) {
        this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.color(Messages.ONLY_PLAYERS_CMD));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!(args.length > 0)) {
            player.sendMessage(Utils.color(Messages.NOT_ENOUGH_ARGS));
            return true;
        }
        
        ReportManager reportManager = plugin.getReportModule().getManager();
        
        if (cmd.getName().equalsIgnoreCase("report")) {
            if (Utils.isInt(args[0])) {
                Report report = reportManager.getReport(Integer.parseInt(args[0]));
                if (report == null) {
                    player.sendMessage(Utils.color("&cYou provided an invalid report id."));
                    return true;
                }
                
                if (report.getReporter() instanceof PlayerActor) {
                    if (!(((PlayerActor) report.getReporter()).getUniqueId().equals(player.getUniqueId()) || player.hasPermission(Perms.STAFF_PERMISSION))) {
                        player.sendMessage(Utils.color("&cYou must be staff or the reporter to view a report."));
                        return true;
                    }
                }
                
                if (args.length == 1) {
                    player.sendMessage(Utils.color("&6---------Report Info---------"));
                    player.sendMessage(Utils.color("&7Report ID: &d" + report.getId()));
                    player.sendMessage(Utils.color("&7Report Target: &3" + report.getTarget().getName()));
                    player.sendMessage(Utils.color("&7Report Reason: &9" + report.getReason()));
                    player.sendMessage(Utils.color("&7Report Status: &b" + report.getStatus().getColor() + report.getStatus().name()));
                    player.sendMessage(Utils.color("&7Report Outcome: &b" + report.getOutcome().getColor() + report.getOutcome().name()));
                } else {
                    if (Utils.checkCmdAliases(args, 1, "setevidence", "se")) {
                        if (args.length == 3) {
                            report.setEvidence(new Evidence(0, player.getUniqueId(), EvidenceType.PLAYER, args[2]));
                            player.sendMessage(Utils.color("&aYou added evidence to the report against" + report.getTarget().getName()));
                        } else {
                            player.sendMessage(Utils.color(Messages.NOT_ENOUGH_ARGS));
                            return true;
                        }
                    } else if (Utils.checkCmdAliases(args, 1, "cancel", "c")) {
                        if (report.getReporter() instanceof PlayerActor) {
                            if (!((PlayerActor) report.getReporter()).getUniqueId().equals(player.getUniqueId())) {
                                player.sendMessage(Utils.color("&cYou can only cancel reports created by you."));
                                return true;
                            }
                        }
                        
                        if (report.getStatus().equals(ReportStatus.CANCELLED)) {
                            player.sendMessage(Utils.color("&cThat report is already cancelled."));
                            return true;
                        }
                        
                        if (report.getOutcome().equals(ReportOutcome.ACCEPTED) || report.getOutcome().equals(ReportOutcome.DENIED)) {
                            player.sendMessage(Utils.color("&cThat report has already been decided on, you cannot cancel it now."));
                            return true;
                        }
                        
                        report.setOutcome(ReportOutcome.CANCELLED);
                        report.setStatus(ReportStatus.CANCELLED);
                        String format = Messages.REPORT_CANCEL;
                        format = format.replace(Variables.TARGET, report.getTarget().getName()).replace(Variables.ACTOR, player.getName());
                        format = format.replace("{id}", report.getId() + "");
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p.hasPermission(Perms.NOTIFY_PUNISHMENTS)) {
                                p.sendMessage(Utils.color(format));
                            }
                        }
                        
                        player.sendMessage(Utils.color("&aYou cancelled the report with the id &b" + report.getId()));
                    }
                }
            } else {
                if (Utils.checkCmdAliases(args, 0, "list", "l")) {
                    List<Report> reports = reportManager.getReportsByReporter(player.getUniqueId());
                    if (reports.isEmpty()) {
                        player.sendMessage(Utils.color("&cCould not find any reports created by you."));
                        return true;
                    }
                    
                    PaginatorFactory<Report> factory = new PaginatorFactory<>();
                    factory.setHeader("&7List of reports by you &e({pagenumber}/{totalpages})").setFooter("&6Type /reports list {nextpage} for more.").setMaxElements(7);
                    reports.forEach(factory::addElement);
                    Paginator<Report> paginator = factory.build();
                    if (args.length > 1) {
                        paginator.display(player, args[1]);
                    } else {
                        paginator.display(player, 1);
                    }
                } else {
                    User targetInfo = plugin.getPlayerManager().getUser(args[0]);
                    if (targetInfo == null) {
                        player.sendMessage(Utils.color(Messages.COULD_NOT_FIND_PLAYER));
                        return true;
                    }
                    String rawReason = StringUtils.join(args, " ", 1, args.length);
                    if (StringUtils.isEmpty(rawReason)) {
                        player.sendMessage(Utils.color("&cYou must provide a valid reason."));
                        return true;
                    }
                    
                    Rule rule = plugin.getRuleModule().getManager().getRule(rawReason);
                    String reason;
                    if (rule != null) {
                        reason = rule.getName();
                    } else {
                        reason = rawReason;
                    }
    
                    Actor actor = new PlayerActor(player.getUniqueId());
                    Target target = new PlayerTarget(targetInfo.getUniqueId());
                    
                    Report report = new Report(actor, target, player.getLocation(), reason);
                    plugin.getReportModule().getManager().addReport(report);
                    
                    String format = Messages.REPORT_CREATE;
                    format = format.replace(Variables.TARGET, targetInfo.getLastName()).replace(Variables.REASON, reason).replace(Variables.ACTOR, player.getName());
                    format = format.replace("{id}", report.getId() + "");
                    
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.hasPermission(Perms.NOTIFY_PUNISHMENTS)) {
                            p.sendMessage(Utils.color(format));
                        }
                    }
                    
                    player.sendMessage(Utils.color(Messages.reportFiledAgainst(targetInfo.getLastName())));
                    
                }
            }
        } else if (cmd.getName().equalsIgnoreCase("reportadmin")) {
            if (!player.hasPermission(Perms.REPORT_ADMIN)) {
                player.sendMessage(Messages.noPermissionCommand(Perms.REPORT_ADMIN));
                return true;
            }
            
            if (!(args.length > 0)) {
                player.sendMessage(Utils.color("&cYou must provide a subcommand."));
                return true;
            }
            
            if (Utils.checkCmdAliases(args, 0, "list", "l")) {
                Collection<Report> reports = reportManager.getReports().values();
                if (reports.isEmpty()) {
                    player.sendMessage(Utils.color("&cCould not find any reports."));
                    return true;
                }
                
                PaginatorFactory<Report> factory = new PaginatorFactory<>();
                factory.setHeader("&7List of all reports &e({pagenumber}/{totalpages})").setFooter("&6Type /reportadmin list {nextpage} for more.").setMaxElements(7);
                reports.forEach(factory::addElement);
                Paginator<Report> paginator = factory.build();
                if (args.length == 1) {
                    paginator.display(player, 1);
                } else {
                    paginator.display(player, args[1]);
                }
                return true;
            }
            
            if (!(args.length > 0)) {
                player.sendMessage(Utils.color("&cYou must provide a report id to modify"));
                return true;
            }
            
            int id;
            try {
                id = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                player.sendMessage(Utils.color("&cThe value for the report id is not a valid number."));
                return true;
            }
            
            Report report = reportManager.getReport(id);
            if (report == null) {
                player.sendMessage(Utils.color("&cYou provided an invalid report id."));
                return true;
            }
            
            if (!(args.length > 1)) {
                player.sendMessage(Utils.color("&cYou must provide a sub command."));
                return true;
            }
            
            if (Utils.checkCmdAliases(args, 1, "assign", "a")) {
                if (!player.hasPermission(Perms.REPORT_ADMIN_ASSIGN)) {
                    player.sendMessage(Messages.noPermissionCommand(Perms.REPORT_ADMIN_ASSIGN));
                    return true;
                }
                
                if (!(args.length > 1)) {
                    player.sendMessage(Utils.color("&cYou must provide a status."));
                    return true;
                }
                
                User info = plugin.getPlayerManager().getUser(args[2]);
                if (info == null) {
                    player.sendMessage(Utils.color("&cYou provided an invalid name."));
                    return true;
                }
                
                report.setAssignee(new PlayerActor(info.getUniqueId()));
                String message = Messages.REPORT_ASSIGN.replace(Variables.TARGET, info.getLastName());
                message = message.replace(Variables.ACTOR, player.getName());
                Messages.sendOutputMessage(player, message, plugin);
            } else if (Utils.checkCmdAliases(args, 1, "setstatus", "ss")) {
                if (!player.hasPermission(Perms.REPORT_ADMIN_STATUS)) {
                    player.sendMessage(Messages.noPermissionCommand(Perms.REPORT_ADMIN_STATUS));
                    return true;
                }
                
                if (!(args.length > 1)) {
                    player.sendMessage(Utils.color("&cYou must provide a status."));
                    return true;
                }
                
                ReportStatus status;
                try {
                    status = ReportStatus.valueOf(args[2].toUpperCase());
                } catch (IllegalArgumentException e) {
                    player.sendMessage(Utils.color("&cYou provided an invalid status type."));
                    return true;
                }
                
                if (report.getStatus().equals(status)) {
                    player.sendMessage(Utils.color("&cThe status you provided is the same as the current status."));
                    return true;
                }
                
                ReportStatus oldStatus = report.getStatus();
                report.setStatus(status);
                player.sendMessage(Utils.color(String.format("&aYou changed the status of the report &b%d &afrom &b%s &ato &b%s&a.", report.getId(), oldStatus, status)));
            } else if (Utils.checkCmdAliases(args, 1, "setoutcome", "so")) {
                if (!player.hasPermission(Perms.REPORT_ADMIN_OUTCOME)) {
                    player.sendMessage(Messages.noPermissionCommand(Perms.REPORT_ADMIN_OUTCOME));
                    return true;
                }
                
                if (!(args.length > 1)) {
                    player.sendMessage(Utils.color("&cYou must provide an outcome."));
                    return true;
                }
                
                ReportOutcome outcome;
                try {
                    outcome = ReportOutcome.valueOf(args[2].toUpperCase());
                } catch (IllegalArgumentException e) {
                    player.sendMessage(Utils.color("&cYou provided an invalid outcome type."));
                    return true;
                }
                
                if (report.getOutcome().equals(outcome)) {
                    player.sendMessage(Utils.color("&cThe outcome you provided is the same as the current outcome."));
                    return true;
                }
                
                ReportOutcome oldOutcome = report.getOutcome();
                report.setOutcome(outcome);
                player.sendMessage(Utils.color(String.format("&aYou changed the outcome of the report &b%d &afrom &b%s &ato &b%s&a.", report.getId(), oldOutcome, outcome)));
            } else if (Utils.checkCmdAliases(args, 1, "teleport", "tp")) {
                if (!player.hasPermission(Perms.REPORT_ADMIN_TELEPORT)) {
                    player.sendMessage(Messages.noPermissionCommand(Perms.REPORT_ADMIN_TELEPORT));
                    return true;
                }
                
                player.teleport(report.getLocation());
                player.sendMessage(Utils.color("&aYou teleported to the location of where the report was created."));
            } else if (Utils.checkCmdAliases(args, 1, "addpunishment", "ap")) {
                if (!player.hasPermission(Perms.REPORT_ADMIN_PUNISHMENT)) {
                    player.sendMessage(Messages.noPermissionCommand(Perms.REPORT_ADMIN_PUNISHMENT));
                    return true;
                }
                
                if (!(args.length > 1)) {
                    player.sendMessage(Utils.color("&cYou must provide a punishment id to add to the report."));
                    return true;
                }
                
                int puId;
                try {
                    puId = Integer.parseInt(args[2]);
                } catch (IllegalArgumentException e) {
                    player.sendMessage(Utils.color("&cThe value you provided for the punishment id is not a valid number"));
                    return true;
                }
                
                Punishment punishment = plugin.getPunishmentModule().getManager().getPunishment(puId);
                if (punishment == null) {
                    player.sendMessage(Utils.color("&cThe id you provided does not match a valid punishment"));
                    return true;
                }
                
                report.addPunishment(punishment);
                player.sendMessage(Utils.color(String.format("&aAdded the punishment &b%s &ato the report &b%s", punishment.getId(), report.getId())));
            }
        }
        
        return true;
    }
}