package net.evilblock.permissions.plugin.bukkit.user.grant.conversation

import net.evilblock.permissions.rank.Rank
import net.evilblock.permissions.user.User
import org.bukkit.ChatColor
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.Prompt
import org.bukkit.conversations.StringPrompt

class GrantCreationReasonPrompt(val user: User, val rank: Rank) : StringPrompt() {

    override fun getPromptText(conversationContext: ConversationContext): String {
        return ChatColor.GREEN.toString() + "Please specify a valid reason."
    }

    override fun acceptInput(conversationContext: ConversationContext, s: String): Prompt? {
        conversationContext.setSessionData("rank", rank)
        conversationContext.setSessionData("reason", s)
        return GrantCreationPeriodPrompt(user)
    }

}
