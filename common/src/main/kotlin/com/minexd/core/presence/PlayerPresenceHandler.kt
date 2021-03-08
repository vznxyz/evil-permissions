package com.minexd.core.presence

import com.minexd.core.CoreXD
import redis.clients.jedis.Jedis
import redis.clients.jedis.Response
import java.util.*

object PlayerPresenceHandler {

    @JvmStatic
    fun findPresence(uuid: UUID, redis: Jedis): PlayerPresence? {
        return if (redis.exists("Core:Presence:$uuid")) {
            PlayerPresence(redis.hgetAll("Core:Presence:$uuid"))
        } else {
            null
        }
    }

    @JvmStatic
    fun findPresence(uuid: UUID): PlayerPresence? {
        return CoreXD.instance.plugin.getRedis().runRedisCommand { redis ->
            findPresence(uuid, redis)
        }
    }

    @JvmStatic
    fun findPresence(set: Set<UUID>): Map<UUID, PlayerPresence> {
        return CoreXD.instance.plugin.getRedis().runRedisCommand { redis ->
            val presenceResults = hashMapOf<UUID, PlayerPresence>()

            redis.pipelined().use { pipeline ->
                val existsResults = arrayListOf<Pair<UUID, Response<Boolean>>>().also { list ->
                    for (uuid in set) {
                        list.add(Pair(uuid, pipeline.exists("Core:Presence:$uuid")))
                    }
                }

                pipeline.sync()

                // collect the uuids that do have a presence
                val hasPresence = existsResults.filter { it.second.get() }.map { it.first }

                val dataResults = arrayListOf<Pair<UUID, Response<Map<String, String>>>>().also { list ->
                    for (uuid in hasPresence) {
                        list.add(Pair(uuid, pipeline.hgetAll("Core:Presence:$uuid")))
                    }
                }

                pipeline.sync()

                for (pair in dataResults) {
                    val response = pair.second.get()
                    presenceResults[pair.first] = PlayerPresence(response)
                }
            }

            return@runRedisCommand presenceResults
        }
    }

    @JvmStatic
    fun updatePresence(uuid: UUID, presence: PlayerPresence) {
        CoreXD.instance.plugin.getRedis().runRedisCommand { redis ->
            redis.hmset("Core:Presence:$uuid", presence.toMap())
        }
    }

}