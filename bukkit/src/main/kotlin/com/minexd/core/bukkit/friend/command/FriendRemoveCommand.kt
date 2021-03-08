package com.minexd.core.bukkit.friend.command

import com.minexd.core.CoreXD
import com.minexd.core.bukkit.util.Constants
import com.minexd.core.friend.cache.FriendsCache
import com.minexd.core.friend.result.FriendRemoveResult
import net.evilblock.cubed.Cubed
import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.parameter.Param
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.util.*

object FriendRemoveCommand {

    @Command(
            names = ["friend remove"],
            description = "Remove a friend",
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

        val friendName = Cubed.instance.uuidCache.name(friendUUID)
        val friendsList = FriendsCache.get(player.uniqueId)

        try {
            val response = CoreXD.instance.friendsService.destroyFriendship(player.uniqueId, friendUUID).execute()
            if (response.isSuccessful) {
                if (response.code() == 200) {
                    friendsList.removeFriend(friendUUID)

                    when (response.body()!!.result) {
                        FriendRemoveResult.FRIEND_REMOVED -> {
                            player.sendMessage(buildString {
                                append(Constants.FRIENDS_PREFIX)
                                append("You've removed ")
                                append(friendName)
                                append(" from your friends list!")
                            })
                        }
                        FriendRemoveResult.REQUEST_CANCELLED -> {
                            player.sendMessage(buildString {
                                append(Constants.FRIENDS_PREFIX)
                                append("You've cancelled your friend request to ")
                                append(friendName)
                                append("!")
                            })
                        }
                        FriendRemoveResult.REQUEST_REJECTED -> {
                            player.sendMessage(buildString {
                                append(Constants.FRIENDS_PREFIX)
                                append("You've rejected ")
                                append(friendName)
                                append("'s friend request!")
                            })
                        }
                    }
                }
            } else {
                player.sendMessage("${ChatColor.RED}${response.errorBody()!!.string()}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            player.sendMessage("${ChatColor.RED}Failed to send request to API!")
        }
    }

}