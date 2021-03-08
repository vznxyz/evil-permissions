package com.minexd.core.bukkit

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.evilblock.cubed.Cubed
import net.evilblock.cubed.command.CommandHandler
import net.evilblock.cubed.store.redis.Redis
import net.evilblock.cubed.util.bukkit.Tasks
import com.minexd.core.CoreXD
import com.minexd.core.plugin.Plugin
import com.minexd.core.plugin.PluginEventHandler
import com.minexd.core.bukkit.command.ListCommand
import com.minexd.core.bukkit.command.ReloadCommand
import com.minexd.core.bukkit.friend.command.FriendAcceptCommand
import com.minexd.core.bukkit.friend.command.FriendAddCommand
import com.minexd.core.bukkit.friend.command.FriendRemoveCommand
import com.minexd.core.bukkit.friend.command.FriendsCommand
import com.minexd.core.bukkit.friend.listener.FriendsLoadListeners
import com.minexd.core.bukkit.friend.listener.FriendsMessageListeners
import com.minexd.core.bukkit.hook.CorePlaceholderExpansion
import com.minexd.core.bukkit.hook.MVdWPlaceholderHook
import com.minexd.core.bukkit.rank.RankCommands
import com.minexd.core.bukkit.rank.RankListeners
import com.minexd.core.bukkit.rank.RankParameterType
import com.minexd.core.bukkit.profile.BukkitProfile
import com.minexd.core.bukkit.profile.listener.ProfileMessageListeners
import com.minexd.core.bukkit.profile.command.AltsCommand
import com.minexd.core.bukkit.profile.command.parameter.ProfileParameterType
import com.minexd.core.bukkit.profile.grant.command.GrantCommands
import com.minexd.core.bukkit.profile.grant.command.CheckGrantsCommand
import com.minexd.core.bukkit.profile.listener.ProfileChatListeners
import com.minexd.core.bukkit.profile.listener.ProfileGrantListeners
import com.minexd.core.bukkit.profile.listener.ProfileLoadListeners
import com.minexd.core.bukkit.profile.listener.ProfilePunishmentListeners
import com.minexd.core.bukkit.profile.task.ProfileCacheExpiryTask
import com.minexd.core.profile.Profile
import com.minexd.core.rank.Rank
import com.minexd.core.profile.ProfileHandler
import com.minexd.core.profile.ProfileSerializer
import com.minexd.core.bukkit.profile.punishment.command.ban.BanCommand
import com.minexd.core.bukkit.profile.punishment.command.ban.TempBanCommand
import com.minexd.core.bukkit.profile.punishment.command.ban.UnbanCommand
import com.minexd.core.bukkit.profile.punishment.command.blacklist.BlacklistCommand
import com.minexd.core.bukkit.profile.punishment.command.blacklist.UnblacklistCommand
import com.minexd.core.bukkit.profile.punishment.command.history.CheckCommand
import com.minexd.core.bukkit.profile.punishment.command.history.StaffHistoryCommand
import com.minexd.core.bukkit.profile.punishment.command.mute.MuteCommand
import com.minexd.core.bukkit.profile.punishment.command.mute.TempMuteCommand
import com.minexd.core.bukkit.profile.punishment.command.mute.UnmuteCommand
import com.minexd.punishments.user.punishment.command.warn.WarnCommand
import com.minexd.core.bukkit.presence.PlayerPresenceTask
import com.minexd.core.bukkit.presence.PlayerPresenceAdapter
import com.minexd.core.bukkit.presence.command.PresenceFindCommand
import com.minexd.core.bukkit.profile.command.ProfileCommand
import com.minexd.rift.bukkit.presence.listener.PlayerSessionTracker
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.store.uuidcache.UUIDCache
import org.bukkit.plugin.java.JavaPlugin
import java.lang.reflect.Type
import java.util.*

class BukkitPlugin : Plugin, JavaPlugin() {

    companion object {
        @JvmStatic lateinit var instance: BukkitPlugin
    }

    private val eventHandler = BukkitPluginEventHandler()
    var presenceAdapter: PlayerPresenceAdapter = PlayerPresenceAdapter.DefaultAdapter()

