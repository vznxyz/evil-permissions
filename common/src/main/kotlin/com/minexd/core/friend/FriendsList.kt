package com.minexd.core.friend

import com.minexd.core.friend.result.Friend
import com.minexd.core.presence.PlayerPresence
import com.minexd.core.presence.PlayerPresenceHandler
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.Comparator

class FriendsList(val owner: UUID) {

    private val friends: MutableMap<UUID, Friend> = ConcurrentHashMap()

    var cacheExpiry: Long? = null

    private var presenceLoaded: Boolean = false
    private var presence: MutableMap<UUID, PlayerPresence> = ConcurrentHashMap()

    fun getFriendsByType(type: FriendshipType): List<Friend> {
        return friends.values.filter { it.type == type }
    }

    fun getFriendships(): Collection<Friend> {
        return friends.values
    }

    fun getFriends(): List<Friend> {
        return getFriendsByType(FriendshipType.FRIENDS)
    }

    fun addFriend(friend: Friend) {
        friends[friend.uuid] = friend
    }

    fun removeFriend(uuid: UUID) {
        friends.remove(uuid)
    }

    fun removeFriend(friend: Friend) {
        friends.remove(friend.uuid)
    }

    fun getIncomingFriends(): List<Friend> {
        return getFriendsByType(FriendshipType.PENDING).filter { it.createdBy != owner }
    }

    fun getOutgoingFriends(): List<Friend> {
        return getFriendsByType(FriendshipType.PENDING).filter { it.createdBy == owner }
    }

    fun hasFriendship(player: UUID): Boolean {
        return friends.containsKey(player)
    }

    fun getFriendship(player: UUID): Friend {
        return friends[player]!!
    }

    fun isFriendsWith(player: UUID): Boolean {
        return hasFriendship(player) && getFriendship(player).type == FriendshipType.FRIENDS
    }

    fun isPresenceLoaded(): Boolean {
        return presenceLoaded
    }

    fun loadPresence() {
        val presenceResults: Map<UUID, PlayerPresence>
        try {
            presenceResults = PlayerPresenceHandler.findPresence(friends.keys)
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }

        presenceLoaded = true

        for (result in presenceResults) {
            this.presence = ConcurrentHashMap<UUID, PlayerPresence>().also { it.putAll(presenceResults) }
        }
    }

    fun hasPresence(player: UUID): Boolean {
        return presence.containsKey(player)
    }

    fun getPresence(player: UUID): PlayerPresence {
        return presence[player]!!
    }

    companion object {
        val SORT: Comparator<Friend> = Comparator { o1: Friend, o2: Friend ->
            if (o1.favorited && !o2.favorited) {
                return@Comparator 1
            } else if (!o1.favorited && o2.favorited) {
                return@Comparator -1
            }

            if (o1.type == FriendshipType.FRIENDS && o2.type == FriendshipType.FRIENDS) {
                if (o1.presence != null && o2.presence != null) {
                    return@Comparator o1.score - o2.score
                } else if ((o1.presence != null && o1.presence!!.isOnline()) && (o2.presence == null || !o2.presence!!.isOnline())) {
                    return@Comparator 1
                } else if ((o1.presence == null || !o1.presence!!.isOnline()) && (o2.presence != null && o2.presence!!.isOnline())) {
                    return@Comparator -1
                } else {
                    return@Comparator o1.score - o2.score
                }
            } else if (o1.type == FriendshipType.FRIENDS && o2.type != FriendshipType.FRIENDS) {
                return@Comparator 1
            } else if (o1.type != FriendshipType.FRIENDS && o2.type == FriendshipType.FRIENDS) {
                return@Comparator -1
            }

            return@Comparator (o1.createdAt - o2.createdAt).toInt()
        }
    }

}