package com.firestar311.enforcer.command;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.model.Prison;
import com.firestar311.enforcer.model.punishment.abstraction.Punishment;
import com.firestar311.enforcer.model.punishment.type.JailPunishment;
import com.firestar311.enforcer.util.*;
import com.firestar311.lib.pagination.Paginator;
import com.firestar311.lib.pagination.PaginatorFactory;
import com.firestar311.lib.region.*;
import com.firestar311.lib.util.Utils;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PrisonCommand implements CommandExecutor, Listener {
    
    private Enforcer plugin;
    
    private SelectionManager selectionManager = new SelectionManager();
    private RegionWandToolHook toolHook;
    
    public PrisonCommand(Enforcer plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.toolHook = new RegionWandToolHook(plugin, Material.NETHER_WART);
        this.plugin.getServer().getPluginManager().registerEvents(new RegionToolListener(selectionManager, toolHook), plugin);
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.color("&cOnly players can use that command."));
            return true;
        }
        
        Player player = ((Player) sender);
        
        if (!(args.length > 0)) {
            player.sendMessage(Utils.color("&cYou do not have enough arguments."));
            return true;
        }
        
        if (!player.hasPermission(Perms.PRISON_MAIN)) {
            player.sendMessage(Utils.color("&cYou do not have permission to use that command."));
            return true;
        }
        
        if (Utils.checkCmdAliases(args, 0, "create", "c")) {
            if (!player.hasPermission(Perms.PRISON_ADD)) {
                player.sendMessage(Utils.color("&cYou do not have permission to use that command."));
                return true;
            }
            
            if (!(args.length > 1)) {
                player.sendMessage(Utils.color("&cUsage: /prison create <maxplayers> [id]"));
                return true;
            }
            
            int maxPlayers = 5;
            try {
                maxPlayers = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(Utils.color("&cYou provided an invalid number for the max players, defaulting to 5"));
            }
            
            int id = -1;
            if (args.length > 2) {
                try {
                    id = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage(Utils.color("&cYou provided an invalid number for the prison id. The id will be auto-assigned"));
                }
            }
            
            Location location = player.getLocation();
            
            Prison prison;
            if (!this.selectionManager.hasSelection(player)) {
                prison = new Prison(id, location, maxPlayers);
            } else {
                Selection selection = this.selectionManager.getSelection(player);
                if (selection.getPointA() != null && selection.getPointB() != null) {
                    prison = new Prison(id, location, maxPlayers, selection.getPointA(), selection.getPointB());
                } else {
                    prison = new Prison(id, location, maxPlayers);
                }
            }
            plugin.getDataManager().addPrison(prison);
            String message = Messages.PRISON_CREATE;
            message = message.replace(Variables.JAIL_ID, prison.getDisplayName());
            sendOutputMessage(player, message);
            
            Set<Prison> prisons = plugin.getDataManager().getPrisonsWithOverflow();
            Set<UUID> playersToAdd = new HashSet<>();
            
            for (Prison pr : prisons) {
                List<UUID> inhabitants = new LinkedList<>(pr.getInhabitants());
                calculateOverflow(prison, playersToAdd, pr, inhabitants);
            }
            
            for (UUID uuid : playersToAdd) {
                Player inhabitant = changePunishmentInfo(prison, uuid);
                if (inhabitant != null) {
                    inhabitant.teleport(prison.getLocation());
                    inhabitant.sendMessage(Utils.color("&dYou were an overflow inhabitant of your former prison, so you were moved to a newly created prison"));
                }
            }
            return true;
        }
        if (Utils.checkCmdAliases(args, 0, "pos1")) {
            if (!player.hasPermission(Perms.PRISON_SELECTION)) {
                player.sendMessage(Utils.color("&cYou do not have permission to use that command."));
                return true;
            }
            
            this.selectionManager.setPointA(player, player.getLocation());
        } else if (Utils.checkCmdAliases(args, 0, "pos2")) {
            if (!player.hasPermission(Perms.PRISON_SELECTION)) {
                player.sendMessage(Utils.color("&cYou do not have permission to use that command."));
                return true;
            }
            
            this.selectionManager.setPointB(player, player.getLocation());
        } else if (Utils.checkCmdAliases(args, 0, "list")) {
            if (!player.hasPermission(Perms.PRISON_LIST)) {
                player.sendMessage(Utils.color("&cYou do not have permission to use that command."));
                return true;
            }
            
            if (plugin.getDataManager().getPrisons().isEmpty()) {
                player.sendMessage(Utils.color("&cThere are no prisons created."));
                return true;
            }
            
            PaginatorFactory<Prison> factory = new PaginatorFactory<>();
            factory.setMaxElements(7).setHeader("&7-=List of Prisons=- &e({pagenumber}/{totalpages})").setFooter("&7Type /prison list {nextpage} for more");
            for (Prison prison : plugin.getDataManager().getPrisons()) {
                factory.addElement(prison);
            }
            Paginator<Prison> paginator = factory.build();
            if (args.length == 1) {
                paginator.display(player, 1);
            } else {
                int page;
                try {
                    page = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage(Utils.color("&cYou provided an invalid number."));
                    return true;
                }
                paginator.display(player, page);
            }
            return true;
        } else if (Utils.checkCmdAliases(args, 0, "clearselection", "cs")) {
            if (!player.hasPermission(Perms.PRISON_CLEAR_SELECTION)) {
                player.sendMessage(Utils.color("&cYou do not have permission to use that command."));
                return true;
            }
            if (!this.selectionManager.hasSelection(player)) {
                player.sendMessage(Utils.color("&cYou do not have a selection currently set."));
                return true;
            }
    
            this.selectionManager.clearSelection(player);
            player.sendMessage(Utils.color("&aCleared your selection."));
            return true;
        }
    
        ///prison <id|name> <subcommand>
        Prison prison = plugin.getDataManager().getPrisonFromString(player, args[0]);
        if (prison == null) return true;
        
        if (Utils.checkCmdAliases(args, 1, "setlocation", "sl")) {
            if (!player.hasPermission(Perms.SET_PRISON_LOCATION)) {
                player.sendMessage(Utils.color("&cYou do not have permission to use that command."));
                return true;
            }
            
            Location newLocation = player.getLocation();
            if (!prison.contains(newLocation)) {
                player.sendMessage(Utils.color("&cThat location is not within the prison bounds"));
                return true;
            }
            prison.setLocation(newLocation);
            
            String message = Messages.PRISON_SET_SPAWN;
            message = message.replace(Variables.JAIL_ID, prison.getDisplayName());
            sendOutputMessage(player, message);
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (plugin.getDataManager().isJailed(p.getUniqueId())) {
                    if (prison.isInhabitant(p.getUniqueId())) {
                        p.teleport(newLocation);
                        p.sendMessage(Utils.color("&dThe prison location was changed by &b" + player.getName() + " &dso you have been teleported to the new location."));
                    }
                }
            }
        } else if (Utils.checkCmdAliases(args, 1, "setmaxplayers", "smp")) {
            if (!player.hasPermission(Perms.PRISON_SET_MAX_PLAYERS)) {
                player.sendMessage(Utils.color("&cYou do not have permission to use that command."));
                return true;
            }
            
            if (args.length != 3) {
                player.sendMessage(Utils.color("&cUsage: /prison <id|name> setmaxplayers|smp <amount>"));
                return true;
            }
            
            int amount = 5;
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage(Utils.color("&cInvalid number provided for the amount, defaulting to 5"));
            }
            
            if (amount == prison.getMaxPlayers()) {
                player.sendMessage(Utils.color("&cThe amount you provided is the same as the current max players value."));
                return true;
            }
            
            if (amount > prison.getMaxPlayers()) {
                Set<Prison> prisons = plugin.getDataManager().getPrisonsWithOverflow();
                Set<UUID> playersToAdd = new HashSet<>();
                for (Prison pr : prisons) {
                    List<UUID> inhabitants = new LinkedList<>(pr.getInhabitants());
                    if (inhabitants.isEmpty()) continue;
                    calculateOverflow(prison, playersToAdd, pr, inhabitants);
                }
                
                for (UUID uuid : playersToAdd) {
                    Player inhabitant = changePunishmentInfo(prison, uuid);
                    if (inhabitant != null) {
                        inhabitant.teleport(prison.getLocation());
                        inhabitant.sendMessage(Utils.color("&dYou were an overflow inhabitant of your former prison, so you were moved to a different prison"));
                    }
                }
                prison.setMaxPlayers(amount);
            } else if (amount < prison.getMaxPlayers()) {
                Set<UUID> playersToRemove = new HashSet<>();
                List<UUID> inhabitants = new LinkedList<>(prison.getInhabitants());
                if (!inhabitants.isEmpty()) {
                    int removalAmount = prison.getMaxPlayers() - amount;
                    for (int i = 0; i < removalAmount; i++) {
                        int index = inhabitants.size() - 1 - i;
                        playersToRemove.add(inhabitants.get(index));
                    }
                }
                prison.setMaxPlayers(amount);
                for (UUID removed : playersToRemove) {
                    Prison newPrison = plugin.getDataManager().findPrison();
                    if (newPrison == null) continue;
                    prison.removeInhabitant(removed);
                    for (Punishment punishment : plugin.getDataManager().getActiveJails(removed)) {
                        ((JailPunishment) punishment).setJailId(newPrison.getId());
                    }
                    Player inhabitant = Bukkit.getPlayer(removed);
                    if (inhabitant != null) {
                        inhabitant.teleport(newPrison.getLocation());
                        inhabitant.sendMessage(Utils.color("&dThe prison you were in had its max players changed to a lower amount, so you were moved to a new prison."));
                    }
                }
            }
            String message = Messages.PRISON_SET_MAX_PLAYERS;
            message = message.replace(Variables.JAIL_ID, prison.getDisplayName());
            message = message.replace(Variables.MAX_PLAYERS, amount + "");
            sendOutputMessage(player, message);
        } else if (Utils.checkCmdAliases(args, 1, "remove", "r")) {
            if (!player.hasPermission(Perms.PRISON_REMOVE)) {
                player.sendMessage(Utils.color("&cYou do not have permission to use that command."));
                return true;
            }
            
            if (args.length != 2) {
                player.sendMessage(Utils.color("&cUsage: /prison <id|name> remove|r"));
                return true;
            }
            
            Set<UUID> inhabitants = new HashSet<>(prison.getInhabitants());
            plugin.getDataManager().removePrison(prison.getId());
            for (UUID inhabitant : inhabitants) {
                Prison newPrison = plugin.getDataManager().findPrison();
                Player jailedUser = changePunishmentInfo(newPrison, inhabitant);
                if (jailedUser != null) {
                    jailedUser.teleport(newPrison.getLocation());
                    jailedUser.sendMessage(Utils.color("&cThe prison you were a part of was removed, you have been moved to a new prison."));
                }
            }
            String message = Messages.PRISON_REMOVE;
            message = message.replace(Variables.JAIL_ID, prison.getId() + "");
            sendOutputMessage(player, message);
        } else if (Utils.checkCmdAliases(args, 1, "teleport", "tp")) {
            if (!player.hasPermission(Perms.PRISON_TELEPORT)) {
                player.sendMessage(Utils.color("&cYou do not have permission to use that command."));
                return true;
            }
            player.teleport(prison.getLocation());
            player.sendMessage(Utils.color("&aYou were teleported to the spawn location of the prison &b" + prison.getDisplayName()));
        } else if (Utils.checkCmdAliases(args, 1, "setname", "sn")) {
            if (!player.hasPermission(Perms.PRISON_SET_NAME)) {
                player.sendMessage(Utils.color("&cYou do not have permission to use that command."));
                return true;
            }
            
            if (args.length != 3) {
                player.sendMessage(Utils.color("&cUsage: /prison <id|name> setname|sn <newname>"));
                return true;
            }
            
            for (Prison pr : plugin.getDataManager().getPrisons()) {
                if (pr.getId() != prison.getId()) {
                    if (pr.getName() != null) {
                        if (pr.getName().equalsIgnoreCase(args[2])) {
                            player.sendMessage(Utils.color("&cA prison with that name already exists. Please choose another name."));
                            return true;
                        }
                    }
                }
            }
            
            prison.setName(args[2]);
            
            String message = Messages.PRISON_SET_NAME;
            message = message.replace(Variables.DISPLAY, prison.getName());
            message = message.replace(Variables.JAIL_ID, prison.getId() + "");
            sendOutputMessage(player, message);
        } else if (Utils.checkCmdAliases(args, 1, "redefine")) {
            if (!player.hasPermission(Perms.PRISON_REDEFINE)) {
                player.sendMessage(Utils.color("&cYou do not have permission to use that command."));
                return true;
            }
            if (!this.selectionManager.hasSelection(player)) {
                player.sendMessage(Utils.color("&cYou do not have a selection to redefine the region"));
                return true;
            }
            
            Selection selection = this.selectionManager.getSelection(player);
            
            prison.setBounds(selection.getPointA(), selection.getPointB());
            player.sendMessage(Utils.color("&aSet the bounds of the prison &b" + prison.getDisplayName() + " &ato the current selection."));
            if (!prison.contains(prison.getLocation())) {
                player.sendMessage(Utils.color("&cThe spawn location of the prison is not in the new prison area."));
            }
            
            String message = Messages.PRISON_REDEFINE;
            message = message.replace(Variables.DISPLAY, prison.getName());
            sendOutputMessage(player, message);
        }
        
        return true;
    }
    
    private Player changePunishmentInfo(Prison prison, UUID uuid) {
        prison.addInhabitant(uuid);
        for (Punishment punishment : plugin.getDataManager().getActiveJails(uuid)) {
            ((JailPunishment) punishment).setJailId(prison.getId());
        }
        return Bukkit.getPlayer(uuid);
    }
    
    private void calculateOverflow(Prison prison, Set<UUID> playersToAdd, Prison pr, List<UUID> inhabitants) {
        int amountOver = inhabitants.size() - pr.getMaxPlayers();
        for (int i = 0; i < amountOver; i++) {
            if (!(playersToAdd.size() >= prison.getMaxPlayers())) {
                int index = inhabitants.size() - 1 - i;
                playersToAdd.add(inhabitants.get(index));
                pr.removeInhabitant(inhabitants.get(index));
            }
        }
    }
    
    private void sendOutputMessage(Player player, String message) {
        Messages.sendOutputMessage(player, message, plugin);
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (plugin.getCustomItemsHook() == null) return;
        if (plugin.getCustomItemsHook().getCustomItems() == null) return;
        
        String name = plugin.getCustomItemsHook().getCustomItems().getItemManager().extractName(mainHand);
        if (name == null || name.equals("")) return;
        e.setCancelled(true);
        if (e.getClickedBlock() == null) {
            System.out.println(e.getClickedBlock());
            player.sendMessage(Utils.color("&cThe block you clicked on is non-existant"));
            return;
        }
        if (name.equalsIgnoreCase("inspecttool")) {
            if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getHand().equals(EquipmentSlot.HAND)) {
                Location location = e.getClickedBlock().getLocation();
                for (Prison prison : plugin.getDataManager().getPrisons()) {
                    if (prison.contains(location)) {
                        System.out.println("Prison contains the block clicked.");
                        player.sendMessage(Utils.color("&aThe block you clicked on is in the prison &b" + prison.getDisplayName()));
                    }
                }
            }
        }
    }
}