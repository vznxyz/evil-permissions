package com.minexd.core.bukkit.presence

import com.minexd.rift.bukkit.presence.listener.PlayerSessionTracker
import com.minexd.core.presence.PlayerPresence
import com.minexd.rift.bukkit.RiftBukkitPlugin
import org.bukkit.entity.Player

interface PlayerPresenceAdapter {

    fun build(player: Player): PlayerPresence

    class DefaultAdapter : PlayerPresenceAdapter {
        override fun build(player: Player): PlayerPresence {
            return PlayerPresence(
                    state = PlayerPresence.State.ONLINE,
                    server = RiftBukkitPlugin.instance.readServerId(),
                    heartbeat = System.currentTimeMillis(),
                    session = PlayerSessionTracker.getSessionTime(player)
            )
        }
    }

}