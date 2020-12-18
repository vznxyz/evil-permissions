package net.evilblock.permissions.plugin

import net.evilblock.permissions.rank.Rank

interface PluginEventHandler {

    fun callRankUpdateEvent(rank: Rank)

}