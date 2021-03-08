package com.minexd.core.bukkit

import com.minexd.core.plugin.PluginEventHandler
import com.minexd.core.bukkit.rank.event.RankUpdateEvent
import com.minexd.core.rank.Rank
import org.bukkit.Bukkit

class BukkitPluginEventHandler : PluginEventHandler {

    override fun callRankUpdateEvent(rank: Rank) {
        Bukkit.getServer().pluginManager.callEvent(RankUpdateEvent(rank))
    }

}