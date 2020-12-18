package net.evilblock.permissions.bukkit.user.grant

import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.bukkit.BukkitPlugin
import net.evilblock.permissions.bukkit.user.grant.event.GrantCreateEvent
import net.evilblock.permissions.bukkit.user.grant.event.GrantRemoveEvent
import net.evilblock.permissions.user.UserHandler
import net.evilblock.permissions.util.TimeUtils
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class GrantListeners : Listener {

    @EventHandler
    fun onGrantCreateEvent(event: GrantCreateEvent) {
        if (!event.grant.rank.hidden) {
            val period = if (event.grant.expiresAt == null) {
                "forever"
            } else {
                TimeUtils.formatIntoDetailedString(((event.grant.expiresAt!! - System.currentTimeMillis()) / 1000).toInt())
            }

            event.player.sendMessage(
                "${ChatColor.GREEN}You've been granted the ${ChatColor.translateAlternateColorCodes(
                    '&',
                    event.grant.rank.getColoredDisplayName()
                )} ${ChatColor.GREEN}rank for a period of ${ChatColor.YELLOW}$period${ChatColor.GREEN}."
            )
        }

        BukkitPlugin.instance.server.scheduler.runTaskAsynchronously(BukkitPlugin.instance) {
            UserHandler.getByUniqueId(event.player.uniqueId)?.apply()
        }
    }

    @EventHandler
    fun onGrantRemoveEvent(event: GrantRemoveEvent) {
        BukkitPlugin.instance.server.scheduler.runTaskAsynchronously(BukkitPlugin.instance) {
            UserHandler.getByUniqueId(event.player.uniqueId)?.apply()
        }
    }

}