package com.minexd.core.bukkit.friend.listener

import com.minexd.core.CoreXD
import com.minexd.core.friend.FriendsList
import com.minexd.core.friend.cache.FriendsCache
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerQuitEvent

object FriendsLoadListeners : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onAsyncPlayerPreLoginEvent(event: AsyncPlayerPreLoginEvent) {
        if (event.loginResult != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return
        }

        try {
            val friendsList = if (FriendsCache.isLoaded(event.uniqueId)) {
                FriendsCache.get(event.uniqueId)
            } else {
                val response  = CoreXD.instance.friendsService.getFriendships(event.uniqueId).execute()
                if (response.isSuccessful) {
                    FriendsList(event.uniqueId).also {
                        val list = response.body()!!
                        for (friend in list) {
                            it.addFriend(friend)
                        }
                    }
                } else {
                    throw IllegalStateException(response.errorBody()!!.string())
                }
            }

            friendsList.cacheExpiry = null

            if (!friendsList.isPresenceLoaded()) {
                friendsList.loadPresence()
            }

            FriendsCache.cache(friendsList)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @EventHandler
    fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        if (FriendsCache.isLoaded(event.player.uniqueId)) {
            FriendsCache.clear(FriendsCache.get(event.player.uniqueId))
        }
    }

}