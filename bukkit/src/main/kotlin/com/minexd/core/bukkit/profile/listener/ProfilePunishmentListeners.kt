package com.minexd.core.bukkit.profile.listener

import com.minexd.core.profile.ProfileHandler
import com.minexd.core.profile.punishment.PunishmentType
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

object ProfilePunishmentListeners : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    fun onAsyncPlayerChatEventLow(event: AsyncPlayerChatEvent) {
        val profile = ProfileHandler.getProfile(event.player.uniqueId)

        val activeMute = profile.getActivePunishment(PunishmentType.MUTE)
        if (activeMute != null) {
            event.isCancelled = true

            if (activeMute.isPermanent()) {
                event.player.sendMessage("${ChatColor.RED}You're permanently muted.")
            } else {
                event.player.sendMessage("${ChatColor.RED}You're muted for another ${activeMute.getFormattedRemainingTime()}.")
            }

            event.player.sendMessage("${ChatColor.RED}Reason: ${activeMute.reason}")
        }
    }

}