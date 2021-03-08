package com.minexd.core.bukkit.profile.grant.event

import net.evilblock.cubed.plugin.PluginEvent
import com.minexd.core.profile.Profile
import com.minexd.core.profile.grant.Grant

class GrantCreateEvent(val profile: Profile, val grant: Grant) : PluginEvent()