package com.minexd.core.proxy.profile

import com.minexd.core.CoreXD
import com.minexd.core.profile.ProfileHandler
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.event.LoginEvent
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

object ProfileLoadListeners : Listener {

    @EventHandler(priority = 99)
    fun onLoginEvent(event: LoginEvent) {
        if (!event.isCancelled) {
            try {
                val response = CoreXD.instance.profilesService.login(event.connection.uniqueId, mapOf("ipAddress" to event.connection.address.address.hostAddress)).execute()
                if (response.isSuccessful) {
                    val profile = response.body()!!
                    profile.cacheExpiry = null
                    ProfileHandler.cacheProfile(profile)
                } else {
                    throw IllegalStateException("Failed to load profile: ${response.errorBody()!!.string()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()

                val message = TextComponent("Failed to load your profile!\nCheck our status page to see if we're having problems!")
                message.color = net.md_5.bungee.api.ChatColor.RED
                message.isBold = true

                event.isCancelled = true
                event.setCancelReason(message)
            }
        }
    }

    @EventHandler(priority = 0)
    fun onPostLoginEvent(event: PostLoginEvent) {
        if (ProfileHandler.isProfileCached(event.player.uniqueId)) {
            ProfileHandler.getProfile(event.player.uniqueId).apply()
        }
    }

    @net.md_5.bungee.event.EventHandler(priority = 99)
    fun onPlayerDisconnectEvent(event: PlayerDisconnectEvent) {
        ProfileHandler.forgetProfile(event.player.uniqueId)
    }

}