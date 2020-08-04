package net.evilblock.permissions.plugin.bukkit.user.grant.menu

import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.bukkit.enchantment.GlowEnchantment
import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.plugin.bukkit.BukkitPlugin
import net.evilblock.permissions.rank.Rank
import net.evilblock.permissions.user.User
import net.evilblock.permissions.plugin.bukkit.user.grant.conversation.GrantCreationReasonPrompt
import net.evilblock.permissions.plugin.bukkit.util.ColorMap
import net.evilblock.permissions.rank.RankHandler
import net.evilblock.permissions.util.Permissions
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.conversations.ConversationFactory
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import java.lang.Exception

class GrantMenu(val user: User) : Menu("Grant ${user.getPlayerListPrefix() + user.getUsername()}") {

    override fun getButtons(player: Player): Map<Int, Button> {
        val buttons = hashMapOf<Int, Button>()

        for (rank in RankHandler.getRanks().filter { it.isActiveOnServer() }.sortedBy { it.displayOrder }) {
            if (rank.default) continue

            if (canBeGranted(player, rank)) {
                buttons[buttons.size] = GrantRankButton(rank, user)
            }
        }

        return buttons
    }

    /**
     * If the given [rank] can be granted by the given [issuer].
     */
    private fun canBeGranted(issuer: Player, rank: Rank): Boolean {
        if (!issuer.hasPermission(Permissions.GRANT)) {
            return false
        }

        if (issuer.hasPermission(Permissions.GRANT + ".*")) {
            return true
        }

        if (issuer.hasPermission(Permissions.GRANT + ".${rank.id}")) {
            return true
        }

        return false
    }

    private class GrantRankButton(private val rank: Rank, private val user: User) : Button() {

        override fun getButtonItem(player: Player): ItemStack {
            val itemStack = super.getButtonItem(player)

            if (rank.getDisplayColorChar() == "4") {
                GlowEnchantment.addGlow(itemStack)
            }

            return itemStack
        }

        override fun getName(player: Player): String {
            var name = rank.getColoredDisplayName()

            if (rank.prefix.isNotBlank()) {
                name = name + " " + rank.prefix
            }

            return name
        }

        override fun getDescription(player: Player): List<String> {
            val description = arrayListOf<String>()
            description.add("")

            if (rank.groups.isEmpty()) {
                description.add("${ChatColor.YELLOW}${ChatColor.BOLD}Active on: ${ChatColor.RESET}None assigned")
            } else {
                val groupsConcat = rank.groups.joinToString { "${ChatColor.BLUE}${ChatColor.BOLD}$it${ChatColor.RESET}" }
                description.add("${ChatColor.YELLOW}${ChatColor.BOLD}Active on: " + groupsConcat)
            }

            description.add("")
            description.add("${ChatColor.GRAY}Click to grant ${ChatColor.RESET}${user.getPlayerListPrefix() + user.getUsername()}${ChatColor.GRAY} the ${rank.getColoredDisplayName()}${ChatColor.GRAY} rank.")

            return description
        }

        override fun getMaterial(player: Player): Material {
            return Material.INK_SACK
        }

        override fun getDamageValue(player: Player): Byte {
            return try {
                (ColorMap.dyeMap[ChatColor.getByChar(rank.getDisplayColorChar())]?: 15).toByte()
            } catch (e: Exception) {
                return 15.toByte()
            }
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            for (grant in user.grants) {
                if (grant.isActive() && grant.rank == rank) {
                    player.sendMessage("${ChatColor.RED}That player has already been assigned that rank.")
                    return
                }
            }

            player.closeInventory()

            val factory = ConversationFactory(BukkitPlugin.instance)
                .withFirstPrompt(GrantCreationReasonPrompt(user, rank))
                .withLocalEcho(false)
                .thatExcludesNonPlayersWithMessage("Go away evil console!")

            player.beginConversation(factory.buildConversation(player))
        }

    }

}
