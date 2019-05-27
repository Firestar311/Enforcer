package com.firestar311.enforcer.modules.punishments.cmds;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.modules.punishments.PunishGUI;
import com.firestar311.enforcer.modules.prison.Prison;
import com.firestar311.enforcer.modules.punishments.*;
import com.firestar311.enforcer.modules.punishments.type.PunishmentType;
import com.firestar311.enforcer.modules.punishments.type.abstraction.Punishment;
import com.firestar311.enforcer.modules.punishments.type.impl.JailPunishment;
import com.firestar311.enforcer.modules.rules.rule.*;
import com.firestar311.enforcer.util.*;
import com.firestar311.lib.player.PlayerInfo;
import com.firestar311.lib.util.Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.Map.Entry;

@SuppressWarnings("Duplicates")
public class PunishmentCommands implements CommandExecutor {
    
    private Enforcer plugin;
    
    private Map<String, List<PunishmentBuilder>> punishmentBuilders = new HashMap<>();
    
    public PunishmentCommands(Enforcer plugin) {
        this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.color("&cOnly players may use punishment commands."));
            return true;
        }
        
        if (cmd.getName().equals("punishment")) {
            if (!(args.length > 0)) {
                return true;
            }
            
            if (args[0].equalsIgnoreCase("confirm")) {
                if (!(args.length > 1)) {
                    return true;
                }
                
                String code = args[1];
                if (!this.punishmentBuilders.containsKey(code)) {
                    return true;
                }
                
                List<PunishmentBuilder> builders = this.punishmentBuilders.get(code);
                for (PunishmentBuilder punishmentBuilder : builders) {
                    Punishment punishment = punishmentBuilder.build();
                    if (punishment instanceof JailPunishment) {
                        plugin.getPunishmentManager().addJailPunishment((JailPunishment) punishment);
                    } else {
                        plugin.getPunishmentManager().addPunishment(punishment);
                    }
                    
                    punishment.executePunishment();
                }
            } else if (args[0].equalsIgnoreCase("cancel")) {
                if (!(args.length > 1)) {
                    return true;
                }
    
                String code = args[1];
                this.punishmentBuilders.remove(code);
                sender.sendMessage(Utils.color("&aCancelled that/those punishment(s)"));
            }
            
            return true;
        }
        
        Player player = ((Player) sender);
        String prefix = plugin.getSettingsManager().getPrefix();
        
        if (!(args.length > 0)) {
            player.sendMessage(Utils.color("&cYou must provide a player to punish."));
            return true;
        }
        
        PlayerInfo info = plugin.getPlayerManager().getPlayerInfo(args[0]);
        if (info == null) {
            player.sendMessage(Utils.color("&cCould not find a player by that name."));
            return true;
        }
        
        //This code is used for my own server
