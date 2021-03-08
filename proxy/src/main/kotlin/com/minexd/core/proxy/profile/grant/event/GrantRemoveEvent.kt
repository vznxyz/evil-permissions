package com.minexd.core.proxy.profile.grant.event

import com.minexd.core.proxy.profile.ProxyProfile
import com.minexd.core.profile.grant.Grant
import net.md_5.bungee.api.plugin.Event

class GrantRemoveEvent(val user: ProxyProfile, val grant: Grant) : Event()