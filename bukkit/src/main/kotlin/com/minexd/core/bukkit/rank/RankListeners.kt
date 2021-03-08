package com.minexd.core.bukkit.rank

import net.evilblock.cubed.util.bukkit.Tasks
import com.minexd.core.bukkit.rank.event.RankUpdateEvent
import com.minexd.core.profile.ProfileHandler
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object RankListeners : Listener {

    @EventHandler
    fun onRankUpdateEvent(event: RankUpdateEvent) {
        Tasks.async {
            for (user in ProfileHandler.getCachedProfiles()) {
                try {
                    grants@
                    for (grant in user.grants) {
                        if (grant.rank.id == event.rank.id) {
                            grant.rank = event.rank
                            user.apply()
                            break@grants
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

}