package com.minexd.core.bukkit.profile.listener

import com.minexd.core.bukkit.BukkitPlugin
import com.minexd.core.profile.ProfileHandler
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

object ProfileChatListeners : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    fun onAsyncPlayerChatEventLow(event: AsyncPlayerChatEvent) {
        if (!ProfileHandler.isProfileCached(event.player.uniqueId)) {
            event.isCancelled = true
            event.player.sendMessage("${ChatColor.RED}You can't send chat messages because your profile isn't loaded!")
            return
        }

        if (BukkitPlugin.instance.config.getBoolean("format-chat")) {
            val profile = ProfileHandler.getProfile(event.player.uniqueId)
            val activeRank = profile.getBestDisplayRank()

            val format = activeRank.processPlaceholders(BukkitPlugin.instance.config.getString("chat-format"))
            event.format = ChatColor.translateAlternateColorCodes('&', format)
        }
    }

}