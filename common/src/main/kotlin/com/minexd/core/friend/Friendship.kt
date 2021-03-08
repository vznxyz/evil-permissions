package com.minexd.core.friend

import java.util.*

/**
 * The model for a friendship between 2 players. Specially crafted
 * to meet the needs of both OOP and efficient MongoDB queries,
 * which is important because we use GSON reflection serializer
 * to convert this object into a BSON document.
 *
 * The core assumption of this model is:
 * player1 is ALWAYS the player to initiate the friendship ("friend request")
 */
data class Friendship(
    val player1: UUID,
    val player2: UUID
) {

    /**
     * A unique ID for this friendship.
     */
    val friendshipId: UUID = UUID.randomUUID()

    /**
     * When this friendship was created.
     */
    var createdAt: Long = System.currentTimeMillis()

    /**
     * The type of friendship between the two participants.
     */
    var type: FriendshipType = FriendshipType.PENDING

    /**
     * This friendship's score, otherwise known as "Friend Score".
     */
    var score: Int = 0

    /**
     * Who involved in this friendship has favorited this friendship on their "friend's list".
     */
    val favorited: MutableList<UUID> = arrayListOf()

    /**
     * If the given [source] is involved in this friendship, the counterpart player UUID is returned, otherwise null.
     */
    fun getOtherPlayer(source: UUID): UUID? {
        return when (source) {
            player1 -> player2
            player2 -> player1
            else -> null
        }
    }

}