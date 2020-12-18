package net.evilblock.permissions.bukkit.rank.menu.attribute

import mkremins.fanciful.FancyMessage
import net.evilblock.cubed.menu.Button
import net.evilblock.permissions.bukkit.BukkitPlugin
import net.evilblock.permissions.rank.Rank
import net.evilblock.permissions.bukkit.rank.menu.EditAttributesMenu
import net.evilblock.permissions.bukkit.rank.menu.attribute.conversation.AddPermissionConversation
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.conversations.ConversationFactory
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView

class EditPermissionsButton(private val parent: EditAttributesMenu, private val rank: Rank) : Button() {

    override fun getName(player: Player): String {
        return "${ChatColor.AQUA}Edit Permissions"
    }

    override fun getDescription(player: Player): List<String> {
        return arrayListOf(
            "",
            "${ChatColor.GREEN}${ChatColor.BOLD}LEFT-CLICK to add a permission",
            "${ChatColor.RED}${ChatColor.BOLD}RIGHT-CLICK to remove permissions"
        )
    }

    override fun getMaterial(player: Player): Material {
        return Material.BONE
    }

    override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
        if (clickType.isLeftClick) {
            player.closeInventory()

            // start conversation
            val factory = ConversationFactory(BukkitPlugin.instance)
                .withFirstPrompt(AddPermissionConversation(parent, rank))
                .withLocalEcho(false)
                .thatExcludesNonPlayersWithMessage("Go away evil console!")

            player.beginConversation(factory.buildConversation(player))
        } else {
            player.closeInventory()

            val permissionsMessages = arrayListOf<FancyMessage>()
            var currentBaseMessage = FancyMessage("")

            rank.permissions.sorted().forEachIndexed { index, permission ->
                currentBaseMessage
                    .then(permission).color(ChatColor.WHITE)
                    .then("[").color(ChatColor.GRAY)
                    .then("✗").color(ChatColor.RED).style(ChatColor.BOLD)
                    .formattedTooltip(FancyMessage("${ChatColor.YELLOW}Click to remove this permission node."))
                    .command("/rank editor rm_perm ${rank.id} $permission")
                    .then("]").color(ChatColor.GRAY)

                if (index != rank.permissions.size - 1) {
                    currentBaseMessage.then(", ").color(ChatColor.WHITE)
                }

                if (currentBaseMessage.toJSONString().length >= 30000) {
                    permissionsMessages.add(currentBaseMessage)
                    currentBaseMessage = FancyMessage("")
                }
            }

            permissionsMessages.add(currentBaseMessage)

            player.sendMessage("")

            FancyMessage("${ChatColor.YELLOW}Permission nodes of ${rank.displayName}")
                .style(ChatColor.BOLD)
                .formattedTooltip(FancyMessage("${ChatColor.YELLOW}You are viewing a list of ${rank.displayName}'s permission nodes."))
                .send(player)

            for (permissionsMessage in permissionsMessages) {
                permissionsMessage.send(player)
            }

            player.sendMessage("")
        }
    }

}