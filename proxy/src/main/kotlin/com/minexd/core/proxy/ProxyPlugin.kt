package com.minexd.core.proxy

import com.google.common.io.ByteStreams
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.evilblock.cubed.Cubed
import net.evilblock.cubed.store.redis.Redis
import com.minexd.core.CoreXD
import com.minexd.core.plugin.PluginEventHandler
import com.minexd.core.profile.Profile
import com.minexd.core.profile.ProfileSerializer
import com.minexd.core.proxy.friend.FriendsListeners
import com.minexd.core.proxy.profile.ProxyProfile
import com.minexd.core.proxy.profile.ProfileLoadListeners
import com.minexd.core.proxy.profile.ProfileMessageListeners
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.store.uuidcache.UUIDCache
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.config.Configuration
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import java.io.File
import java.io.FileOutputStream
import java.lang.reflect.Type
import java.util.*

class ProxyPlugin : com.minexd.core.plugin.Plugin, Plugin() {

    companion object {
        @JvmStatic
        lateinit var instance: ProxyPlugin
    }

    private val eventHandler = ProxyPluginEventHandler()
    private lateinit var configuration: Configuration

    override fun onLoad() {
        instance = this
    }

    override fun onEnable() {
        saveDefaultConfig()
        loadConfig()

        Serializers.useGsonBuilderThenRebuild { builder ->
            builder.registerTypeAdapter(Profile::class.java, ProfileSerializer)
        }

        CoreXD(this).initialLoad()
        CoreXD.instance.pidgin.registerListener(ProfileMessageListeners)

        proxy.pluginManager.registerListener(this, ProfileLoadListeners)
        proxy.pluginManager.registerListener(this, FriendsListeners)
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

                getResourceAsStream("bungee_config.yml").use { input ->
                    FileOutputStream(configFile).use { output ->
                        ByteStreams.copy(input, output)
                    }
                }
            } catch (e: Throwable) {
                throw RuntimeException("Unable to create configuration file", e)
            }
        }
    }

    override fun getEventHandler(): PluginEventHandler {
        return eventHandler
    }

    override fun getActiveGroups(): Set<String> {
        return configuration.getStringList("active-rank-groups").toSet()
    }

    override fun createProfileInstance(uuid: UUID): Profile {
        return ProxyProfile(uuid)
    }

    override fun getProfileType(): Type {
        return object : TypeToken<ProxyProfile>() {}.type
    }

    override fun getRedis(): Redis {
        return Cubed.instance.redis
    }

    override fun getAPIUrl(): String {
        return configuration.getString("api-url")
    }

    override fun getAPIKey(): String {
        return configuration.getString("api-key")
    }

    override fun getUUIDCache(): UUIDCache {
        return Cubed.instance.uuidCache
    }

    override fun getGSON(): Gson {
        return Serializers.gson
    }

}