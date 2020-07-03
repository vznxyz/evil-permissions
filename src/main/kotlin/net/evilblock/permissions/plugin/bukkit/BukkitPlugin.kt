package net.evilblock.permissions.plugin.bukkit

import com.mongodb.MongoClient
import net.evilblock.cubed.Cubed
import net.evilblock.cubed.CubedOptions
import net.evilblock.cubed.command.CommandHandler
import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.plugin.Plugin
import net.evilblock.permissions.plugin.PluginEventHandler
import net.evilblock.permissions.plugin.bukkit.command.ListCommand
import net.evilblock.permissions.plugin.bukkit.command.ReloadCommand
import net.evilblock.permissions.plugin.bukkit.user.command.UserResetCommand
import net.evilblock.permissions.plugin.bukkit.hook.MVdWPlaceholderHook
import net.evilblock.permissions.plugin.bukkit.hook.PermissionProvider
import net.evilblock.permissions.plugin.bukkit.hook.EvilPlaceholderExtension
import net.evilblock.permissions.plugin.bukkit.rank.RankCommands
import net.evilblock.permissions.plugin.bukkit.rank.RankListeners
import net.evilblock.permissions.plugin.bukkit.rank.RankParameterType
import net.evilblock.permissions.plugin.bukkit.user.*
import net.evilblock.permissions.plugin.bukkit.user.command.parameter.UserParameterType
import net.evilblock.permissions.plugin.bukkit.user.grant.GrantCommands
import net.evilblock.permissions.plugin.bukkit.user.grant.GrantListeners
import net.evilblock.permissions.plugin.bukkit.user.listener.BukkitUserListeners
import net.evilblock.permissions.plugin.bukkit.user.listener.BukkitUserMessageListeners
import net.evilblock.permissions.plugin.bukkit.user.task.UserApplyTask
import net.evilblock.permissions.plugin.bukkit.user.task.UserSaveTask
import net.evilblock.permissions.rank.Rank
import net.evilblock.permissions.user.User
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.plugin.java.JavaPlugin
import redis.clients.jedis.JedisPool
import java.util.*

class BukkitPlugin : Plugin, JavaPlugin() {

    companion object {
        @JvmStatic
        lateinit var instance: BukkitPlugin
    }

    private val eventHandler = BukkitPluginEventHandler()

    override fun onEnable() {
        instance = this

        // save default config before initializing core module
        saveDefaultConfig()

        // configure cubed options
        Cubed.instance.configureOptions(CubedOptions(requireRedis = true, requireMongo = true))

        // initialize core now that we can read from config
        EvilPermissions(this)

        loadCommands()
        logger.info("Loaded commands")

        loadListeners()
        logger.info("Loaded listeners")

        loadTasks()
        logger.info("Loaded tasks")

        loadPidgin()
        logger.info("Loaded pidgin listeners")

        PermissionProvider.hook()
        logger.info("Hooked into Vault")

        if (server.pluginManager.getPlugin("PlaceholderAPI") != null) {
            EvilPlaceholderExtension().register()
            logger.info("Hooked into PlaceholderAPI")
        }

        if (server.pluginManager.getPlugin("MVdWPlaceholderAPI") != null) {
            MVdWPlaceholderHook.hook()
            logger.info("Hooked into MVdWPlaceholderAPI")
        }

        loadLoggedInUsers()
    }

    private fun loadCommands() {
        CommandHandler.registerClass(RankCommands::class.java)
        CommandHandler.registerClass(ListCommand::class.java)
        CommandHandler.registerClass(ReloadCommand::class.java)
        CommandHandler.registerClass(UserResetCommand::class.java)
        CommandHandler.registerClass(GrantCommands::class.java)
        CommandHandler.registerParameterType(User::class.java, UserParameterType)
        CommandHandler.registerParameterType(Rank::class.java, RankParameterType)
    }

    private fun loadListeners() {
        server.pluginManager.registerEvents(BukkitUserListeners(), this)
        server.pluginManager.registerEvents(GrantListeners(), this)
        server.pluginManager.registerEvents(RankListeners(), this)
    }

    private fun loadTasks() {
        server.scheduler.runTaskTimerAsynchronously(this, UserApplyTask(), 20L, 20L)
        server.scheduler.runTaskTimerAsynchronously(this, UserSaveTask(), 20L * 15, 20L * 15)
    }

    private fun loadPidgin() {
        EvilPermissions.instance.pidgin.registerListener(BukkitUserMessageListeners)
    }

    /**
     * Loads all users logged into the server.
     */
    private fun loadLoggedInUsers() {
        for (player in Bukkit.getOnlinePlayers()) {
            val user = EvilPermissions.instance.database.fetchUser(player.uniqueId)
            if (user == null) {
                player.kickPlayer("${ChatColor.RED}${ChatColor.BOLD}Failed to load your user data.\n${ChatColor.RED}${ChatColor.BOLD}Please try reconnecting again later.")
                continue
            }

            EvilPermissions.instance.userHandler.loadedUsers[player.uniqueId] = user
        }
    }

    override fun getEventHandler(): PluginEventHandler {
        return eventHandler
    }

    override fun getActiveGroups(): Set<String> {
        return config.getStringList("active-rank-groups").toSet()
    }

    override fun makeUser(uuid: UUID): User {
        return BukkitUser(uuid)
    }

    override fun getJedisPool(): JedisPool {
        return Cubed.instance.redis.jedisPool!!
    }

    override fun getMongoClient(): MongoClient {
        return Cubed.instance.mongo.client
    }

}