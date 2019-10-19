package com.stardevmc.enforcer.modules.punishments.cmds;

import com.firestar311.lib.player.PlayerInfo;
import com.firestar311.lib.util.Utils;
import com.stardevmc.enforcer.Enforcer;
import com.stardevmc.enforcer.modules.prison.Prison;
import com.stardevmc.enforcer.modules.punishments.*;
import com.stardevmc.enforcer.modules.punishments.actor.*;
import com.stardevmc.enforcer.modules.punishments.target.*;
import com.stardevmc.enforcer.modules.punishments.type.PunishmentType;
import com.stardevmc.enforcer.modules.punishments.type.abstraction.Punishment;
import com.stardevmc.enforcer.modules.punishments.type.impl.BlacklistPunishment;
import com.stardevmc.enforcer.modules.punishments.type.impl.JailPunishment;
import com.stardevmc.enforcer.modules.rules.RuleManager;
import com.stardevmc.enforcer.modules.rules.rule.*;
import com.stardevmc.enforcer.util.*;
import com.stardevmc.enforcer.util.evidence.Evidence;
import com.stardevmc.enforcer.util.evidence.EvidenceType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.Map.Entry;

public class PunishmentCommands implements CommandExecutor {
    
    private Enforcer plugin;
    
    private Map<String, List<PunishmentBuilder>> punishmentBuilders = new HashMap<>();
    
    public PunishmentCommands(Enforcer plugin) {
        this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Actor actor;
        if (sender instanceof Player) {
            actor = new PlayerActor(((Player) sender).getUniqueId());
        } else if (sender instanceof ConsoleCommandSender) {
            actor = new ConsoleActor();
        } else {
            sender.sendMessage(Utils.color("&cOnly Players or the Console can use punishment commands."));
            return true;
        }
        
        if (cmd.getName().equals("punishment")) {
            if (!(args.length > 0)) {
                return true;
            }
            
            if (!sender.hasPermission("enforcer.command.punishment")) {
                return true;
            }
            
            PunishmentManager punishmentManager = plugin.getPunishmentModule().getManager();
            
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
                        punishmentManager.addJailPunishment((JailPunishment) punishment);
                    } else {
                        punishmentManager.addPunishment(punishment);
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
            } else if (Utils.isInt(args[0])) {
                Player player = ((Player) sender);
                int id;
                try {
                    id = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    player.sendMessage(Utils.color("&cThe value for the id is not a valid number."));
                    return true;
                }
                
                Punishment punishment = punishmentManager.getPunishment(id);
                
                if (punishment == null) {
                    player.sendMessage(Utils.color("&cCould not find a punishment with that id."));
                    return true;
                }
                
                if (Utils.checkCmdAliases(args, 1, "setevidence", "se")) {
                    if (!player.hasPermission(Perms.PUNISHMENTS_SET_EVIDENCE)) {
                        player.sendMessage(Messages.noPermissionCommand(Perms.PUNISHMENTS_SET_EVIDENCE));
                        return true;
                    }
                    
                    if (args.length != 2) {
                        player.sendMessage(Utils.color("&cUsage: /punishmentinfo <punishment id> setevidence|se <link>"));
                        return true;
                    }
                    
                    Evidence evidence = new Evidence(0, player.getUniqueId(), EvidenceType.STAFF, args[2]);
                    punishment.setEvidence(evidence);
                    player.sendMessage(Utils.color("&aYou set the evidence of the punishment &b" + punishment.getId() + " &ato &b" + evidence.getLink()));
                }
            }
            
            return true;
        }
        
        String prefix = plugin.getSettingsManager().getPrefix();
        
        if (!(args.length > 0)) {
            sender.sendMessage(Utils.color("&cYou must provide a player to punish."));
            return true;
        }
        
        String targetArg = args[0];
        Target target = EnforcerUtils.getTarget(targetArg);
    
        if (target == null) {
            sender.sendMessage(Utils.color("&cInvalid target: " + targetArg));
            return true;
        }
        
        if (target instanceof IPTarget || target instanceof IPListTarget) {
            if (cmd.getName().equalsIgnoreCase("blacklist")) {
                BlacklistPunishment blacklistPunishment = new BlacklistPunishment(plugin.getSettingsManager().getPrefix(),
                        actor, target, getReason(1, args), System.currentTimeMillis());
                plugin.getPunishmentModule().getManager().addPunishment(blacklistPunishment);
                blacklistPunishment.executePunishment();
            }
            return true;
        }
        
        if (!(target instanceof PlayerTarget)) {
            return true;
        }
        
        PlayerInfo info = ((PlayerTarget) target).getPlayerInfo();
        
        if (Bukkit.getPlayer(info.getUuid()) == null) {
            if (!sender.hasPermission(Perms.OFFLINE_PUNISH)) {
                sender.sendMessage(Messages.noPermissionCommand(Perms.OFFLINE_PUNISH));
                return true;
            }
        }
        
