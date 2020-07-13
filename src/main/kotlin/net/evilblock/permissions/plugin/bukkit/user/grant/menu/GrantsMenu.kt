package net.evilblock.permissions.plugin.bukkit.user.grant.menu

import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.plugin.bukkit.BukkitPlugin
import net.evilblock.permissions.user.User
import net.evilblock.permissions.user.grant.Grant
import net.evilblock.permissions.plugin.bukkit.user.grant.conversation.GrantRemovalPrompt
import net.evilblock.permissions.util.TimeUtils
import org.apache.commons.lang.StringUtils
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.conversations.ConversationFactory
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import java.lang.Exception
import java.util.*
import kotlin.collections.HashMap

class GrantsMenu(internal val target: User) : PaginatedMenu() {

    internal data class GrantResourceData(val issuer: User?, val remover: User?)

    internal val resources = hashMapOf<Grant, GrantResourceData>()

    init {
        async = true
    }

    override fun asyncLoadResources(callback: (Boolean) -> Unit) {
        try {
            for (grant in target.grants) {
                var issuer: User? = null
                var remover: User? = null

                if (grant.issuedBy != null) {
                    issuer = EvilPermissions.instance.userHandler.loadOrCreate(grant.issuedBy!!)
                }

                if (grant.removedBy != null) {
                    remover = EvilPermissions.instance.userHandler.loadOrCreate(grant.removedBy!!)
                }

                resources[grant] = GrantResourceData(issuer, remover)
            }

            callback.invoke(true)
        } catch (e: Exception) {
            callback.invoke(false)
        }
    }

    override fun getPrePaginatedTitle(player: Player): String {
        return "${ChatColor.RED}Grants - ${target.getUsername()}"
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button> {
        val buttons = HashMap<Int, Button>()

        val grants = target.grants.sortedBy { -it.issuedAt }
        for (grant in grants) {
            buttons[buttons.size] = GrantButton(this, grant)
        }

        return buttons
    }

    private class GrantButton(private val parent: GrantsMenu, private val grant: Grant) : Button() {

        companion object {
            private val BAR = "${ChatColor.GRAY}${ChatColor.STRIKETHROUGH}${StringUtils.repeat("-", 32)}"
        }

        override fun getName(player: Player): String {
            return StringUtils.capitalize(grant.rank.getColoredDisplayName())
        }

        override fun getDescription(player: Player): List<String> {
            val resourceData = parent.resources[grant]!!

            val issuerName = if (grant.issuedBy == null) {
                "Console"
            } else {
                resourceData.issuer?.getPlayerListPrefix() + resourceData.issuer?.getUsername()
            }

            val description = arrayListOf<String>()
            description.add("${ChatColor.YELLOW}Active on: ${grant.rank.groups.joinToString { "${ChatColor.BLUE}${ChatColor.BOLD}$it" }}")
            description.add("${ChatColor.YELLOW}Issued by: ${ChatColor.RED}$issuerName")
            description.add("${ChatColor.YELLOW}Issued on: ${ChatColor.RED}${TimeUtils.formatIntoCalendarString(Date(grant.issuedAt))}")
            description.add("${ChatColor.YELLOW}Reason: ${ChatColor.RED}${ChatColor.ITALIC}${grant.reason}")

            if (grant.isActive()) {
                if (grant.expiresAt == null) {
                    description.add("${ChatColor.YELLOW}Duration: ${ChatColor.RED}Permanent")
                } else {
                    description.add("${ChatColor.YELLOW}Time remaining: ${ChatColor.RED}" + TimeUtils.formatIntoDetailedString(((grant.expiresAt!! - System.currentTimeMillis()) / 1_000L).toInt()))
                }

                if (grant.canBeRemovedBy(player)) {
                    description.add(BAR)
                    description.add("${ChatColor.YELLOW}Click to remove this grant.")
                }
            } else if (grant.removedAt != null) {
                val removerName = if (grant.removedBy == null) {
                    "Console"
                } else {
                    resourceData.remover?.getPlayerListPrefix() + resourceData.remover?.getUsername()
                }

                description.add(BAR)
                description.add("${ChatColor.YELLOW}Removed by: ${ChatColor.RED}$removerName")
                description.add("${ChatColor.YELLOW}Removed on: ${ChatColor.RED}${TimeUtils.formatIntoCalendarString(Date(grant.removedAt!!))}")
                description.add("${ChatColor.YELLOW}Reason: ${ChatColor.RED}${ChatColor.ITALIC}${grant.removalReason}")
            }

            description.add(0, BAR)
            description.add(BAR)

            return description
        }

        override fun getMaterial(player: Player): Material {
            return Material.INK_SACK
        }

        override fun getDamageValue(player: Player): Byte {
            return if (grant.isActive()) {
                10
            } else {
                1
            }
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            if (grant.removedAt == null && grant.canBeRemovedBy(player)) {
                player.closeInventory()

                val factory = ConversationFactory(BukkitPlugin.instance)
                    .withFirstPrompt(GrantRemovalPrompt(parent.target, grant))
                    .withLocalEcho(false)
                    .thatExcludesNonPlayersWithMessage("Go away evil console!")

                player.beginConversation(factory.buildConversation(player))
            }
        }

    }

}