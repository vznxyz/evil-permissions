package com.minexd.core.bukkit.profile.listener

import com.minexd.core.bukkit.profile.grant.event.GrantCreateEvent
import com.minexd.core.util.TimeUtils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object ProfileGrantListeners : Listener {

    @EventHandler
    fun onGrantCreateEvent(event: GrantCreateEvent) {
        val player = Bukkit.getPlayer(event.profile.uuid) ?: return

        if (!event.grant.rank.hidden) {
            val period = if (event.grant.expiresAt == null) {
                "forever"
            } else {
                TimeUtils.formatIntoDetailedString(((event.grant.expiresAt!! - System.currentTimeMillis()) / 1000).toInt())
            }

            player.sendMessage(buildString {
                append("${ChatColor.GREEN}You've been granted the ")
                append(event.grant.rank.getColoredDisplayName())
                append(" ${ChatColor.GREEN}rank for a period of ")
                append("${ChatColor.YELLOW}$period")
                append("${ChatColor.GREEN}!")
            })
        }
    }

}