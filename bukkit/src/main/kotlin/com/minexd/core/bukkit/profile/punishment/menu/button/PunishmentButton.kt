package com.minexd.core.bukkit.profile.punishment.menu.button

import com.minexd.core.profile.punishment.Punishment
import com.minexd.core.CoreXD
import com.minexd.core.profile.Profile
import com.minexd.core.profile.punishment.PunishmentType
import net.evilblock.cubed.Cubed
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.time.TimeUtil
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.bukkit.prompt.InputPrompt
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import java.util.*

class PunishmentButton(
        private val returnTo: Menu,
        private val profile: Profile,
        private val punishment: Punishment,
        private val appendIssuedTo: Boolean = false
) : Button() {

    override fun getName(player: Player): String {
        return "${punishment.punishmentType.color}${ChatColor.BOLD}${punishment.punishmentType.name}"
    }

    override fun getDescription(player: Player): List<String> {
        val description = arrayListOf<String>()

        description.add(BAR)

        if (appendIssuedTo) {
            description.add("${ChatColor.YELLOW}Issued to: ${ChatColor.RESET}${profile.getUsername()}")
        }

        val issuedBy = if (punishment.issuedBy == null) {
            "${ChatColor.DARK_RED}Console"
        } else {
            Cubed.instance.uuidCache.name(punishment.issuedBy!!)
        }

        description.add("${ChatColor.YELLOW}Issued by: ${ChatColor.RED}$issuedBy")
        description.add("${ChatColor.YELLOW}Issued at: ${ChatColor.RED}${TimeUtil.formatIntoCalendarString(Date(punishment.issuedAt))}")
        description.add("${ChatColor.YELLOW}Reason: ${ChatColor.RED}${ChatColor.ITALIC}${punishment.reason}")

        if (punishment.isActive()) {
            if (punishment.expiresAt == null) {
                description.add("${ChatColor.YELLOW}Duration: ${ChatColor.RED}Permanent")
            } else {
                val timeRemaining = TimeUtil.formatIntoDetailedString(((punishment.expiresAt!! - System.currentTimeMillis()) / 1_000L).toInt())
                description.add("${ChatColor.YELLOW}Time remaining: ${ChatColor.RED}$timeRemaining")
            }

            if (player.hasPermission(punishment.punishmentType.getDeletePermission())) {
                description.add(BAR)
                description.add("${ChatColor.YELLOW}Click to pardon this punishment")
            }
        } else if (punishment.pardoned) {
            val removedBy = if (punishment.pardonedBy == null) {
                "${ChatColor.DARK_RED}Console"
            } else {
                Cubed.instance.uuidCache.name(punishment.pardonedBy!!)
            }

            val removedAt = TimeUtil.formatIntoCalendarString(Date(punishment.pardonedAt!!))

            description.add(BAR)
            description.add("${ChatColor.YELLOW}Removed by: ${ChatColor.RED}$removedBy")
            description.add("${ChatColor.YELLOW}Removed at: ${ChatColor.RED}$removedAt")
            description.add("${ChatColor.YELLOW}Reason: ${ChatColor.RED}${ChatColor.ITALIC}${punishment.pardonReason}")
        }

        description.add(BAR)

        return description
    }

    override fun getMaterial(player: Player): Material {
        return Material.INK_SACK
    }

    override fun getDamageValue(player: Player): Byte {
        return if (punishment.isActive()) {
            10
        } else {
            1
        }
    }

    override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
        if (punishment.pardoned) {
            return
        }

        InputPrompt()
                .withText("${ChatColor.GREEN}Please specify a valid reason.")
                .acceptInput { input ->
                    Tasks.async {
                        try {
                            val requestData = mapOf(
                                    "punishmentType" to PunishmentType.MUTE.name,
                                    "pardonedBy" to player.uniqueId,
                                    "pardonReason" to input,
                                    "silent" to true
                            )

                            val response = CoreXD.instance.profilesService.pardon(profile.uuid, requestData).execute()
                            if (response.isSuccessful) {
                                Tasks.sync {
                                    returnTo.openMenu(player)
                                }
                            } else {
                                player.sendMessage("${ChatColor.RED}Failed to pardon punishment: ${response.errorBody()!!.string()}")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                .start(player)
    }

}