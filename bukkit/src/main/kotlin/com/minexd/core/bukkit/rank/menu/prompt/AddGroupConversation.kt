package com.minexd.core.bukkit.rank.menu.prompt

import net.evilblock.cubed.menu.Menu
import com.minexd.core.CoreXD
import com.minexd.core.rank.Rank
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.ChatColor
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.Prompt
import org.bukkit.conversations.StringPrompt
import org.bukkit.entity.Player
import java.util.regex.Pattern

class AddGroupConversation(private val origin: Menu, private val rank: Rank) : StringPrompt() {

    private val groupPattern = Pattern.compile("^[A-Za-z0-9_]+\$")

    override fun getPromptText(conversationContext: ConversationContext): String {
        return "${ChatColor.GREEN}Please provide a group to assign.\n${ChatColor.YELLOW}Type 'cancel' to cancel the edit procedure."
    }

    override fun acceptInput(conversationContext: ConversationContext, input: String): Prompt? {
        if (input.equals("cancel", ignoreCase = true)) {
            conversationContext.forWhom.sendRawMessage("${ChatColor.RED}Cancelled the edit procedure.")
            return Prompt.END_OF_CONVERSATION
        }

        if (input.length > 32) {
            conversationContext.forWhom.sendRawMessage("${ChatColor.RED}Rank groups must be 1-16 characters long.")
            return Prompt.END_OF_CONVERSATION
        }

        if (!groupPattern.matcher(input).matches()) {
            conversationContext.forWhom.sendRawMessage("${ChatColor.RED}Invalid character input. Allowed characters: [A-Z, 0-9, _]")
            return Prompt.END_OF_CONVERSATION
        }

        Tasks.async {
            val response = CoreXD.instance.ranksService.update(rank.id, mapOf("addGroup" to input)).execute()
            if (response.isSuccessful) {
                Tasks.sync {
                    origin.openMenu(conversationContext.forWhom as Player)
                }
            } else {
                conversationContext.forWhom.sendRawMessage("${ChatColor.RED}${response.errorBody()!!.string()}!")
            }
        }

        return Prompt.END_OF_CONVERSATION
    }

}
