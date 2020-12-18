package net.evilblock.permissions.bukkit

import net.evilblock.permissions.plugin.PluginEventHandler
import net.evilblock.permissions.bukkit.rank.event.RankUpdateEvent
import net.evilblock.permissions.rank.Rank
import org.bukkit.Bukkit

class BukkitPluginEventHandler : PluginEventHandler {

    override fun callRankUpdateEvent(rank: Rank) {
        Bukkit.getServer().pluginManager.callEvent(RankUpdateEvent(rank))
    }

}