package com.minexd.core.plugin

import com.minexd.core.rank.Rank

interface PluginEventHandler {

    fun callRankUpdateEvent(rank: Rank)

}