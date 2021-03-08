package com.minexd.core.proxy.friend

import com.minexd.core.CoreXD
import com.minexd.core.presence.PlayerPresence
import com.minexd.core.profile.ProfileHandler
import net.evilblock.pidgin.message.Message
import net.md_5.bungee.api.connection.Server
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.ServerConnectEvent
import net.md_5.bungee.api.event.ServerSwitchEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import java.util.*

object FriendsListeners : Listener {

    private val previousServer: MutableMap<UUID, Server?> = hashMapOf()
    private val currentServer: MutableMap<UUID, Server?> = hashMapOf()

    @EventHandler(priority = 0)
    fun onPlayerDisconnectEvent(event: PlayerDisconnectEvent) {
        previousServer.remove(event.player.uniqueId)

        val from = currentServer.remove(event.player.uniqueId)
        if (from != null) {
            val profile = ProfileHandler.getProfile(event.player.uniqueId)

            val data = mapOf(
                    "PlayerUUID" to event.player.uniqueId.toString(),
                    "PlayerName" to event.player.name,
                    "ColoredName" to profile.getColoredUsername(),
                    "State" to PlayerPresence.State.OFFLINE,
                    "Server" to from.info.name
            )

            CoreXD.instance.pidgin.sendMessage(Message("FriendStateChange", data))
        }
    }

    @EventHandler
    fun onServerSwitchEvent(event: ServerSwitchEvent) {
        currentServer[event.player.uniqueId] = event.player.server

        val previousServer = previousServer[event.player.uniqueId]
        if (previousServer == null) {
            val profile = ProfileHandler.getProfile(event.player.uniqueId)

            val data = hashMapOf(
                    "PlayerUUID" to event.player.uniqueId.toString(),
                    "PlayerName" to event.player.name,
                    "ColoredName" to profile.getColoredUsername(),
                    "State" to PlayerPresence.State.ONLINE,
                    "Server" to event.player.server.info.name
            )

            CoreXD.instance.pidgin.sendMessage(Message("FriendStateChange", data))
        }
//        else {
//            val data = hashMapOf(
//                "PlayerName" to event.player.name,
//                "ColoredName" to profile.getColoredUsername(),
//                "Action" to StaffAction.SWITCH_SERVER.name,
//                "OriginServer" to previousServer.info.name,
//                "TargetServer" to event.player.server.info.name
//            )
//
//            CoreXD.instance.pidgin.sendMessage(Message("StaffAction", data))
//        }
    }

    @EventHandler
    fun onServerConnectEvent(event: ServerConnectEvent) {
        previousServer[event.player.uniqueId] = event.player.server
    }

}