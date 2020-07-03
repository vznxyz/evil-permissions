package net.evilblock.permissions.plugin.bukkit.user.grant.conversation

import net.evilblock.pidgin.message.Message
import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.rank.Rank
import net.evilblock.permissions.user.User
import net.evilblock.permissions.user.grant.Grant
import net.evilblock.permissions.util.DateUtil
import net.evilblock.permissions.util.TimeUtils
import org.bukkit.ChatColor
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.Prompt
import org.bukkit.conversations.StringPrompt
import org.bukkit.entity.Player
import java.lang.Exception

class GrantCreationPeriodPrompt(val user: User) : StringPrompt() {

    override fun getPromptText(conversationContext: ConversationContext): String {
        return ChatColor.GREEN.toString() + "Please specify a valid time."
    }

    override fun acceptInput(conversationContext: ConversationContext, input: String): Prompt? {
        var perm = false
        var expiresAt = 0L

        if (input.toLowerCase() == "perm") {
            perm = true
        }

        if (!(perm)) {
            try {
                expiresAt = System.currentTimeMillis() - DateUtil.parseDateDiff(input, false)
            } catch (exception: Exception) {
                conversationContext.forWhom.sendRawMessage("${ChatColor.RED}Invalid duration.")
                return Prompt.END_OF_CONVERSATION
            }
        }

        val grant = Grant()
        grant.rank = conversationContext.getSessionData("rank") as Rank
        grant.reason = conversationContext.getSessionData("reason") as String
        grant.issuedBy = (conversationContext.forWhom as Player).uniqueId
        grant.issuedAt = System.currentTimeMillis()

        if (!perm) {
            grant.expiresAt = expiresAt + System.currentTimeMillis()
        }

        user.grants.add(grant)

        EvilPermissions.instance.database.saveUser(user)
        EvilPermissions.instance.pidgin.sendMessage(Message("GRANT_UPDATE", mapOf("uniqueId" to user.uniqueId.toString(), "grant" to grant.id.toString())))

        val period = if (grant.expiresAt == null) {
            "forever"
        } else {
            TimeUtils.formatIntoDetailedString(((grant.expiresAt!! - System.currentTimeMillis()) / 1000).toInt())
        }

        conversationContext.forWhom.sendRawMessage("${ChatColor.GREEN}You've granted ${user.getPlayerListPrefix() + user.getUsername()} ${ChatColor.GREEN}the ${grant.rank.getColoredDisplayName()} ${ChatColor.GREEN}rank for a period of ${ChatColor.YELLOW}$period${ChatColor.GREEN}.")

        return Prompt.END_OF_CONVERSATION
    }

}
