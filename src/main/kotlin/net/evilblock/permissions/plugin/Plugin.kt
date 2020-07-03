package net.evilblock.permissions.plugin

import com.mongodb.MongoClient
import net.evilblock.permissions.user.User
import redis.clients.jedis.JedisPool
import java.util.*
import java.util.logging.Logger

interface Plugin {

    fun getEventHandler(): PluginEventHandler

    fun getActiveGroups(): Set<String>

    fun getLogger(): Logger

    fun makeUser(uuid: UUID): User

    fun getJedisPool(): JedisPool

    fun getMongoClient(): MongoClient

}