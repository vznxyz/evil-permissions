package com.minexd.core.proxy.profile.grant.event

import com.minexd.core.proxy.profile.ProxyProfile
import net.md_5.bungee.api.plugin.Event

class PermissionRemoveEvent(val user: ProxyProfile, val permission: String) : Event()