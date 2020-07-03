package net.evilblock.permissions.plugin.bukkit.rank

import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.plugin.bukkit.BukkitPlugin
import net.evilblock.permissions.plugin.bukkit.rank.event.RankUpdateEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class RankListeners : Listener {

    @EventHandler
    fun onRankUpdateEvent(event: RankUpdateEvent) {
        BukkitPlugin.instance.server.scheduler.runTaskAsynchronously(BukkitPlugin.instance) {
            for (user in EvilPermissions.instance.userHandler.loadedUsers.values) {
                for (grant in user.grants) {
                    if (grant.rank.id == event.rank.id) {
                        grant.rank = event.rank
                        user.apply()
                    }
                }
            }
        }
    }

}