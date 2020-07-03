package net.evilblock.permissions.plugin.bukkit.user.grant.conversation

import net.evilblock.pidgin.message.Message
import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.plugin.bukkit.BukkitPlugin
import net.evilblock.permissions.user.User
import net.evilblock.permissions.user.grant.Grant
import org.bukkit.ChatColor
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.Prompt
import org.bukkit.conversations.StringPrompt
import org.bukkit.entity.Player

class GrantRemovalPrompt(val user: User, val grant: Grant) : StringPrompt() {

    override fun getPromptText(conversationContext: ConversationContext): String {
        return ChatColor.GREEN.toString() + "Please specify a valid reason."
    }

    override fun acceptInput(conversationContext: ConversationContext, s: String): Prompt? {
        grant.removedBy = (conversationContext.forWhom as Player).uniqueId
        grant.removedAt = System.currentTimeMillis()
        grant.removalReason = s

        BukkitPlugin.instance.server.scheduler.runTaskAsynchronously(BukkitPlugin.instance) {
            EvilPermissions.instance.database.saveUser(user)
            EvilPermissions.instance.pidgin.sendMessage(Message("GRANT_UPDATE", mapOf("uniqueId" to user.uniqueId.toString(), "grant" to grant.id.toString())))
        }

        conversationContext.forWhom.sendRawMessage("${ChatColor.GOLD}Grant removed.")

        return Prompt.END_OF_CONVERSATION
    }

}
