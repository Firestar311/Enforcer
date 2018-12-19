package com.firestar311.enforcer.prompt;

import com.firestar311.enforcer.Enforcer;
import com.firestar311.lib.util.Utils;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;

public class InternalRuleIdPrompt extends StringPrompt {
    public Prompt acceptInput(ConversationContext context, String input) {
        Enforcer plugin = (Enforcer) context.getPlugin();
        Player player = (Player) context.getForWhom();
        //plugin.getRuleManager().getRuleBuilder(player.getUniqueId()).setInternalId(input);
        return new MessagePrompt() {
            public String getPromptText(ConversationContext context) {
                return Utils.color("&aSet the Internal Rule ID to " + input);
            }
            protected Prompt getNextPrompt(ConversationContext context) {
                Player player = (Player) context.getForWhom();
                
                Enforcer plugin = (Enforcer) context.getPlugin();
//                RuleCreateGUI gui = plugin.getRuleManager().getRuleGUI(player);
//
//                GUIButton button = gui.getiIdButton();
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
        return Utils.color("&aPlease provide the internal id to use for this rule.");
    }
}
