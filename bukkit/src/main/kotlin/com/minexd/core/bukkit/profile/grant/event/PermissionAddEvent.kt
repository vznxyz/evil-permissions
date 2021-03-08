package com.minexd.core.bukkit.profile.grant.event

import com.minexd.core.profile.Profile
import net.evilblock.cubed.plugin.PluginEvent

class PermissionAddEvent(val profile: Profile, val permission: String) : PluginEvent()