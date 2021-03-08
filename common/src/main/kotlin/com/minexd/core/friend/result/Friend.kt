package com.minexd.core.friend.result

import com.minexd.core.friend.FriendshipType
import com.minexd.core.presence.PlayerPresence
import com.minexd.core.presence.PlayerPresenceHandler
import java.util.*

/**
 * Represents a friend from a friendship.
 */
data class Friend(
        val uuid: UUID,
        var username: String,
        var type: FriendshipType,
        var score: Int,
        var createdBy: UUID,
        var createdAt: Long,
        var favorited: Boolean
) {

    var presence: PlayerPresence? = null

    fun loadPresence() {
        presence = PlayerPresenceHandler.findPresence(uuid)
    }

    override fun equals(other: Any?): Boolean {
        return if (other is Friend) {
            uuid == other.uuid
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return uuid.hashCode()
    }

}