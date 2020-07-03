package net.evilblock.permissions.plugin.bungee.user.grant.event

import net.evilblock.permissions.user.grant.Grant
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Event

class GrantCreateEvent(val player: ProxiedPlayer, val grant: Grant) : Event()