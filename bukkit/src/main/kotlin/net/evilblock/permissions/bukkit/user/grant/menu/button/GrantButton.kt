package net.evilblock.permissions.bukkit.user.grant.menu.button

import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.bukkit.prompt.EzPrompt
import net.evilblock.cubed.util.bukkit.prompt.InputPrompt
import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.bukkit.BukkitPlugin
import net.evilblock.permissions.rank.Rank
import net.evilblock.permissions.user.User
import net.evilblock.permissions.user.grant.Grant
import net.evilblock.permissions.util.Permissions
import net.evilblock.permissions.util.TimeUtils
import net.evilblock.pidgin.message.Message
import org.apache.commons.lang.StringUtils
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import java.util.*

class GrantButton(
    private val returnTo: Menu,
    private val user: User,
    private val grant: Grant,
    private val issuedBy: User?,
    private val removedBy: User?,
    private val appendIssuedTo: Boolean = false
) : Button() {

    override fun getName(player: Player): String {
        return StringUtils.capitalize(grant.rank.getColoredDisplayName())
    }

    override fun getDescription(player: Player): List<String> {
        val issuerName = if (grant.issuedBy == null) {
            "Console"
        } else {
            issuedBy?.getPlayerListPrefix() + issuedBy?.getUsername()
        }

        val description = arrayListOf<String>()
        description.add("${ChatColor.YELLOW}Active on: ${grant.rank.groups.joinToString { "${ChatColor.BLUE}${ChatColor.BOLD}$it" }}")

        if (appendIssuedTo) {
            description.add("${ChatColor.YELLOW}Issued to: ${ChatColor.RED}${user.getPlayerListPrefix()}${user.getUsername()}")
        }

        description.add("${ChatColor.YELLOW}Issued by: ${ChatColor.RED}$issuerName")
        description.add("${ChatColor.YELLOW}Issued on: ${ChatColor.RED}${TimeUtils.formatIntoCalendarString(Date(grant.issuedAt))}")
        description.add("${ChatColor.YELLOW}Reason: ${ChatColor.RED}${ChatColor.ITALIC}${grant.reason}")

        if (grant.isActive()) {
            if (grant.expiresAt == null) {
                description.add("${ChatColor.YELLOW}Duration: ${ChatColor.RED}Permanent")
            } else {
                description.add("${ChatColor.YELLOW}Time remaining: ${ChatColor.RED}" + TimeUtils.formatIntoDetailedString(((grant.expiresAt!! - System.currentTimeMillis()) / 1_000L).toInt()))
            }

            if (canBeRemovedBy(player, grant.rank)) {
                description.add(BAR)
                description.add("${ChatColor.YELLOW}Click to remove this grant")
            }
        } else if (grant.removedAt != null) {
            val removerName = if (grant.removedBy == null) {
                "Console"
            } else {
                removedBy?.getPlayerListPrefix() + removedBy?.getUsername()
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
        if (grant.removedAt == null && canBeRemovedBy(player, grant.rank)) {
            player.closeInventory()

            InputPrompt()
                    .withText("${ChatColor.GREEN}Please specify a valid reason.")
                    .acceptInput { input ->
                        Tasks.async {
                            grant.removedBy = player.uniqueId
                            grant.removedAt = System.currentTimeMillis()
                            grant.removalReason = input

                            BukkitPlugin.instance.server.scheduler.runTaskAsynchronously(BukkitPlugin.instance) {
                                EvilPermissions.instance.database.saveUser(user)
                                EvilPermissions.instance.pidgin.sendMessage(Message("GRANT_UPDATE", mapOf("uniqueId" to user.uniqueId.toString(), "grant" to grant.id.toString())))
                            }

                            player.sendMessage("${ChatColor.GOLD}Grant removed.")

                            returnTo.openMenu(player)
                        }
                    }
                    .start(player)
        }
    }

    companion object {
        private val BAR = "${ChatColor.GRAY}${ChatColor.STRIKETHROUGH}${StringUtils.repeat("-", 32)}"

        private fun canBeRemovedBy(remover: Player, rank: Rank): Boolean {
            if (!remover.hasPermission(Permissions.GRANT_REMOVE)) {
                return false
            }

            if (remover.hasPermission(Permissions.GRANT_REMOVE + ".*")) {
                return true
            }

            if (remover.hasPermission(Permissions.GRANT_REMOVE + ".${rank.id}")) {
                return true
            }

            return false
        }
    }

}