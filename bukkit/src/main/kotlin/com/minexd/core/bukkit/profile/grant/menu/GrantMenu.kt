package com.minexd.core.bukkit.profile.grant.menu

import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.bukkit.enchantment.GlowEnchantment
import net.evilblock.cubed.util.bukkit.prompt.DurationPrompt
import net.evilblock.cubed.util.bukkit.prompt.InputPrompt
import com.minexd.core.CoreXD
import com.minexd.core.rank.Rank
import com.minexd.core.bukkit.util.ColorMap
import com.minexd.core.profile.Profile
import com.minexd.core.rank.RankHandler
import com.minexd.core.profile.grant.Grant
import com.minexd.core.util.Permissions
import com.minexd.core.util.TimeUtils
import net.evilblock.cubed.util.time.Duration
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack

class GrantMenu(val profile: Profile) : Menu("Grant ${profile.getColoredUsername()}") {

    override fun getButtons(player: Player): Map<Int, Button> {
        val buttons = hashMapOf<Int, Button>()

        for (rank in RankHandler.getRanks().filter { it.isActiveOnServer() }.sortedBy { it.displayOrder }) {
            if (rank.default) continue

            if (canBeGranted(player, rank)) {
                buttons[buttons.size] = GrantRankButton(rank, profile)
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

    private inner class GrantRankButton(private val rank: Rank, private val profile: Profile) : Button() {
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
            return arrayListOf<String>().also { desc ->
                desc.add("")
                desc.add("${ChatColor.YELLOW}${ChatColor.BOLD}Rank Groups")

                if (rank.groups.isEmpty()) {
                    desc.add("${ChatColor.WHITE}None assigned")
                } else {
                    for (group in rank.groups) {
                        desc.add("${ChatColor.WHITE}$group")
                    }
                }

                desc.add("")
                desc.add(styleAction(ChatColor.GREEN, "LEFT-CLICK", "to grant rank"))
            }
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
            for (grant in profile.grants) {
                if (grant.isActive() && grant.rank == rank) {
                    player.sendMessage("${ChatColor.RED}That player has already been assigned that rank.")
                    return
                }
            }

            InputPrompt()
                    .withText("${ChatColor.GREEN}Please specify a valid reason.")
                    .acceptInput { reason ->
                        Tasks.delayed(1L) {
                            DurationPrompt { duration ->
                                Tasks.async {
                                    addGrant(rank, duration, reason, player)
                                }
                            }.start(player)
                        }
                    }
                    .start(player)
        }
    }

    private fun addGrant(rank: Rank, duration: Duration, reason: String, issuer: Player) {
        val grant = Grant(
                rank = rank,
                issuedBy = issuer.uniqueId,
                reason = reason,
                expiresAt = if (duration.isPermanent()) null else (System.currentTimeMillis() + duration.get())
        )

        try {
            val response = CoreXD.instance.profilesService.grant(profile.uuid, grant).execute()
            if (response.isSuccessful) {
                val period = if (grant.expiresAt == null) {
                    "forever"
                } else {
                    TimeUtils.formatIntoDetailedString(((grant.expiresAt!! - System.currentTimeMillis()) / 1000).toInt())
                }

                issuer.sendMessage(buildString {
                    append("${ChatColor.GREEN}You've granted ")
                    append(profile.getColoredUsername())
                    append(" ${ChatColor.GREEN}the ")
                    append(grant.rank.getColoredDisplayName())
                    append(" ${ChatColor.GREEN}rank for a period of ")
                    append("${ChatColor.AQUA}$period")
                    append("${ChatColor.GREEN}!")
                })

                Tasks.sync {
                    openMenu(issuer)
                }
            } else {
                issuer.sendMessage("${ChatColor.RED}${response.errorBody()!!.string()}!")
            }
        } catch (e: Exception) {
            issuer.sendMessage("${ChatColor.RED}Failed to send request to API!")
            e.printStackTrace()
            return
        }
    }

}