    override fun onEnable() {
        instance = this

        saveDefaultConfig()

        Serializers.useGsonBuilderThenRebuild { builder ->
            builder.registerTypeAdapter(Profile::class.java, ProfileSerializer)
        }

        CoreXD(this).initialLoad()
        CoreXD.instance.pidgin.registerListener(ProfileMessageListeners)
        CoreXD.instance.pidgin.registerListener(FriendsMessageListeners)

        loadCommands()
        loadListeners()
        loadTasks()

        if (server.pluginManager.getPlugin("PlaceholderAPI") != null) {
            CorePlaceholderExpansion().register()
            logger.info("Hooked into PlaceholderAPI")
        }

        if (server.pluginManager.getPlugin("MVdWPlaceholderAPI") != null) {
            MVdWPlaceholderHook.hook()
            logger.info("Hooked into MVdWPlaceholderAPI")
        }
    }

    private fun loadCommands() {
        CommandHandler.registerParameterType(Profile::class.java, ProfileParameterType())
        CommandHandler.registerParameterType(Rank::class.java, RankParameterType())
        CommandHandler.registerClass(FriendsCommand.javaClass)
        CommandHandler.registerClass(FriendAddCommand.javaClass)
        CommandHandler.registerClass(FriendRemoveCommand.javaClass)
        CommandHandler.registerClass(FriendAcceptCommand.javaClass)
        CommandHandler.registerClass(ProfileCommand.javaClass)
        CommandHandler.registerClass(RankCommands.javaClass)
        CommandHandler.registerClass(ListCommand.javaClass)
        CommandHandler.registerClass(ReloadCommand.javaClass)
        CommandHandler.registerClass(GrantCommands.javaClass)
        CommandHandler.registerClass(CheckGrantsCommand.javaClass)
        CommandHandler.registerClass(BlacklistCommand.javaClass)
        CommandHandler.registerClass(UnblacklistCommand.javaClass)
        CommandHandler.registerClass(BanCommand.javaClass)
        CommandHandler.registerClass(TempBanCommand.javaClass)
        CommandHandler.registerClass(UnbanCommand.javaClass)
        CommandHandler.registerClass(MuteCommand.javaClass)
        CommandHandler.registerClass(TempMuteCommand.javaClass)
        CommandHandler.registerClass(UnmuteCommand.javaClass)
        CommandHandler.registerClass(WarnCommand.javaClass)
        CommandHandler.registerClass(AltsCommand.javaClass)
        CommandHandler.registerClass(CheckCommand.javaClass)
        CommandHandler.registerClass(StaffHistoryCommand.javaClass)
        CommandHandler.registerClass(PresenceFindCommand.javaClass)
    }

    private fun loadListeners() {
        server.pluginManager.registerEvents(ProfileLoadListeners, this)
        server.pluginManager.registerEvents(ProfileChatListeners, this)
        server.pluginManager.registerEvents(ProfileGrantListeners, this)
        server.pluginManager.registerEvents(ProfilePunishmentListeners, this)
        server.pluginManager.registerEvents(RankListeners, this)
        server.pluginManager.registerEvents(FriendsLoadListeners, this)
        server.pluginManager.registerEvents(PlayerSessionTracker, this)
    }

    private fun loadTasks() {
        Tasks.asyncTimer(20L, 20L) {
            for (user in ProfileHandler.getCachedProfiles()) {
                try {
                    if (user.requiresApply) {
                        user.requiresApply = false
                        user.apply()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        Tasks.asyncTimer(ProfileCacheExpiryTask, 20L, 20L)
        Tasks.asyncTimer(PlayerPresenceTask, 20L * 5, 20L * 5)
    }

    override fun getEventHandler(): PluginEventHandler {
        return eventHandler
    }

    override fun getActiveGroups(): Set<String> {
        return config.getStringList("active-rank-groups").toSet()
    }

    override fun createProfileInstance(uuid: UUID): Profile {
        return BukkitProfile(uuid)
    }

    override fun getProfileType(): Type {
        return object : TypeToken<BukkitProfile>() {}.type
    }

    override fun getAPIUrl(): String {
        return config.getString("api-url")
    }

    override fun getAPIKey(): String {
        return config.getString("api-key")
    }

    override fun getRedis(): Redis {
        return Cubed.instance.redis
    }

    override fun getUUIDCache(): UUIDCache {
        return Cubed.instance.uuidCache
    }

    override fun getGSON(): Gson {
        return Serializers.gson
    }

}