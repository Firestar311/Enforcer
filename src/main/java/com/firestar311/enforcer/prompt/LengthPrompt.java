package com.firestar311.enforcer.prompt;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.lib.util.Utils;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;

public class LengthPrompt extends NumericPrompt {
    protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
        Enforcer plugin = (Enforcer) context.getPlugin();
        Player player = (Player) context.getForWhom();
       // plugin.getRuleManager().getRuleBuilder(player.getUniqueId()).setId((Integer) input);
        return new MessagePrompt() {
            public String getPromptText(ConversationContext context) {
                return Utils.color("&aSet the length to " + input);
            }
            protected Prompt getNextPrompt(ConversationContext context) {
                Player player = (Player) context.getForWhom();
                
                Enforcer plugin = (Enforcer) context.getPlugin();
//                OffensePunishmentCreateGUI gui = plugin.getRuleManager().getOffenseCreateGUI(player);
//
//                GUIButton button = gui.getLengthButton();
//                ItemBuilder builder = new ItemBuilder(button.getItem());
//                builder.withLore("", Utils.color("&aCurrent Value: " + input));
//                button.setItem(builder.buildItem());
//
//                player.openInventory(gui.getInventory());
                return END_OF_CONVERSATION;
            }
        };
    }
    
    public String getPromptText(ConversationContext context) {
        return Utils.color("&aPlease provide the length as a number for this punishment");
    }
}
