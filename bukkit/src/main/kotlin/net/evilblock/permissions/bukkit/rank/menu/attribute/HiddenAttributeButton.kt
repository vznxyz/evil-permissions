package net.evilblock.permissions.bukkit.rank.menu.attribute

import net.evilblock.cubed.menu.Button
import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.bukkit.BukkitPlugin
import net.evilblock.permissions.bukkit.rank.event.RankUpdateEvent
import net.evilblock.permissions.rank.Rank
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView

class HiddenAttributeButton(private val rank: Rank) : Button() {

    override fun getName(player: Player): String {
        return "${ChatColor.AQUA}Hidden"
    }

    override fun getMaterial(player: Player): Material {
        return Material.INK_SACK
    }

    override fun getDamageValue(player: Player): Byte {
        return if (rank.hidden) {
            10.toByte()
        } else {
            8.toByte()
        }
    }

    override fun getDescription(player: Player): List<String> {
        val text = if (rank.hidden) {
            "${ChatColor.GRAY}Currently hidden"
        } else {
            "${ChatColor.GREEN}Currently visible"
        }

        return arrayListOf(
                text,
                "",
                "${ChatColor.GRAY}If hidden, this rank will not be",
                "${ChatColor.GRAY}displayed in the /list command",
                "${ChatColor.GRAY}and users will not be notified",
                "${ChatColor.GRAY}in chat when they receive this rank."
        )
    }

    override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
        rank.hidden = !rank.hidden

        BukkitPlugin.instance.server.scheduler.runTaskAsynchronously(BukkitPlugin.instance) {
            EvilPermissions.instance.database.saveRank(rank)
        }

        BukkitPlugin.instance.server.pluginManager.callEvent(RankUpdateEvent(rank))
    }

}