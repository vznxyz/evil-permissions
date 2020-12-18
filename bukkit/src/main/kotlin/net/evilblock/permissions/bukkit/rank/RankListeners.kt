package net.evilblock.permissions.bukkit.rank

import net.evilblock.permissions.bukkit.BukkitPlugin
import net.evilblock.permissions.bukkit.rank.event.RankUpdateEvent
import net.evilblock.permissions.user.UserHandler
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class RankListeners : Listener {

    @EventHandler
    fun onRankUpdateEvent(event: RankUpdateEvent) {
        BukkitPlugin.instance.server.scheduler.runTaskAsynchronously(BukkitPlugin.instance) {
            for (user in UserHandler.loadedUsers.values) {
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