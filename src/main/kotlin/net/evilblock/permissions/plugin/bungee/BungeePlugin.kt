package net.evilblock.permissions.plugin.bungee

import com.google.common.io.ByteStreams
import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.plugin.PluginEventHandler
import net.evilblock.permissions.plugin.bungee.user.BungeeUser
import net.evilblock.permissions.plugin.bungee.user.BungeeUserListeners
import net.evilblock.permissions.plugin.bungee.user.BungeeUserMessageListeners
import net.evilblock.permissions.user.User
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.config.Configuration
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import java.io.File
import java.io.FileOutputStream
import java.util.*

class BungeePlugin : net.evilblock.permissions.plugin.Plugin, Plugin() {

    companion object {
        @JvmStatic lateinit var instance: BungeePlugin
    }

    private val eventHandler = BungeePluginEventHandler()
    private lateinit var configuration: Configuration
    private lateinit var jedisPool: JedisPool
    private lateinit var mongoClient: MongoClient

    override fun onEnable() {
        instance = this

        // initialize the configuration
        saveDefaultConfig()
        loadConfig()

        loadStore()
        logger.info("Loaded store")

        // initialize core
        EvilPermissions(this)

        loadListeners()
        logger.info("Loaded listeners")

        loadPidgin()
        logger.info("Loaded pidgin listeners")
    }

    private fun loadConfig() {
        configuration = ConfigurationProvider.getProvider(YamlConfiguration::class.java).load(File(dataFolder, "config.yml"))
    }

    private fun saveDefaultConfig() {
        if (!dataFolder.exists()) {
            dataFolder.mkdir()
        }

        val configFile = File(dataFolder, "config.yml")
        if (!configFile.exists()) {
            try {
                configFile.createNewFile()
                getResourceAsStream("bungee_config.yml").use { `is` -> FileOutputStream(configFile).use { os -> ByteStreams.copy(`is`, os) } }
            } catch (e: Throwable) {
                throw RuntimeException("Unable to create configuration file", e)
            }
        }
    }

    private fun loadStore() {
        val redisHost = configuration.getString("redis.host")
        val redisPort = configuration.getInt("redis.port")
        val redisPassword = configuration.getString("redis.password")
        val redisDbId = configuration.getInt("redis.dbId")

        jedisPool = JedisPool(JedisPoolConfig(), redisHost, redisPort, 5000, redisPassword, redisDbId)

        val mongoHost = configuration.getString("mongo.host")
        val mongoPort = configuration.getInt("mongo.port")
        val mongoUsername = configuration.getString("mongo.username")
        val mongoPassword = configuration.getString("mongo.password")

        mongoClient = if (mongoPassword != null && mongoPassword.isNotEmpty()) {
            val serverAddress = ServerAddress(mongoHost, mongoPort)
            val credential = MongoCredential.createCredential(mongoUsername, "admin", mongoPassword.toCharArray())

            MongoClient(serverAddress, credential, MongoClientOptions.builder().build())
        } else {
            MongoClient(mongoHost, mongoPort)
        }
    }

    private fun loadListeners() {
        proxy.pluginManager.registerListener(this, BungeeUserListeners())
    }

    private fun loadPidgin() {
        EvilPermissions.instance.pidgin.registerListener(BungeeUserMessageListeners)
    }

    override fun getEventHandler(): PluginEventHandler {
        return eventHandler
    }

    override fun getActiveGroups(): Set<String> {
        return configuration.getStringList("active-rank-groups").toSet()
    }

    override fun makeUser(uuid: UUID): User {
        return BungeeUser(uuid)
    }

    override fun getJedisPool(): JedisPool {
        return jedisPool
    }

    override fun getMongoClient(): MongoClient {
        return mongoClient
    }

}