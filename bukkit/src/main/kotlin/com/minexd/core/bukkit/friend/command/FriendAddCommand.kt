package com.minexd.core.bukkit.friend.command

import com.minexd.core.CoreXD
import com.minexd.core.bukkit.util.Constants
import com.minexd.core.friend.FriendshipType
import com.minexd.core.friend.cache.FriendsCache
import com.minexd.core.friend.result.Friend
import com.minexd.core.friend.result.FriendRequestResult
import net.evilblock.cubed.Cubed
import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.parameter.Param
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.util.*

object FriendAddCommand {

    @Command(
            names = ["friend add"],
            description = "Add a friend",
            async = true
    )
    @JvmStatic
    fun execute(player: Player, @Param(name = "player") friendUUID: UUID) {
        if (!FriendsCache.isLoaded(player.uniqueId)) {
            player.sendMessage(Constants.FRIENDS_NOT_LOADED)
            return
        }

        if (player.uniqueId == friendUUID) {
            player.sendMessage("${ChatColor.RED}You can't send a friend request to yourself!")
            return
        }

        val friendsList = FriendsCache.get(player.uniqueId)
        val friendName = Cubed.instance.uuidCache.name(friendUUID)

        try {
            val response = CoreXD.instance.friendsService.createFriendship(player.uniqueId, friendUUID).execute()
            if (response.isSuccessful) {
                if (response.code() == 201) {
                    val newFriendship = response.body()!!

                    if (friendsList.hasFriendship(friendUUID)) {
                        val friend = friendsList.getFriendship(friendUUID)
                        friend.username = friendName
                        friend.type = newFriendship.type
                        friend.score = newFriendship.score
                        friend.createdBy = newFriendship.player1
                        friend.createdAt = newFriendship.createdAt
                        friend.favorited = newFriendship.favorited.contains(player.uniqueId)
                    } else {
                        val friend = Friend(
                                uuid = friendUUID,
                                username = friendName,
                                type = newFriendship.type,
                                score = newFriendship.score,
                                createdBy = newFriendship.player1,
                                createdAt = newFriendship.createdAt,
                                favorited = newFriendship.favorited.contains(player.uniqueId)
                        )

                        friendsList.addFriend(friend)
                    }

                    if (newFriendship.type == FriendshipType.PENDING) {
                        player.sendMessage(buildString {
                            append(Constants.FRIENDS_PREFIX)
                            append("Your friend request was sent to ")
                            append(friendName)
                            append("!")
                        })
                    } else {
                        player.sendMessage(buildString {
                            append(Constants.FRIENDS_PREFIX)
                            append("You are now friends with ")
                            append(friendName)
                            append("!")
                        })
                    }
                } else {
                    throw Exception(response.body()?.toString() ?: "Empty body ${response.code()}")
                }
            } else {
                when (FriendRequestResult.valueOf(response.errorBody()!!.string())) {
                    FriendRequestResult.ALREADY_FRIENDS -> {
                        player.sendMessage("${ChatColor.RED}You are already friends with ${ChatColor.YELLOW}$friendName${ChatColor.RED}!")
                    }
                    FriendRequestResult.ALREADY_REQUESTED -> {
                        player.sendMessage("${ChatColor.RED}You've already sent a friend request to ${ChatColor.YELLOW}$friendName${ChatColor.RED}!")
                    }
                    FriendRequestResult.REQUEST_CREATED -> {
                        player.sendMessage("${ChatColor.GREEN}You've sent a friend request to ${ChatColor.YELLOW}${friendName}${ChatColor.GREEN}!")
                    }
                    FriendRequestResult.REQUEST_ACCEPTED -> {
                        player.sendMessage("${ChatColor.GREEN}You accepted ${ChatColor.YELLOW}$friendName${ChatColor.GREEN}'s friend request!")
                    }
                    FriendRequestResult.NOT_REQUESTED -> {
                        player.sendMessage("${ChatColor.YELLOW}$friendName ${ChatColor.RED}hasn't sent you a friend request!")
                    }
                    FriendRequestResult.CANNOT_REQUEST_SELF -> {
                        player.sendMessage("${ChatColor.RED}You can't send a friend request to yourself!")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            player.sendMessage("${ChatColor.RED}Failed to send request to API!")
        }
    }

}