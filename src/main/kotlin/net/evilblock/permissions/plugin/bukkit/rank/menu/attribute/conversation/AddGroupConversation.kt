package net.evilblock.permissions.plugin.bukkit.rank.menu.attribute.conversation

import net.evilblock.cubed.menu.Menu
import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.plugin.bukkit.BukkitPlugin
import net.evilblock.permissions.plugin.bukkit.rank.event.RankUpdateEvent
import net.evilblock.permissions.rank.Rank
import org.bukkit.ChatColor
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.Prompt
import org.bukkit.conversations.StringPrompt
import org.bukkit.entity.Player
import java.util.regex.Pattern

class AddGroupConversation(private val parent: Menu, private val ranks: Set<Rank>) : StringPrompt() {

    private val groupPattern = Pattern.compile("^[A-Za-z0-9_]+\$")

    override fun getPromptText(conversationContext: ConversationContext): String {
        return "${ChatColor.GREEN}Please provide a group to assign.\n${ChatColor.YELLOW}Type 'cancel' to cancel the edit procedure."
    }

    override fun acceptInput(conversationContext: ConversationContext, s: String): Prompt? {
        if (s.equals("cancel", ignoreCase = true)) {
            conversationContext.forWhom.sendRawMessage("${ChatColor.RED}Cancelled the edit procedure.")
            return Prompt.END_OF_CONVERSATION
        }

        if (s.length > 32) {
            conversationContext.forWhom.sendRawMessage("${ChatColor.RED}Rank groups must be 1-16 characters long.")
            return Prompt.END_OF_CONVERSATION
        }

        if (!groupPattern.matcher(s).matches()) {
            conversationContext.forWhom.sendRawMessage("${ChatColor.RED}Invalid character input. Allowed characters: [A-Z, 0-9, _]")
            return Prompt.END_OF_CONVERSATION
        }

        var added = false

        for (rank in ranks) {
            added = rank.groups.add(s)
        }

        if (ranks.size == 1) {
            if (added) {
                conversationContext.forWhom.sendRawMessage("${ChatColor.AQUA}Added group ${ChatColor.BLUE}${ChatColor.BOLD}$s ${ChatColor.AQUA}to rank.")
            } else {
                conversationContext.forWhom.sendRawMessage("${ChatColor.RED}Group ${ChatColor.BLUE}${ChatColor.BOLD}$s ${ChatColor.RED}has already been assigned to that rank.")
                return Prompt.END_OF_CONVERSATION
            }
        } else {
            conversationContext.forWhom.sendRawMessage("${ChatColor.AQUA}Added group ${ChatColor.BLUE}${ChatColor.BOLD}$s ${ChatColor.AQUA}to selected ranks.")
        }

        BukkitPlugin.instance.server.scheduler.runTaskAsynchronously(BukkitPlugin.instance) {
            for (rank in ranks) {
                EvilPermissions.instance.database.saveRank(rank)

                BukkitPlugin.instance.server.pluginManager.callEvent(RankUpdateEvent(rank))
            }
        }

        parent.openMenu(conversationContext.forWhom as Player)

        return Prompt.END_OF_CONVERSATION
    }

}