//        UUID firestar311 = UUID.fromString("3f7891ce-5a73-4d52-a2ba-299839053fdc");
//        if (info.getUuid().equals(firestar311) && !player.getUniqueId().equals(firestar311)) {
//            player.sendMessage(Utils.color("&cYou cannot punish that player."));
//            return true;
//        }
        
        if (Bukkit.getPlayer(info.getUuid()) == null) {
            if (!player.hasPermission(Perms.OFFLINE_PUNISH)) {
                player.sendMessage(Utils.color("&cYou cannot punish offline players."));
                return true;
            }
        }
        
        try {
            net.milkbowl.vault.permission.Permission perms = Enforcer.getInstance().getPermission();
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(info.getUuid());
            String groupName = perms.getPrimaryGroup(player.getWorld().getName(), offlinePlayer).toLowerCase();
            if (groupName != null && !groupName.equals("")) {
                if (!player.hasPermission("enforcer.immunity." + groupName)) {
                    player.sendMessage(Utils.color("&cYou cannot punish that player because they are immune."));
                    return true;
                }
            }
        } catch (Exception ignored) {}
        
        Visibility visibility = Visibility.NORMAL;
        boolean ignoreTraining = false, ignoreConfirm = false;
        for (String arg : args) {
            if (arg.equalsIgnoreCase("-p")) {
                visibility = Visibility.PUBLIC;
                break;
            }
            if (arg.equalsIgnoreCase("-s")) {
                visibility = Visibility.SILENT;
                break;
            }
            if (arg.equalsIgnoreCase("-t")) {
                if (!player.hasPermission(Perms.FLAG_IGNORE_TRAINING)) {
                    player.sendMessage(Utils.color("&cYou do not have permission to ignore training mode."));
                    return true;
                }
                ignoreTraining = true;
            }
            
            if (arg.equalsIgnoreCase("-c")) {
                if (!player.hasPermission(Perms.FLAG_IGNORE_CONFIRM)) {
                    player.sendMessage(Utils.color("&cYou do not have permission to ignore confirmation."));
                    return true;
                }
                
                ignoreConfirm = true;
            }
        }
        
        if (cmd.getName().equalsIgnoreCase("punish")) {
            if (!player.hasPermission(Perms.PUNISH_COMMAND)) {
                player.sendMessage(Utils.color("&cYou do not have permission to use that command."));
                return true;
            }
            
            if (args.length == 1) {
                PunishGUI punishGUI = new PunishGUI(plugin, player, info);
                player.openInventory(punishGUI.getInventory());
                return true;
            }
            
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                if (!args[i].startsWith("-")) {
                    sb.append(args[i]);
                    if (i != args.length - 1) {
                        sb.append(" ");
                    }
                }
            }
            
            Rule rule = plugin.getRuleManager().getRule(sb.toString());
            if (rule == null) {
                player.sendMessage(Utils.color("&cThe value you provided does not match to a valid rule."));
                return true;
            }
            
            if (!rule.hasPermission(player)) {
                player.sendMessage(Utils.color("&cYou do not have permission to punish with this rule."));
                return true;
            }
            
            Entry<Integer, Integer> offenseNumbers = plugin.getRuleManager().getNextOffense(player.getUniqueId(), info.getUuid(), rule);
            
            RuleOffense offense = rule.getOffense(offenseNumbers.getKey());
            if (offense == null) {
                player.sendMessage(Utils.color("&cThere was a severe problem getting the next offense, use a manual punishment if an emergency, otherwise, contact the plugin developer"));
                return true;
            }
            
            if (!offense.hasPermission(player)) {
                player.sendMessage(Utils.color("&cYou do not have permission to punish with this offense."));
                return true;
            }
            
            String server = plugin.getSettingsManager().getPrefix();
            long currentTime = System.currentTimeMillis();
            UUID punisher = player.getUniqueId(), target = info.getUuid();
            String reason = rule.getName() + " Offense #" + offenseNumbers.getValue();
            List<PunishmentBuilder> puBuilders = new ArrayList<>();
            for (RulePunishment rulePunishment : offense.getPunishments().values()) {
                PunishmentBuilder puBuilder = new PunishmentBuilder(target);
                puBuilder.setType(rulePunishment.getType());
                puBuilder.setReason(reason).setPunisher(punisher).setServer(server).setDate(currentTime).setLength(rulePunishment.getLength());
                puBuilder.setRuleId(rule.getId());
                puBuilder.setOffenseNumber(offenseNumbers.getValue());
                puBuilders.add(puBuilder);
            }
            
            if (puBuilders.size() == 1) {
                PunishmentBuilder puBuilder = puBuilders.get(0);
                if (plugin.getSettingsManager().mustConfirmPunishments()) {
                    if (!ignoreConfirm) {
                        String code = Code.generateNewCode(6);
                        addPunishmentBuilder(code, puBuilder);
                        sendConfirmMessage(player, puBuilder, code);
                        return true;
                    }
                }
            } else {
                String code = Code.generateNewCode(6);
                if (!ignoreConfirm) {
                    sendConfirmMessage(player, code, target, reason, puBuilders);
                    addPunishmentBuilders(code, puBuilders.toArray(new PunishmentBuilder[0]));
                    return true;
                }
            }
            if (ignoreConfirm) {
                player.sendMessage(Utils.color("&2You are ignoring the punishment confirmation for this punishment."));
            }
            
            for (PunishmentBuilder puBuilder : puBuilders) {
                Punishment punishment = puBuilder.build();
                plugin.getPunishmentManager().addPunishment(punishment);
                punishment.executePunishment();
            }
        } else {
            long currentTime = System.currentTimeMillis();
            
            PunishmentBuilder puBuilder = new PunishmentBuilder(info.getUuid());
            puBuilder.setDate(currentTime).setPunisher(player.getUniqueId()).setVisibility(visibility).setServer(prefix);
            if (!ignoreTraining) {
                puBuilder.setTrainingMode(plugin.getTrainingModeManager().isTrainingMode(player.getUniqueId()));
            }
            
            String reason = "";
            
            if (cmd.getName().equalsIgnoreCase("ban")) {
                if (!player.hasPermission(Perms.BAN)) {
                    player.sendMessage(Utils.color("&cYou lack the permission &7(" + Perms.BAN + ")"));
                    return true;
                }
                reason = getReason(1, args);
                
                puBuilder.setType(PunishmentType.PERMANENT_BAN);
            } else if (cmd.getName().equalsIgnoreCase("tempban")) {
                if (!player.hasPermission(Perms.TEMP_BAN)) {
                    player.sendMessage(Utils.color("&cYou lack the permission &7(" + Perms.TEMP_BAN + ")"));
                    return true;
                }
                
                long expire = Punishment.calculateLength(args[1]);
                
                if (!(args.length > 2)) {
                    player.sendMessage(Utils.color("&cYou must supply a reason for the punishment."));
                    return true;
                }
                reason = getReason(2, args);
                puBuilder.setType(PunishmentType.TEMPORARY_BAN).setLength(expire);
            } else if (cmd.getName().equalsIgnoreCase("mute")) {
                if (!player.hasPermission(Perms.MUTE)) {
                    player.sendMessage(Utils.color("&cYou lack the permission &7(" + Perms.MUTE + ")"));
                    return true;
                }
                
                reason = getReason(1, args);
                puBuilder.setType(PunishmentType.PERMANENT_MUTE);
            } else if (cmd.getName().equalsIgnoreCase("tempmute")) {
                if (!player.hasPermission(Perms.TEMP_MUTE)) {
                    player.sendMessage(Utils.color("&cYou lack the permission &7(" + Perms.TEMP_MUTE + ")"));
                    return true;
                }
                
                long expire = Punishment.calculateLength(args[1]);
                
                if (!(args.length > 2)) {
                    player.sendMessage(Utils.color("&cYou must supply a reason for the punishment."));
                    return true;
                }
                
                reason = getReason(2, args);
                puBuilder.setType(PunishmentType.TEMPORARY_MUTE).setLength(expire);
            } else if (cmd.getName().equalsIgnoreCase("kick")) {
                if (!player.hasPermission(Perms.KICK)) {
                    player.sendMessage(Utils.color("&cYou lack the permission &7(" + Perms.KICK + ")"));
                    return true;
                }
                
                reason = getReason(1, args);
                puBuilder.setType(PunishmentType.KICK);
            } else if (cmd.getName().equalsIgnoreCase("warn")) {
                if (!player.hasPermission(Perms.WARN)) {
                    player.sendMessage(Utils.color("&cYou lack the permission &7(" + Perms.WARN + ")"));
                    return true;
                }
                
                reason = getReason(1, args);
                puBuilder.setType(PunishmentType.WARN);
            } else if (cmd.getName().equalsIgnoreCase("jail")) {
                if (!player.hasPermission(Perms.JAIL)) {
                    player.sendMessage(Utils.color("&cYou lack the permission &7(" + Perms.JAIL + ")"));
                    return true;
                }
                
                if (plugin.getPrisonManager().getPrisons().isEmpty()) {
                    player.sendMessage(Utils.color("&cThere are no prisons created. Jail punishments are disabled until at least 1 is created."));
                    return true;
                }
                
                reason = getReason(1, args);
                puBuilder.setType(PunishmentType.JAIL);
                Prison prison = plugin.getPrisonManager().findPrison();
                puBuilder.setPrisonId(prison.getId());
            }
            
            puBuilder.setReason(reason);
            
            if (plugin.getSettingsManager().mustConfirmPunishments()) {
                if (!ignoreConfirm) {
                    String code = Code.generateNewCode(6);
                    addPunishmentBuilder(code, puBuilder);
                    sendConfirmMessage(player, puBuilder, code);
                    return true;
                }
                player.sendMessage(Utils.color("&2You are ignoring the punishment confirmation for this punishment."));
            }
            
            Punishment punishment = puBuilder.build();
            plugin.getPunishmentManager().addPunishment(punishment);
            punishment.executePunishment();
        }
        return true;
    }
    
    private void sendConfirmMessage(Player player, PunishmentBuilder puBuilder, String code) {
        PlayerInfo targetInfo = plugin.getPlayerManager().getPlayerInfo(puBuilder.getTarget());
        player.sendMessage("");
        player.sendMessage(Utils.color("&4╔═════════════════════════════"));
        player.sendMessage(Utils.color("&4║ &fYou are about to punish &b" + targetInfo.getLastName() + " "));
        player.sendMessage(Utils.color("&4║ &fReason: &e" + puBuilder.getReason()));
        String punishmentString = EnforcerUtils.getPunishString(puBuilder);
        player.sendMessage(Utils.color("&4║ &fThis action will result in the punishment " + punishmentString));
    
        BaseComponent[] baseComponents = new ComponentBuilder("║ ").color(ChatColor.DARK_RED).append("Click to ").color(ChatColor.WHITE).append("[Confirm]").color(ChatColor.GREEN).bold(true).event(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder("Click to confirm punishment").create())).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/punishment confirm " + code)).append(" [Cancel]").color(ChatColor.RED).bold(true).event(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder("Click to cancel punishment").create())).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/punishment cancel " + code)).create();
        player.spigot().sendMessage(baseComponents);
        player.sendMessage(Utils.color("&4╚═════════════════════════════"));
        player.sendMessage("");
    }
    
    private void sendConfirmMessage(Player player, String code, UUID target, String reason, List<PunishmentBuilder> builders) {
        PlayerInfo targetInfo = plugin.getPlayerManager().getPlayerInfo(target);
        player.sendMessage("");
        player.sendMessage(Utils.color("&4╔═════════════════════════════"));
        player.sendMessage(Utils.color("&4║ &fYou are about to punish &b" + targetInfo.getLastName() + " "));
        player.sendMessage(Utils.color("&4║ &fReason: &e" + reason));
        List<String> punishmentStrings = new ArrayList<>();
        for (PunishmentBuilder puBuilder : builders) {
            punishmentStrings.add(EnforcerUtils.getPunishString(puBuilder));
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < punishmentStrings.size(); i++) {
            String puString = punishmentStrings.get(i);
            sb.append(Utils.color(" &8- ")).append(Utils.color(puString));
            if (i < punishmentStrings.size() - 1) {
                sb.append("\n");
            }
        }
        
        BaseComponent[] punishmentComponents = new ComponentBuilder("║ ").color(ChatColor.DARK_RED).append("This action will result in ").color(ChatColor.WHITE).append(builders.size() + " punishments").color(ChatColor.DARK_AQUA).event(new HoverEvent(Action.SHOW_TEXT, TextComponent.fromLegacyText(sb.toString()))).create();
        player.spigot().sendMessage(punishmentComponents);
        //player.sendMessage(Utils.color("&4║ &fThis action will result in the punishment " + punishmentString));
        
        BaseComponent[] baseComponents = new ComponentBuilder("║ ").color(ChatColor.DARK_RED).append("Click to ").color(ChatColor.WHITE).append("[Confirm]").color(ChatColor.GREEN).bold(true).event(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder("Click to confirm punishment").create())).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/punishment confirm " + code)).append(" [Cancel]").color(ChatColor.RED).bold(true).event(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder("Click to cancel punishment").create())).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/punishment cancel " + code)).create();
        player.spigot().sendMessage(baseComponents);
        player.sendMessage(Utils.color("&4╚═════════════════════════════"));
        player.sendMessage("");
    }
    
    private String getReason(int index, String[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = index; i < args.length; i++) {
            if (!args[i].startsWith("-")) {
                sb.append(args[i]);
                if (i != args.length - 1) {
                    sb.append(" ");
                }
            }
        }
        return sb.toString();
    }
    
    private void checkTraining(Punishment punishment, boolean ignoreTraining) {
        if (plugin.getTrainingModeManager().isTrainingMode(punishment.getPunisher())) {
            if (ignoreTraining) {
                punishment.setTrainingMode(false);
            }
        }
    }
    
    private long extractExpire(Player player, long currentTime, String s) {
        try {
            return Punishment.calculateExpireDate(currentTime, s);
        } catch (Exception e) {
            player.sendMessage(Utils.color("&cInvalid time format."));
            return -1;
        }
    }
    
    private void addPunishmentBuilder(String code, PunishmentBuilder builder) {
        if (this.punishmentBuilders.containsKey(code)) {
            this.punishmentBuilders.get(code).add(builder);
        } else {
            this.punishmentBuilders.put(code, new ArrayList<>(Collections.singletonList(builder)));
        }
    }
    
    private void addPunishmentBuilders(String code, PunishmentBuilder... builders) {
        if (this.punishmentBuilders.containsKey(code)) {
            this.punishmentBuilders.get(code).addAll(Arrays.asList(builders));
        } else {
            this.punishmentBuilders.put(code, new ArrayList<>(Arrays.asList(builders)));
        }
    }
}
