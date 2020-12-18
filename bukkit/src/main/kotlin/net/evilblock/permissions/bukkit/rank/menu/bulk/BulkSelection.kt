package net.evilblock.permissions.bukkit.rank.menu.bulk

import net.evilblock.cubed.menu.Menu
import net.evilblock.permissions.bukkit.BukkitPlugin
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import java.util.*

/**
 * Usually a bulk selection is tracked via menus, but sometimes we'll need to exit a menu
 * and still have access to the selection. This utility allows a bulk selection to be
 * tracked and expired by utilizing Bukkit's metadata storage.
 */
object BulkSelection {

    val MISSING_SESSION = "${ChatColor.RED}Couldn't find your bulk selection. Maybe your session expired?"

    @JvmStatic
    fun track(player: Player, selection: Set<*>, menu: Menu? = null) {
        val bulkSelectionId = UUID.randomUUID()

        player.setMetadata("CUR_BULK_SEL_ID", FixedMetadataValue(BukkitPlugin.instance, bulkSelectionId))
        player.setMetadata("CUR_BULK_SEL", FixedMetadataValue(BukkitPlugin.instance, selection))

        if (menu != null) {
            player.setMetadata("BULK_SEL_MENU", FixedMetadataValue(BukkitPlugin.instance, menu))
        }

        BukkitPlugin.instance.server.scheduler.runTaskLater(BukkitPlugin.instance, {
            if (player.hasMetadata("CUR_BULK_SEL_ID")) {
                if (player.getMetadata("CUR_BULK_SEL_ID")[0].value() as UUID == bulkSelectionId) {
                    clear(player)
                }
            }
        }, 20L * 30)
    }

    @JvmStatic
    fun get(player: Player): Set<*>? {
        if (player.hasMetadata("CUR_BULK_SEL")) {
            return player.getMetadata("CUR_BULK_SEL")[0].value() as Set<*>
        }
        return null
    }

    @JvmStatic
    fun clear(player: Player) {
        player.removeMetadata("CUR_BULK_SEL_ID", BukkitPlugin.instance)
        player.removeMetadata("CUR_BULK_SEL", BukkitPlugin.instance)
        player.removeMetadata("BULK_SEL_MENU", BukkitPlugin.instance)
    }

    @JvmStatic
    fun reopen(player: Player) {
        if (player.hasMetadata("BULK_SEL_MENU")) {
            (player.getMetadata("BULK_SEL_MENU")[0].value() as Menu).openMenu(player)
        } else {
            player.sendMessage(MISSING_SESSION)
        }
    }

}