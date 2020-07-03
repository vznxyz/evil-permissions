package net.evilblock.permissions.plugin.bukkit.rank.menu.attribute

import mkremins.fanciful.FancyMessage
import net.evilblock.cubed.menu.Button
import net.evilblock.permissions.plugin.bukkit.BukkitPlugin
import net.evilblock.permissions.rank.Rank
import net.evilblock.permissions.plugin.bukkit.rank.menu.EditAttributesMenu
import net.evilblock.permissions.plugin.bukkit.rank.menu.attribute.conversation.AddGroupConversation
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.conversations.ConversationFactory
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView

class EditGroupsButton(private val parent: EditAttributesMenu, private val rank: Rank) : Button() {

    override fun getName(player: Player): String {
        return "${ChatColor.AQUA}Edit Groups"
    }

    override fun getDescription(player: Player): List<String> {
        return arrayListOf(
            "",
            "${ChatColor.GREEN}${ChatColor.BOLD}LEFT-CLICK to add a group",
            "${ChatColor.RED}${ChatColor.BOLD}RIGHT-CLICK to remove groups"
        )
    }

    override fun getMaterial(player: Player): Material {
        return Material.HOPPER
    }

    override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
        if (clickType.isLeftClick) {
            player.closeInventory()

            // start conversation
            val factory = ConversationFactory(BukkitPlugin.instance)
                .withFirstPrompt(AddGroupConversation(parent, setOf(rank)))
                .withLocalEcho(false)
                .thatExcludesNonPlayersWithMessage("Go away evil console!")

            player.beginConversation(factory.buildConversation(player))
        } else {
            player.closeInventory()

            val groupsMessage = FancyMessage("")

            rank.groups.forEachIndexed { index, group ->
                groupsMessage
                    .then(group).color(ChatColor.BLUE).style(ChatColor.BOLD)
                    .then("[").color(ChatColor.GRAY)
                    .then("✗").color(ChatColor.RED).style(ChatColor.BOLD)
                    .formattedTooltip(FancyMessage("${ChatColor.YELLOW}Click to remove this group."))
                    .command("/rank editor rm_group ${rank.id} $group")
                    .then("]").color(ChatColor.GRAY)

                if (index != rank.groups.size - 1) {
                    groupsMessage.then(", ").color(ChatColor.WHITE)
                }
            }

            player.sendMessage("")

            FancyMessage("${ChatColor.YELLOW}Groups of ${rank.displayName}")
                .style(ChatColor.BOLD)
                .formattedTooltip(FancyMessage("${ChatColor.YELLOW}You are viewing a list of ${rank.displayName}'s groups."))
                .send(player)

            groupsMessage.send(player)

            player.sendMessage("")
        }
    }

}