package com.minexd.core.friend.cache

import com.minexd.core.friend.FriendsList
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object FriendsCache : Runnable {

    private val cache: MutableMap<UUID, FriendsList> = ConcurrentHashMap()

    fun getLoadedLists(): Map<UUID, FriendsList> {
        return cache
    }

    fun isLoaded(uuid: UUID): Boolean {
        return cache.containsKey(uuid)
    }

    fun get(uuid: UUID): FriendsList {
        return cache[uuid]!!
    }

    fun cache(friendsList: FriendsList) {
        cache[friendsList.owner] = friendsList
    }

    fun clear(friendsList: FriendsList) {
        cache.remove(friendsList.owner)
    }

    override fun run() {
        val expiredEntries = arrayListOf<FriendsList>()

        for (entry in cache.values) {
            if (entry.cacheExpiry != null) {
                if (System.currentTimeMillis() >= entry.cacheExpiry!!) {
                    expiredEntries.add(entry)
                }
            }
        }

        if (expiredEntries.isNotEmpty()) {
            for (entry in expiredEntries) {
                clear(entry)
            }
        }
    }

}