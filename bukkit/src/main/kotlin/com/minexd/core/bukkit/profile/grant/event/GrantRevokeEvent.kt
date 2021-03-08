package com.minexd.core.bukkit.profile.grant.event

import net.evilblock.cubed.plugin.PluginEvent
import com.minexd.core.bukkit.profile.BukkitProfile
import com.minexd.core.profile.Profile
import com.minexd.core.profile.grant.Grant

class GrantRevokeEvent(val profile: Profile, val grant: Grant) : PluginEvent()