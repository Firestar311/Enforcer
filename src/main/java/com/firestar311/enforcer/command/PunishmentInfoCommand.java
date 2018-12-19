package com.firestar311.enforcer.command;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.model.punishment.abstraction.Punishment;
import com.firestar311.enforcer.util.Perms;
import com.firestar311.lib.audit.AuditEntry;
import com.firestar311.lib.pagination.Paginator;
import com.firestar311.lib.pagination.PaginatorFactory;
import com.firestar311.lib.util.Utils;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
import java.util.*;

public class PunishmentInfoCommand implements CommandExecutor {

    private Enforcer plugin;
    
    private Map<UUID, Paginator<AuditEntry>> auditPaginators = new HashMap<>();
    
    public PunishmentInfoCommand(Enforcer plugin){
        this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.color("&cOnly players may use that command"));
            return true;
        }
        
        Player player = ((Player) sender);
        
        if (!player.hasPermission("enforcer.info.punishment")) {
            player.sendMessage("&cYou do not have enough permission");
            return true;
        }
        
        if (!(args.length > 0)) {
            player.sendMessage(Utils.color("&cYou do not have enough arguments."));
            return true;
        }
        
        if (Utils.checkCmdAliases(args, 0, "page", "p")) {
            if (!(args.length > 1)) {
                player.sendMessage(Utils.color("&cYou must provide a page number."));
                return true;
            }
            
            if (!this.auditPaginators.containsKey(player.getUniqueId())) {
                player.sendMessage(Utils.color("&cYou do not have any results yet."));
                return true;
            }
            
            int page;
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(Utils.color("&cThe value for the page you provided is not a number."));
                return true;
            }
            
            Paginator<AuditEntry> paginator = this.auditPaginators.get(player.getUniqueId());
            paginator.display(player, page);
        }
    
        int id;
        try {
            id = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(Utils.color("&cThe value for the id is not a valid number."));
            return true;
        }
    
        Punishment punishment = plugin.getDataManager().getPunishment(id);
        
        if (punishment == null) {
            player.sendMessage(Utils.color("&cCould not find a punishment with that id."));
            return true;
        }
        
        if (Utils.checkCmdAliases(args, 1, "auditlog", "al")) {
            PaginatorFactory<AuditEntry> factory = new PaginatorFactory<>();
            factory.setMaxElements(7).setHeader("&a-=Audit log for Punishment " + punishment.getId() + "=- &e({pagenumber}/{totalpages})").setFooter("&aType /punishmentinfo page {nextpage} for more");
            punishment.getAuditLog().getAuditEntries().forEach(factory::addElement);
            Paginator<AuditEntry> paginator = factory.build();
            this.auditPaginators.put(player.getUniqueId(), paginator);
    
            paginator.display(player, 1);
        }
        return true;
    }
}