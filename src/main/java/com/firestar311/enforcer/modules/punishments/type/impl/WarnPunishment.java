package com.firestar311.enforcer.modules.punishments.type.impl;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.enforcer.modules.punishments.type.PunishmentType;
import com.firestar311.enforcer.modules.punishments.Visibility;
import com.firestar311.enforcer.modules.punishments.type.abstraction.Punishment;
import com.firestar311.enforcer.modules.punishments.type.interfaces.Acknowledgeable;
import com.firestar311.lib.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class WarnPunishment extends Punishment implements Acknowledgeable {
    
    private boolean acknowledged;
    
    private Prompt prompt;
    
    public WarnPunishment(Map<String, Object> serialized) {
        super(serialized);
    }
    
    public WarnPunishment(String server, UUID punisher, UUID target, String reason, long date) {
        super(PunishmentType.WARN, server, punisher, target, reason, date);
    }
    
    public WarnPunishment(String server, UUID punisher, UUID target, String reason, long date, Visibility visibility) {
        super(PunishmentType.WARN, server, punisher, target, reason, date, visibility);
    }
    
    public WarnPunishment(int id, String server, UUID punisher, UUID target, String reason, long date, boolean active, boolean purgatory, Visibility visibility) {
        super(id, PunishmentType.WARN, server, punisher, target, reason, date, active, purgatory, visibility);
    }
    
    public Prompt createPrompt() {
        Player player = Bukkit.getPlayer(target);
        String code = Enforcer.getInstance().getPunishmentModule().getManager().generateAckCode(this.id);
        if (player != null) {
            prompt = new ValidatingPrompt() {
                public String getPromptText(ConversationContext context) {
                    return Utils.color("&cYou have been warned by &7" + getPunisherName() + " &cfor &7" + reason
                            + "\n&cYou must acknowledge this warning before you may speak again."
                            + "\n&cPlease type the code &7" + code + " &cto acknowledge.");
                }
            
                protected boolean isInputValid(ConversationContext context, String input) {
                    return input.equals(code);
                }
            
                protected Prompt acceptValidatedInput(ConversationContext context, String input) {
                    setAcknowledged(true);
                    setActive(false);
                    context.getForWhom().sendRawMessage(Utils.color("&aYou have acknowledged your warning."));
                
                    String format = visibility.getPrefix() + "&6(" + server.toUpperCase() + ") &2" + getTargetName() + " &fhas acknowledged their warning.";
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (canSeeMessages(p, visibility)) {
                            p.sendMessage(Utils.color(format));
                        }
                    }
                    return END_OF_CONVERSATION;
                }
            };
        
            Conversation conv = new ConversationFactory(Enforcer.getInstance()).withFirstPrompt(prompt).withLocalEcho(false).buildConversation(player);
            conv.begin();
        } else {
            setOffline(true);
        }
        return prompt;
    }
    
    public void executePunishment() {
        Player player = Bukkit.getPlayer(target);
        this.sendPunishMessage();
        if (player != null) {
            createPrompt();
        } else {
            setOffline(true);
        }
    }
    
    public void reversePunishment(UUID remover, long removedDate) {
    
    }
    
    public boolean isAcknowledged() {
        return acknowledged;
    }
    
    public void setAcknowledged(boolean value) {
        this.acknowledged = value;
    }
    
    public void onAcknowledge() {
        this.acknowledged = true;
    }
    
    public Prompt getPrompt() {
        if (this.prompt == null) {
            return createPrompt();
        }
        return prompt;
    }
}