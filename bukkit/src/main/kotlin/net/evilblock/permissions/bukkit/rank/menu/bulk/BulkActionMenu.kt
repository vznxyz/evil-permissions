package net.evilblock.permissions.bukkit.rank.menu.bulk

import mkremins.fanciful.FancyMessage
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.menu.buttons.BackButton
import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.bukkit.BukkitPlugin
import net.evilblock.permissions.bukkit.rank.menu.RanksMenu
import net.evilblock.permissions.bukkit.rank.menu.attribute.conversation.AddGroupConversation
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.conversations.ConversationFactory
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView

class BulkActionMenu(private val parent: RanksMenu) : Menu() {

    override fun getTitle(player: Player): String {
        return "Bulk Action"
    }

    override fun getButtons(player: Player): Map<Int, Button> {
        val buttons = hashMapOf<Int, Button>()

        // buttons positioned on left
        buttons[0] = EditGroupsButton(this)
        buttons[1] = HiddenAttributeButton(this)

        // back button positioned on right
        buttons[8] = BackButton {
            parent.openMenu(player)
        }

        // toolbar placeholders
        for (i in 0..8) {
            if (!buttons.containsKey(i)) {
                buttons[i] = Button.placeholder(Material.STAINED_GLASS_PANE, 8, " ")
            }
        }

        return buttons
    }

    private class EditGroupsButton(private val parent: BulkActionMenu) : Button() {

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
                    .withFirstPrompt(AddGroupConversation(parent, parent.parent.bulkSelection))
                    .withLocalEcho(false)
                    .thatExcludesNonPlayersWithMessage("Go away evil console!")

                player.beginConversation(factory.buildConversation(player))
            } else {
                // start tracking the bulk selection
                BulkSelection.track(player, parent.parent.bulkSelection, parent)

                // close the menu
                player.closeInventory()

                val groups = flatten(parent.parent.bulkSelection.map { it.groups.toSet() })
                val groupsMessage = FancyMessage("")

                groups.forEachIndexed { index, group ->
                    groupsMessage
                        .then(group).color(ChatColor.BLUE).style(ChatColor.BOLD)
                        .then("[").color(ChatColor.GRAY)
                        .then("✗").color(ChatColor.RED).style(ChatColor.BOLD)
                        .formattedTooltip(FancyMessage("${ChatColor.YELLOW}Click to remove this group from your selected ranks."))
                        .command("/rank editor rm_group_bulk $group")
                        .then("]").color(ChatColor.GRAY)

                    if (index != groups.size - 1) {
                        groupsMessage.then(", ").color(ChatColor.WHITE)
                    }
                }

                val headerMessage = FancyMessage("${ChatColor.YELLOW}${ChatColor.BOLD}Combined groups of bulk selection")
                    .formattedTooltip(FancyMessage("${ChatColor.YELLOW}You are viewing a combined list of groups collected from your bulk selection."))

                val reopenMessage = FancyMessage("${ChatColor.GRAY}[${ChatColor.AQUA}${ChatColor.BOLD}RE-OPEN MENU${ChatColor.BOLD}${ChatColor.GRAY}]")
                    .command("/rank editor reopen_bulk_sel")
                    .formattedTooltip(FancyMessage("${ChatColor.YELLOW}Click here to re-open the bulk selection menu."))

                player.sendMessage("")
                headerMessage.send(player)
                groupsMessage.send(player)
                player.sendMessage("")
                reopenMessage.send(player)
                player.sendMessage("")
            }
        }

        private fun <T> flatten(mapped: List<Set<T>>): Set<T> {
            val result = HashSet<T>()
            for (element in mapped) {
                result.addAll(element)
            }
            return result
        }

    }

    private class HiddenAttributeButton(private val parent: BulkActionMenu) : Button() {

        override fun getName(player: Player): String {
            return "${ChatColor.AQUA}Hidden"
        }

        override fun getMaterial(player: Player): Material {
            return Material.EYE_OF_ENDER
        }

        override fun getDescription(player: Player): List<String> {
            return arrayListOf(
                "${ChatColor.GRAY}If hidden, this rank will not be",
                "${ChatColor.GRAY}displayed in the /list command",
                "${ChatColor.GRAY}and users will not be notified",
                "${ChatColor.GRAY}in chat when they receive this rank.",
                "",
                "${ChatColor.AQUA}${ChatColor.BOLD}LEFT-CLICK to make hidden",
                "${ChatColor.AQUA}${ChatColor.BOLD}RIGHT-CLICK to make visible"
            )
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            if (!(clickType.isLeftClick || clickType.isRightClick)) {
                return
            }

            for (rank in parent.parent.bulkSelection) {
                rank.hidden = clickType.isLeftClick
                EvilPermissions.instance.database.saveRank(rank)
            }

            player.sendMessage("${ChatColor.AQUA}Updated visibility of selected ranks.")
        }

    }

}