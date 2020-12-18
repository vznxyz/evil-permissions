package net.evilblock.permissions.bukkit.rank.event

import net.evilblock.permissions.rank.Rank
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

open class RankEvent(val rank: Rank) : Event() {

    companion object {
        @JvmStatic val handlerList = HandlerList()
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }

}