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

class AddPermissionConversation(private val parent: EditAttributesMenu, private val rank: Rank) : StringPrompt() {

    override fun getPromptText(conversationContext: ConversationContext): String {
        return "${ChatColor.GREEN}Please provide the permission node.\n${ChatColor.YELLOW}Type 'cancel' to cancel the edit procedure."
    }

    override fun acceptInput(conversationContext: ConversationContext, s: String): Prompt? {
        if (s.equals("cancel", ignoreCase = true)) {
            conversationContext.forWhom.sendRawMessage("${ChatColor.RED}Cancelled the edit procedure.")
            return Prompt.END_OF_CONVERSATION
        }

        if (rank.permissions.add(s)) {
            conversationContext.forWhom.sendRawMessage("${ChatColor.GREEN}Added permission node.")

            BukkitPlugin.instance.server.scheduler.runTaskAsynchronously(BukkitPlugin.instance) {
                EvilPermissions.instance.database.saveRank(rank)
            }

            BukkitPlugin.instance.server.pluginManager.callEvent(RankUpdateEvent(rank))

            parent.openMenu(conversationContext.forWhom as Player)
        } else {
            conversationContext.forWhom.sendRawMessage("${ChatColor.RED}Permission node already assigned to rank.")
        }

        return Prompt.END_OF_CONVERSATION
    }

}
