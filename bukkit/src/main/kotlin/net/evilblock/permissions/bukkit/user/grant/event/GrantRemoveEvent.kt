package net.evilblock.permissions.bukkit.user.grant.event

import net.evilblock.permissions.user.grant.Grant
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class GrantRemoveEvent(val player: Player, val grant: Grant) : Event() {

    companion object {
        @JvmStatic val handlerList = HandlerList()
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }

}