package com.minexd.core.bukkit.presence

import com.minexd.core.CoreXD
import com.minexd.core.bukkit.BukkitPlugin
import com.minexd.core.friend.cache.FriendsCache
import org.bukkit.Bukkit

object PlayerPresenceTask : Runnable {

    override fun run() {
        CoreXD.instance.plugin.getRedis().runRedisCommand { redis ->
            val pipeline = redis.pipelined()

            for (player in Bukkit.getOnlinePlayers()) {
                val presence = BukkitPlugin.instance.presenceAdapter.build(player)

                // update presence instantly for friends on the same server
                for (otherPlayer in Bukkit.getOnlinePlayers()) {
                    if (player == otherPlayer) {
                        continue
                    }

                    if (FriendsCache.isLoaded(otherPlayer.uniqueId)) {
                        val friendsList = FriendsCache.get(otherPlayer.uniqueId)
                        if (friendsList.hasFriendship(player.uniqueId)) {
                            val friendship = friendsList.getFriendship(player.uniqueId)
                            friendship.presence = presence
                        }
                    }
                }

                pipeline.hmset("Core:Presence:${player.uniqueId}", presence.toMap())
            }

            pipeline.sync()
        }
    }

}