        try {
            if (sender instanceof Player) {
                Player player = ((Player) sender);
                net.milkbowl.vault.permission.Permission perms = Enforcer.getInstance().getPermission();
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(info.getUuid());
                String groupName = perms.getPrimaryGroup(player.getWorld().getName(), offlinePlayer).toLowerCase();
                if (groupName != null && !groupName.equals("")) {
                    if (!player.hasPermission("enforcer.immunity." + groupName)) {
                        player.sendMessage(Utils.color("&cYou cannot punish that player because they are immune."));
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {}
        
        Visibility visibility = Visibility.NORMAL;
        boolean ignoreTraining = false, ignoreConfirm = false;
        EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);
        
        for (String arg : args) {
            Flag flag = Flag.matchFlag(arg);
            if (flag != null) {
                flags.add(flag);
            }
        }
        
        for (Flag flag : flags) {
            if (flag == Flag.PUBLIC) {
                visibility = Visibility.PUBLIC;
            }
            if (flag == Flag.SILENT) {
                visibility = Visibility.SILENT;
            }
            if (flag == Flag.IGNORE_TRAINING) {
                if (!sender.hasPermission(Perms.FLAG_IGNORE_TRAINING)) {
                    sender.sendMessage(Utils.color("&cYou do not have permission to ignore training mode."));
                    return true;
                }
                ignoreTraining = true;
            }
            
            if (flag == Flag.IGNORE_CONFIRM) {
                if (!sender.hasPermission(Perms.FLAG_IGNORE_CONFIRM)) {
                    sender.sendMessage(Utils.color("&cYou do not have permission to ignore confirmation."));
                    return true;
                }
                
                ignoreConfirm = true;
            }
        }
        
        if (cmd.getName().equalsIgnoreCase("punish")) {
            if (sender instanceof ConsoleCommandSender) {
                sender.sendMessage(Utils.color("&cOnly players may use the punish command."));
                return true;
            }
            
            Player player = ((Player) sender);
            if (!player.hasPermission(Perms.PUNISH_COMMAND)) {
                player.sendMessage(Messages.noPermissionCommand(Perms.PUNISH_COMMAND));
                return true;
            }
            
            if (args.length == 1) {
                PunishGUI punishGUI = new PunishGUI(plugin, player, target);
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
            
            RuleManager ruleManager = plugin.getRuleModule().getManager();
            
            Rule rule = ruleManager.getRule(sb.toString());
            if (rule == null) {
                player.sendMessage(Utils.color("&cThe value you provided does not match to a valid rule."));
                return true;
            }
            
            if (!rule.hasPermission(player)) {
                player.sendMessage(Utils.color("&cYou do not have permission to punish with this rule."));
                return true;
            }
            
            Entry<Integer, Integer> offenseNumbers = ruleManager.getNextOffense(player.getUniqueId(), info.getUuid(), rule);
            
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
            UUID punisher = player.getUniqueId();
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
                plugin.getPunishmentModule().getManager().addPunishment(punishment);
                punishment.executePunishment();
            }
        } else {
            long currentTime = System.currentTimeMillis();
            
            PunishmentBuilder puBuilder = new PunishmentBuilder(target);
            puBuilder.setDate(currentTime).setPunisher(actor).setVisibility(visibility).setServer(prefix);
            if (!ignoreTraining) {
                puBuilder.setTrainingMode(plugin.getTrainingModule().getManager().isTrainingMode(actor));
            }
            
            String reason = "";
            
            if (cmd.getName().equalsIgnoreCase("ban")) {
                if (!sender.hasPermission(Perms.BAN)) {
                    sender.sendMessage(Messages.noPermissionCommand(Perms.BAN));
                    return true;
                }
                reason = getReason(1, args);
                
                puBuilder.setType(PunishmentType.PERMANENT_BAN);
            } else if (cmd.getName().equalsIgnoreCase("tempban")) {
                if (!sender.hasPermission(Perms.TEMP_BAN)) {
                    sender.sendMessage(Messages.noPermissionCommand(Perms.TEMP_BAN));
                    return true;
                }
                
                long expire = Utils.parseTime(args[1]);
                
                if (!(args.length > 2)) {
                    sender.sendMessage(Utils.color("&cYou must supply a reason for the punishment."));
                    return true;
                }
                reason = getReason(2, args);
                puBuilder.setType(PunishmentType.TEMPORARY_BAN).setLength(expire);
            } else if (cmd.getName().equalsIgnoreCase("mute")) {
                if (!sender.hasPermission(Perms.MUTE)) {
                    sender.sendMessage(Messages.noPermissionCommand(Perms.MUTE));
                    return true;
                }
                
                reason = getReason(1, args);
                puBuilder.setType(PunishmentType.PERMANENT_MUTE);
            } else if (cmd.getName().equalsIgnoreCase("tempmute")) {
                if (!sender.hasPermission(Perms.TEMP_MUTE)) {
                    sender.sendMessage(Messages.noPermissionCommand(Perms.TEMP_MUTE));
                    return true;
                }
                
                long expire = Utils.parseTime(args[1]);
                
                if (!(args.length > 2)) {
                    sender.sendMessage(Utils.color("&cYou must supply a reason for the punishment."));
                    return true;
                }
                
                reason = getReason(2, args);
                puBuilder.setType(PunishmentType.TEMPORARY_MUTE).setLength(expire);
            } else if (cmd.getName().equalsIgnoreCase("kick")) {
                if (!sender.hasPermission(Perms.KICK)) {
                    sender.sendMessage(Messages.noPermissionCommand(Perms.KICK));
                    return true;
                }
                
                reason = getReason(1, args);
                puBuilder.setType(PunishmentType.KICK);
            } else if (cmd.getName().equalsIgnoreCase("warn")) {
                if (!sender.hasPermission(Perms.WARN)) {
                    sender.sendMessage(Messages.noPermissionCommand(Perms.WARN));
                    return true;
                }
                
                reason = getReason(1, args);
                puBuilder.setType(PunishmentType.WARN);
            } else if (cmd.getName().equalsIgnoreCase("jail")) {
                if (!sender.hasPermission(Perms.JAIL)) {
                    sender.sendMessage(Messages.noPermissionCommand(Perms.JAIL));
                    return true;
                }
                
                if (plugin.getPrisonModule().getManager().getPrisons().isEmpty()) {
                    sender.sendMessage(Utils.color("&cThere are no prisons created. Jail punishments are disabled until at least 1 is created."));
                    return true;
                }
                
                reason = getReason(1, args);
                puBuilder.setType(PunishmentType.JAIL);
                Prison prison = plugin.getPrisonModule().getManager().findPrison();
                puBuilder.setPrisonId(prison.getId());
            }
            
            puBuilder.setReason(reason);
            
            if (plugin.getSettingsManager().mustConfirmPunishments()) {
                if (!ignoreConfirm) {
                    String code = Code.generateNewCode(6);
                    addPunishmentBuilder(code, puBuilder);
                    if (sender instanceof Player) {
                        sendConfirmMessage(((Player) sender), puBuilder, code);
                    }
                    return true;
                }
                sender.sendMessage(Utils.color("&2You are ignoring the punishment confirmation for this punishment."));
            }
            
            Punishment punishment = puBuilder.build();
            plugin.getPunishmentModule().getManager().addPunishment(punishment);
            punishment.executePunishment();
        }
        return true;
    }
    
    private void addPunishmentBuilder(String code, PunishmentBuilder builder) {
        if (this.punishmentBuilders.containsKey(code)) {
            this.punishmentBuilders.get(code).add(builder);
        } else {
            this.punishmentBuilders.put(code, new ArrayList<>(Collections.singletonList(builder)));
        }
    }
    
    private void sendConfirmMessage(Player player, PunishmentBuilder puBuilder, String code) {
        Target targetInfo = puBuilder.getTarget();
        player.sendMessage("");
        player.sendMessage(Utils.color("&4╔═════════════════════════════"));
        player.sendMessage(Utils.color("&4║ &fYou are about to punish &b" + targetInfo.getName() + " "));
        player.sendMessage(Utils.color("&4║ &fReason: &e" + puBuilder.getReason()));
        String punishmentString = EnforcerUtils.getPunishString(puBuilder);
        player.sendMessage(Utils.color("&4║ &fThis action will result in the punishment " + punishmentString));
        BaseComponent[] baseComponents = new ComponentBuilder("║ ").color(ChatColor.DARK_RED).append("Click to ").color(ChatColor.WHITE).append("[Confirm]").color(ChatColor.GREEN).bold(true).event(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder("Click to confirm punishment").create())).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/punishment confirm " + code)).append(" [Cancel]").color(ChatColor.RED).bold(true).event(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder("Click to cancel punishment").create())).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/punishment cancel " + code)).create();
        player.spigot().sendMessage(baseComponents);
        player.sendMessage(Utils.color("&4╚═════════════════════════════"));
        player.sendMessage("");
    }
    
    private void sendConfirmMessage(Player player, String code, Target target, String reason, List<PunishmentBuilder> builders) {
        player.sendMessage("");
        player.sendMessage(Utils.color("&4╔═════════════════════════════"));
        player.sendMessage(Utils.color("&4║ &fYou are about to punish &b" + target.getName() + " "));
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
    
    private void addPunishmentBuilders(String code, PunishmentBuilder... builders) {
        if (this.punishmentBuilders.containsKey(code)) {
            this.punishmentBuilders.get(code).addAll(Arrays.asList(builders));
        } else {
            this.punishmentBuilders.put(code, new ArrayList<>(Arrays.asList(builders)));
        }
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
        if (plugin.getTrainingModule().getManager().isTrainingMode(punishment.getPunisher())) {
            if (ignoreTraining) {
                punishment.setTrainingMode(false);
            }
        }
    }
}
