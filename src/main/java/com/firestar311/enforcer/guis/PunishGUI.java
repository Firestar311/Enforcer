package com.firestar311.enforcer.guis;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.model.Prison;
import com.firestar311.enforcer.model.punishment.abstraction.Punishment;
import com.firestar311.enforcer.model.punishment.type.*;
import com.firestar311.enforcer.model.rule.*;
import com.firestar311.lib.gui.GUIButton;
import com.firestar311.lib.gui.PaginatedGUI;
import com.firestar311.lib.player.PlayerInfo;
import com.firestar311.lib.util.Utils;
import org.bukkit.entity.Player;

import java.util.Map.Entry;
import java.util.UUID;

public class PunishGUI extends PaginatedGUI {
    static {
        PaginatedGUI.prepare(Enforcer.getInstance());
    }
    
    public PunishGUI(Enforcer plugin, PlayerInfo t) {
        super(plugin, "Punish " + t.getLastName(), false, 54);
        
        for (Rule r : plugin.getDataManager().getRules()) {
            if (r.getMaterial() != null) {
                GUIButton button = new GUIButton(r.getItemStack());
                
                button.setListener(e -> {
                    final UUID target = t.getUuid();
                    
                    Player player = ((Player) e.getWhoClicked());
    
                    Entry<Integer, Integer> offenseNumbers = plugin.getDataManager().getNextOffense(target, r);
    
                    RuleOffense offense = r.getOffense(offenseNumbers.getKey());
                    if (offense == null) {
                        player.sendMessage(Utils.color("&cThere was a severe problem getting the next offense, use a manual punishment if an emergency, otherwise, contact the plugin developer"));
                        return;
                    }
    
                    String server = plugin.getDataManager().getPrefix();
                    long currentTime = System.currentTimeMillis();
                    UUID punisher = player.getUniqueId();
                    String reason = r.getName() + " Offense #" + offenseNumbers.getValue();
                    for (RulePunishment rulePunishment : offense.getPunishments().values()) {
                        Punishment punishment = null;
                        long expire = currentTime + rulePunishment.getLength();
                        switch(rulePunishment.getType()) {
                            case PERMANENT_BAN: punishment = new PermanentBan(server, punisher, target, reason, currentTime);
                                break;
                            case TEMPORARY_BAN: punishment = new TemporaryBan(server, punisher, target, reason, currentTime, expire);
                                break;
                            case PERMANENT_MUTE: punishment = new PermanentMute(server, punisher, target, reason, currentTime);
                                break;
                            case TEMPORARY_MUTE: punishment = new TemporaryMute(server, punisher, target, reason, currentTime, expire);
                                break;
                            case WARN: punishment = new WarnPunishment(server, punisher, target, reason, currentTime);
                                break;
                            case KICK: punishment = new KickPunishment(server, punisher, target, reason, currentTime);
                                break;
                            case JAIL:
                                Prison prison = plugin.getDataManager().findPrison();
                                punishment = new JailPunishment(server, punisher, target, reason, currentTime, prison.getId());
                                break;
                        }
                        punishment.setRuleId(r.getId());
                        punishment.setOffenseNumber(offenseNumbers.getValue());
                        plugin.getDataManager().addPunishment(punishment);
                        punishment.executePunishment();
                    }
                    player.closeInventory();
                });
                addButton(button);
            }
        }
    }
}