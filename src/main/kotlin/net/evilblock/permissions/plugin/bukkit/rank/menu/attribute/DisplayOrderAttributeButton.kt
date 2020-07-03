package net.evilblock.permissions.plugin.bukkit.rank.menu.attribute

import net.evilblock.cubed.menu.Button
import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.plugin.bukkit.BukkitPlugin
import net.evilblock.permissions.plugin.bukkit.rank.event.RankUpdateEvent
import net.evilblock.permissions.rank.Rank
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView

class DisplayOrderAttributeButton(private val rank: Rank) : Button() {

    override fun getMaterial(player: Player): Material {
        return Material.LEVER
    }

    override fun getName(player: Player): String {
        return "${ChatColor.AQUA}Display Order: ${rank.displayOrder}"
    }

    override fun getDescription(player: Player): List<String> {
        return arrayListOf(
                "",
                "${ChatColor.GREEN}${ChatColor.BOLD}CLICK to increment order by 1",
                "${ChatColor.RED}${ChatColor.BOLD}RIGHT-CLICK to decrement order by 1",
                "",
                "${ChatColor.GREEN}${ChatColor.BOLD}SHIFT-CLICK to increment order by 10",
                "${ChatColor.RED}${ChatColor.BOLD}SHIFT-RIGHT-CLICK to decrement order by 10"
        )
    }

    override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
        if (clickType.isShiftClick) {
            if (clickType.isLeftClick) {
                rank.displayOrder = (rank.displayOrder + 10).coerceAtMost(999)
            } else if (clickType.isRightClick) {
                rank.displayOrder = (rank.displayOrder - 10).coerceAtLeast(0)
            }
        } else {
            if (clickType.isLeftClick) {
                rank.displayOrder = (rank.displayOrder + 1).coerceAtMost(999)
            } else if (clickType.isRightClick) {
                rank.displayOrder = (rank.displayOrder - 1).coerceAtLeast(0)
            }
        }

        if (clickType.isLeftClick || clickType.isRightClick) {
            BukkitPlugin.instance.server.scheduler.runTaskAsynchronously(BukkitPlugin.instance) {
                EvilPermissions.instance.database.saveRank(rank)
            }

            BukkitPlugin.instance.server.pluginManager.callEvent(RankUpdateEvent(rank))
        }
    }

}