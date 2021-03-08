package com.minexd.core.profile

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.minexd.core.CoreXD
import net.evilblock.cubed.store.uuidcache.UUIDCache
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object ProfileHandler {

    private val GSON: Gson = GsonBuilder().serializeNulls().create()
    private val PROFILE_TYPE: Type = object : TypeToken<Profile>() {}.type

    private val profiles: MutableMap<UUID, Profile> = ConcurrentHashMap()

    /**
     * Retrieves a collection of the [Profile]s that are currently cached.
     */
    fun getCachedProfiles(): Collection<Profile> {
        return profiles.values
    }

    /**
     * Determine if a [Profile] is cached for the given [uuid].
     */
    fun isProfileCached(uuid: UUID): Boolean {
        return profiles.containsKey(uuid)
    }

    /**
     * Retrieves a [Profile] that is known to be cached.
     */
    fun getProfile(uuid: UUID): Profile {
        if (!profiles.containsKey(uuid)) {
            throw IllegalStateException("Profile $uuid is not cached in memory")
        } else {
            return profiles[uuid]!!
        }
    }

    /**
     * Attempts to retrieve a [Profile] first from the cache, or by fetching it.
     */
    fun getOrFetchProfile(uuid: UUID, ensureExists: Boolean): Profile? {
        return if (profiles.containsKey(uuid)) {
            getProfile(uuid)
        } else {
            fetchProfile(uuid, ensureExists)?.also { profile ->
                profile.cacheExpiry = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30L)
                profiles[uuid] = profile
                println("cached profile $uuid")
            }
        }
    }

    /**
     * Retrieves a [Profile] first from the cache, or by fetching it.
     */
    fun getOrFetchProfile(uuid: UUID): Profile {
        return getOrFetchProfile(uuid, ensureExists = true)!!
    }

    /**
     * Inserts a player's profile into the cache.
     */
    fun cacheProfile(profile: Profile) {
        profiles[profile.uuid] = profile
    }

    /**
     * Removes a player's profile from the cache.
     */
    fun forgetProfile(uuid: UUID): Profile? {
        return profiles.remove(uuid)
    }

    /**
     * Retrieves a [Profile] for the given [uuid].
     */
    fun fetchProfile(uuid: UUID, ensureExists: Boolean): Profile? {
        val start = System.currentTimeMillis()
        if (isProfileCachedInRedis(uuid)) {
            val profile = fetchProfileFromRedis(uuid)
            if (profile != null) {
                println("found profile (redis) in ${System.currentTimeMillis() - start}ms")
                return profile
            }
        }

        val response = if (ensureExists) {
            CoreXD.instance.profilesService.touch(uuid)
        } else {
            CoreXD.instance.profilesService.get(uuid)
        }.execute()

        return if (response.isSuccessful) {
            println("found profile (api) in ${System.currentTimeMillis() - start}ms")
            response.body()!!
        } else {
            if (response.code() == 404 && !ensureExists) {
                return null
            } else {
                throw IllegalStateException("Failed to fetch profile: ${response.code()} ${response.errorBody()?.string()}")
            }
        }
    }

    /**
     * Retrieves or creates a [Profile] for the given [uuid].
     */
    fun fetchProfile(uuid: UUID): Profile {
        return fetchProfile(uuid, ensureExists = true)!!
    }

    /**
     * Fetches a [Profile] for the given [input].
     *
     * If a profile does not exist for the given [input], and [ensureExists] is true, a new profile
     * will be created, stored, and returned.
     */
    fun fetchProfile(input: String, ensureExists: Boolean): Profile? {
        var uuid = try {
            UUID.fromString(input)
        } catch (e: Exception) {
            CoreXD.instance.plugin.getUUIDCache().uuid(input)
        }

        if (uuid == null) {
            val optionalProfile = UUIDCache.fetchFromMojang(input)
            if (optionalProfile.isPresent) {
                uuid = optionalProfile.get().first
                CoreXD.instance.plugin.getUUIDCache().update(optionalProfile.get().first, optionalProfile.get().second)
            }
        }

        if (uuid == null) {
            return null
        }

        return getOrFetchProfile(uuid, ensureExists)
    }

    private fun isProfileCachedInRedis(uuid: UUID): Boolean {
        return CoreXD.instance.plugin.getRedis().runRedisCommand { redis ->
            redis.exists("Cache:Perms:$uuid")
        }
    }

    private fun fetchProfileFromRedis(uuid: UUID): Profile? {
        return CoreXD.instance.plugin.getRedis().runRedisCommand { redis ->
            val raw = redis.hget("Cache:Perms:$uuid", "Raw")
            if (raw == null || raw.isEmpty() || raw.isBlank()) {
                null
            } else {
                GSON.fromJson(raw, PROFILE_TYPE)
            }
        }
    }

}