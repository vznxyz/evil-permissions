package com.minexd.core.bukkit.friend.listener

import com.google.gson.JsonObject
import com.minexd.core.bukkit.util.Constants
import com.minexd.core.friend.FriendshipType
import com.minexd.core.friend.cache.FriendsCache
import com.minexd.core.friend.result.Friend
import com.minexd.core.presence.PlayerPresence
import net.evilblock.pidgin.message.handler.IncomingMessageHandler
import net.evilblock.pidgin.message.listener.MessageListener
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import java.util.*

object FriendsMessageListeners : MessageListener {

    @IncomingMessageHandler("FriendRequest")
    fun onFriendRequest(data: JsonObject) {
        val receivingPlayer = Bukkit.getPlayer(UUID.fromString(data["Receiver"].asString)) ?: return

        val sender = UUID.fromString(data["Sender"].asString)
        val senderName = data["SenderName"].asString

        if (FriendsCache.isLoaded(receivingPlayer.uniqueId)) {
            val friendsList = FriendsCache.get(receivingPlayer.uniqueId)

            val friend = Friend(
                    sender,
                    ChatColor.stripColor(senderName),
                    FriendshipType.PENDING,
                    0,
                    sender,
                    System.currentTimeMillis(),
                    false
            )

            friendsList.addFriend(friend)

            receivingPlayer.sendMessage(buildString {
                append(Constants.FRIENDS_PREFIX)
                append("You have a new friend request from ")
                append(friend.username)
                append("!")
            })
        }
    }

    @IncomingMessageHandler("FriendAccept")
    fun onFriendAccept(data: JsonObject) {
        val receivingPlayer = Bukkit.getPlayer(UUID.fromString(data["Receiver"].asString)) ?: return

        val sender = UUID.fromString(data["Sender"].asString)
        val senderName = data["SenderName"].asString

        if (FriendsCache.isLoaded(receivingPlayer.uniqueId)) {
            val friendsList = FriendsCache.get(receivingPlayer.uniqueId)

            val friend: Friend
            if (friendsList.hasFriendship(sender)) {
                friend = friendsList.getFriendship(sender)
                friend.type = FriendshipType.FRIENDS
            } else {
                friend = Friend(
                        uuid = sender,
                        username = ChatColor.stripColor(senderName),
                        type = FriendshipType.PENDING,
                        score = 0,
                        createdBy = sender,
                        createdAt = System.currentTimeMillis(),
                        favorited = false
                )

                friendsList.addFriend(friend)
            }

            receivingPlayer.sendMessage(buildString {
                append(Constants.FRIENDS_PREFIX)
                append(friend.username)
                append(" accepted your friend request!")
            })
        }
    }

    @IncomingMessageHandler("FriendRemove")
    fun onFriendRemove(data: JsonObject) {
        val receiverUUID = UUID.fromString(data["Receiver"].asString)
        val senderUUID = UUID.fromString(data["Sender"].asString)

        if (FriendsCache.isLoaded(receiverUUID)) {
            val friendsList = FriendsCache.get(receiverUUID)
            friendsList.removeFriend(senderUUID)
        }
    }

    @IncomingMessageHandler("FriendStateChange")
    fun onFriendStateChange(data: JsonObject) {
        val playerUUID = UUID.fromString(data["PlayerUUID"].asString)
        val playerName = data["PlayerName"].asString
        val server = data["Server"].asString

        val newState = PlayerPresence.State.valueOf(data["State"].asString)

        for ((owner, friendsList) in FriendsCache.getLoadedLists()) {
            val player = Bukkit.getPlayer(owner) ?: continue

            if (friendsList.isFriendsWith(playerUUID)) {
                val friendship = friendsList.getFriendship(playerUUID)
                if (friendship.presence == null) {
                    friendship.presence = PlayerPresence(
                            state = newState,
                            server = server,
                            heartbeat = System.currentTimeMillis(),
                            session = 0L
                    )
                } else {
                    val presence = friendship.presence!!

                    if (presence.server != server) {
                        presence.session = 0L
                    }

                    presence.state = newState
                    presence.server = server
                    presence.heartbeat = System.currentTimeMillis()
                }

                player.sendMessage(buildString {
                    append(Constants.FRIENDS_PREFIX)
                    append(playerName)
                    append(" is now ")

                    if (newState == PlayerPresence.State.ONLINE) {
                        append("${ChatColor.GREEN}${ChatColor.BOLD}online")
                    } else {
                        append("${ChatColor.RED}${ChatColor.BOLD}offline")
                    }

                    append("${ChatColor.WHITE}!")
                })
            }
        }
    }

}