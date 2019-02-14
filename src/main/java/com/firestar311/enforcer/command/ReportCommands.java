package com.firestar311.enforcer.command;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.model.reports.Report;
import com.firestar311.enforcer.util.*;
import com.firestar311.lib.player.PlayerInfo;
import com.firestar311.lib.util.Utils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.List;

public class ReportCommands implements CommandExecutor {

    private Enforcer plugin;
    
    public ReportCommands(Enforcer plugin) {
        this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.color("&cOnly players may use that command."));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!(args.length > 0)) {
            player.sendMessage(Utils.color("&cYou do not have enough arguments."));
            return true;
        }
        
        if (cmd.getName().equalsIgnoreCase("report")) {
            PlayerInfo targetInfo = plugin.getDataManager().getInfo(args[0]);
            if (targetInfo != null) {
                String reason = StringUtils.join(args, " ", 1, args.length);
                if (StringUtils.isEmpty(reason)) {
                    player.sendMessage(Utils.color("&cYou must provide a valid reason."));
                    return true;
                }
    
                Report report = new Report(player.getUniqueId(), targetInfo.getUuid(), player.getLocation(), reason);
                plugin.getReportManager().addReport(report);
    
                String format = Messages.REPORT_CREATE;
                format = format.replace(Variables.TARGET, targetInfo.getLastName()).replace(Variables.REASON, reason).replace(Variables.ACTOR, player.getName());
                format = format.replace("{id}", report.getId() + "");
    
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.hasPermission(Perms.NOTIFY_PUNISHMENTS)) {
                        p.sendMessage(Utils.color(format));
                    }
                }
    
                player.sendMessage(Utils.color("&aA report has been filed against &b" + targetInfo.getLastName()));
            } else if (Utils.isInt(args[0])){
                Report report = plugin.getReportManager().getReport(Integer.parseInt(args[0]));
                if (report == null) {
                    player.sendMessage(Utils.color("&cYou provided an invalid report id."));
                    return true;
                }
                
                //TODO
            } else if (Utils.checkCmdAliases(args, 0, "list", "l")) {
                List<Report> reports = plugin.getReportManager().getReportsByReporter(player.getUniqueId());
                if (reports.isEmpty()) {
                    player.sendMessage(Utils.color("&cCould not find any reports created by you."));
                    return true;
                }
                //TODO
            }
        } else if (cmd.getName().equalsIgnoreCase("reportadmin")) {
            player.sendMessage(Utils.color("&cThat command is not implemented yet."));
        }
        
        return true;
    }
}