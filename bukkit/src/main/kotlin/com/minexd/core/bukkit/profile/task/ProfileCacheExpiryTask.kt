/*
 * Copyright (c) 2020. Joel Evans
 *
 * Use and or redistribution of compiled JAR file and or source code is permitted only if given
 * explicit permission from original author: Joel Evans
 */

package com.minexd.core.bukkit.profile.task

import com.minexd.core.bukkit.profile.event.ProfileUnloadEvent
import com.minexd.core.profile.Profile
import com.minexd.core.profile.ProfileHandler
import org.bukkit.Bukkit

object ProfileCacheExpiryTask : Runnable {

    override fun run() {
        val expired = arrayListOf<Profile>()

        // find expired users
        for (profile in ProfileHandler.getCachedProfiles()) {
            if (Bukkit.getPlayer(profile.uuid) == null && profile.cacheExpiry != null && System.currentTimeMillis() >= profile.cacheExpiry!!) {
                expired.add(profile)
            }
        }

        // save (if needed) and unload expired users
        for (user in expired) {
            val unloadEvent = ProfileUnloadEvent(user)
            unloadEvent.call()

            if (unloadEvent.isCancelled) {
                continue
            }

            ProfileHandler.forgetProfile(user.uuid)
        }
    }

}