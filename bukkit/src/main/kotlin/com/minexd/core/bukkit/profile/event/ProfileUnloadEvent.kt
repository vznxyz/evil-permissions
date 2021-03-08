/*
 * Copyright (c) 2020. Joel Evans
 *
 * Use and or redistribution of compiled JAR file and or source code is permitted only if given
 * explicit permission from original author: Joel Evans
 */

package com.minexd.core.bukkit.profile.event

import com.minexd.core.profile.Profile
import net.evilblock.cubed.plugin.PluginEvent
import org.bukkit.event.Cancellable

class ProfileUnloadEvent(val profile: Profile) : PluginEvent(), Cancellable {

    private var cancelled: Boolean = false

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

}