package net.evilblock.permissions.bukkit.rank.menu.attribute

import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.util.TextSplitter
import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.bukkit.BukkitPlugin
import net.evilblock.permissions.bukkit.rank.event.RankUpdateEvent
import net.evilblock.permissions.rank.Rank
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView

class StaffAttributeButton(private val rank: Rank) : Button() {

    override fun getMaterial(player: Player): Material {
        return Material.INK_SACK
    }

    override fun getDamageValue(player: Player): Byte {
        return if (rank.default) {
            10.toByte()
        } else {
            8.toByte()
        }
    }

    override fun getName(player: Player): String {
        return "${ChatColor.AQUA}Staff Rank"
    }

    override fun getDescription(player: Player): List<String> {
        val text = if (rank.default) {
            "${ChatColor.GREEN}Currently default"
        } else {
            "${ChatColor.GRAY}Currently not default"
        }

        return arrayListOf<String>().also {
            it.add(text)
            it.add("")
            it.addAll(TextSplitter.split(linePrefix = ChatColor.GRAY.toString(), text = "If enabled, this rank will be considered a rank of staff privilege."))
        }
    }

    override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
        rank.staff = !rank.staff

        BukkitPlugin.instance.server.scheduler.runTaskAsynchronously(BukkitPlugin.instance) {
            EvilPermissions.instance.database.saveRank(rank)
        }

        BukkitPlugin.instance.server.pluginManager.callEvent(RankUpdateEvent(rank))
    }

}