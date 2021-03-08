package com.minexd.core.bukkit.profile.grant.menu.button

import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.bukkit.prompt.InputPrompt
import com.minexd.core.CoreXD
import com.minexd.core.profile.Profile
import com.minexd.core.rank.Rank
import com.minexd.core.profile.grant.Grant
import com.minexd.core.util.Permissions
import com.minexd.core.util.TimeUtils
import org.apache.commons.lang.StringUtils
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import java.util.*

class GrantButton(
        private val returnTo: Menu,
        private val profile: Profile,
        private val grant: Grant,
        private val issuedBy: Profile?,
        private val removedBy: Profile?,
        private val appendIssuedTo: Boolean = false
) : Button() {

    override fun getName(player: Player): String {
        return StringUtils.capitalize(grant.rank.getColoredDisplayName())
    }

    override fun getDescription(player: Player): List<String> {
        val description = arrayListOf<String>()
        description.add("${ChatColor.YELLOW}Active on: ${ChatColor.RESET}${grant.rank.groups.joinToString { "$it" }}")

        if (appendIssuedTo) {
            description.add("${ChatColor.YELLOW}Issued to: ${ChatColor.RESET}${profile.getColoredUsername()}")
        }

        val issuerName = if (grant.issuedBy == null) {
            "${ChatColor.DARK_RED}Console"
        } else {
            issuedBy?.getColoredUsername()
        }

        description.add("${ChatColor.YELLOW}Issued by: ${ChatColor.RESET}$issuerName")
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
        } else if (grant.removed) {
            val removerName = if (grant.removedBy == null) {
                "${ChatColor.DARK_RED}Console"
            } else {
                removedBy?.getColoredUsername()
            }

            description.add(BAR)
            description.add("${ChatColor.YELLOW}Removed by: ${ChatColor.RED}$removerName")
            description.add("${ChatColor.YELLOW}Removed on: ${ChatColor.RED}${TimeUtils.formatIntoCalendarString(Date(grant.removedAt!!))}")
            description.add("${ChatColor.YELLOW}Reason: ${ChatColor.RED}${ChatColor.ITALIC}${grant.removeReason}")
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
        if (grant.removed || !canBeRemovedBy(player, grant.rank)) {
            return
        }

        player.closeInventory()

        InputPrompt()
                .withText("${ChatColor.GREEN}Please specify a valid reason.")
                .acceptInput { input ->
                    val requestData = mapOf(
                            "rank" to grant.rank.id,
                            "removedBy" to player.uniqueId.toString(),
                            "removeReason" to input
                    )

                    Tasks.async {
                        try {
                            val response = CoreXD.instance.profilesService.revokeGrant(profile.uuid, requestData).execute()
                            if (response.isSuccessful) {
                                Tasks.sync {
                                    returnTo.openMenu(player)
                                }
                            } else {
                                player.sendMessage("${ChatColor.RED}${response.errorBody()!!.string()}!")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                .start(player)
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