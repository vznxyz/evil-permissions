package com.minexd.rift.bukkit.presence.listener

import com.minexd.rift.bukkit.RiftBukkitPlugin
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.metadata.FixedMetadataValue

object PlayerSessionTracker : Listener {

    @JvmStatic
    fun getSessionTime(player: Player): Long {
        return player.getMetadata("PresenceSession").firstOrNull()?.asLong() ?: 0L
    }

    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        event.player.setMetadata("PresenceSession", FixedMetadataValue(RiftBukkitPlugin.instance, System.currentTimeMillis()))
    }

    @EventHandler
    fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        event.player.removeMetadata("PresenceSession", RiftBukkitPlugin.instance)
    }

}