package com.minexd.core.bukkit.rank.event

import net.evilblock.cubed.plugin.PluginEvent
import com.minexd.core.rank.Rank

open class RankEvent(val rank: Rank) : PluginEvent()