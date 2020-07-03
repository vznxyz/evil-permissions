package net.evilblock.permissions.plugin.bungee.user

import net.evilblock.permissions.plugin.bungee.BungeePlugin
import net.evilblock.permissions.user.User
import java.util.*

class BungeeUser(uuid: UUID) : User(uuid) {

    override fun getUsername(): String {
        return ""
    }

    override fun apply() {
        val player = BungeePlugin.instance.proxy.getPlayer(uniqueId) ?: return

        getCompoundedPermissions().forEach {
            if (it.startsWith("-")) {
                player.setPermission(it.substring(1), false)
            } else {
                player.setPermission(it, true)
            }
        }
    }

}