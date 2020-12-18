package net.evilblock.permissions.bukkit.rank.menu.attribute.conversation

import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.bukkit.BukkitPlugin
import net.evilblock.permissions.bukkit.rank.event.RankUpdateEvent
import net.evilblock.permissions.bukkit.rank.menu.EditAttributesMenu
import net.evilblock.permissions.rank.Rank
import org.bukkit.ChatColor
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.Prompt
import org.bukkit.conversations.StringPrompt
import org.bukkit.entity.Player

class EditAttributeConversation(private val parent: EditAttributesMenu, private val attribute: Attribute, private val rank: Rank) : StringPrompt() {

    override fun getPromptText(conversationContext: ConversationContext): String {
        return attribute.promptText + "\n${ChatColor.YELLOW}Type 'cancel' to cancel the edit procedure."
    }

    override fun acceptInput(conversationContext: ConversationContext, s: String): Prompt? {
        if (s.equals("cancel", ignoreCase = true)) {
            conversationContext.forWhom.sendRawMessage("${ChatColor.RED}Cancelled the edit procedure.")
            return Prompt.END_OF_CONVERSATION
        }

        var successful = false

        when (attribute) {
            Attribute.DISPLAY_NAME -> {
                successful = true
                rank.displayName = s
            }
            Attribute.PREFIX -> {
                successful = true
                rank.prefix = s
            }
            Attribute.PLAYER_LIST_PREFIX -> {
                if (s.length > 16) {
                    conversationContext.forWhom.sendRawMessage("${ChatColor.RED}The player list prefix can't be longer than 16 characters.")
                } else {
                    successful = true
                    rank.playerListPrefix = ChatColor.translateAlternateColorCodes('&', s)
                }
            }
        }

        if (successful) {
            conversationContext.forWhom.sendRawMessage(attribute.updateText)

            BukkitPlugin.instance.server.scheduler.runTaskAsynchronously(BukkitPlugin.instance) {
                EvilPermissions.instance.database.saveRank(rank)
            }

            BukkitPlugin.instance.server.pluginManager.callEvent(RankUpdateEvent(rank))

            parent.openMenu(conversationContext.forWhom as Player)
        }

        return Prompt.END_OF_CONVERSATION
    }

    enum class Attribute(val buttonTitle: String,
                         val promptText: String,
                         val updateText: String) {
        DISPLAY_NAME(
                "${ChatColor.AQUA}Display Name",
                "${ChatColor.GREEN}Please provide a new display name.",
                "${ChatColor.AQUA}Updated display name."
        ),
        PREFIX(
                "${ChatColor.AQUA}Prefix",
                "${ChatColor.GREEN}Please provide a new prefix.",
                "${ChatColor.AQUA}Updated prefix."
        ),
        PLAYER_LIST_PREFIX(
                "${ChatColor.AQUA}Player List Prefix",
                "${ChatColor.GREEN}Please provide a new player list prefix.",
                "${ChatColor.AQUA}Updated player list prefix."
        )
    }

}
