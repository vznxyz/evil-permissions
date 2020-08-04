package net.evilblock.permissions.plugin.bukkit.rank.conversation

import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.plugin.bukkit.BukkitPlugin
import net.evilblock.permissions.plugin.bukkit.rank.menu.EditAttributesMenu
import net.evilblock.permissions.plugin.bukkit.rank.menu.RanksMenu
import net.evilblock.permissions.rank.RankHandler
import org.bukkit.ChatColor
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.Prompt
import org.bukkit.conversations.StringPrompt
import org.bukkit.entity.Player
import java.util.regex.Pattern

class RankCreationPrompt : StringPrompt() {

    private val namePattern = Pattern.compile("^[A-Za-z0-9_]+\$")

    override fun getPromptText(conversationContext: ConversationContext): String {
        return "${ChatColor.GREEN}Please provide a unique ID for the rank.\n${ChatColor.YELLOW}Type 'cancel' to cancel the creation procedure."
    }

    override fun acceptInput(conversationContext: ConversationContext, s: String): Prompt? {
        if (s.equals("cancel", ignoreCase = true)) {
            conversationContext.forWhom.sendRawMessage("${ChatColor.RED}Cancelled the creation procedure.")
            return Prompt.END_OF_CONVERSATION
        }

        if (s.length > 32) {
            conversationContext.forWhom.sendRawMessage("${ChatColor.RED}Rank names must be 1-32 characters long.")
            return Prompt.END_OF_CONVERSATION
        }

        if (!namePattern.matcher(s).matches()) {
            conversationContext.forWhom.sendRawMessage("${ChatColor.RED}Invalid character input. Allowed characters: [A-Z, 0-9, _]")
            return Prompt.END_OF_CONVERSATION
        }

        if (RankHandler.getRankById(s) != null) {
            conversationContext.forWhom.sendRawMessage("${ChatColor.RED}A rank by that ID already exists.")
            return Prompt.END_OF_CONVERSATION
        }

        val rank = RankHandler.createRank(s)

        conversationContext.forWhom.sendRawMessage("${ChatColor.GREEN}Created new rank.")

        BukkitPlugin.instance.server.scheduler.runTaskAsynchronously(BukkitPlugin.instance) {
            EvilPermissions.instance.database.saveRank(rank)
        }

        EditAttributesMenu(RanksMenu("GLOBAL"), rank).openMenu(conversationContext.forWhom as Player)

        return Prompt.END_OF_CONVERSATION
    }

}
