package com.minexd.core.plugin

import com.google.gson.Gson
import com.minexd.core.profile.Profile
import net.evilblock.cubed.store.redis.Redis
import net.evilblock.cubed.store.uuidcache.UUIDCache
import java.lang.reflect.Type
import java.util.*
import java.util.logging.Logger

interface Plugin {

    fun getEventHandler(): PluginEventHandler

    fun getActiveGroups(): Set<String>

    fun getLogger(): Logger

    fun createProfileInstance(uuid: UUID): Profile

    fun getProfileType(): Type

    fun getAPIUrl(): String

    fun getAPIKey(): String

    fun getRedis(): Redis

    fun getUUIDCache(): UUIDCache

    fun getGSON(): Gson

}