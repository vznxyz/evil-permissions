package net.evilblock.permissions.plugin.bungee.user

import com.google.gson.JsonObject
import net.evilblock.pidgin.message.handler.IncomingMessageHandler
import net.evilblock.pidgin.message.listener.MessageListener
import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.plugin.bungee.BungeePlugin
import net.evilblock.permissions.plugin.bungee.user.grant.event.GrantCreateEvent
import net.evilblock.permissions.plugin.bungee.user.grant.event.GrantRemoveEvent
import java.util.*

object BungeeUserMessageListeners : MessageListener {

    @IncomingMessageHandler("GRANT_UPDATE")
    fun onGrantUpdate(json: JsonObject) {
        val uniqueId = UUID.fromString(json.get("uniqueId").asString)
        val player = BungeePlugin.instance.proxy.getPlayer(uniqueId) ?: return

        val user = EvilPermissions.instance.userHandler.getByUniqueId(uniqueId)
        if (user != null) {
            val grantId = UUID.fromString(json.get("grant").asString)
            for (grant in user.grants) {
                if (grant.id == grantId) {
                    if (grant.removedAt != null) {
                        BungeePlugin.instance.proxy.pluginManager.callEvent(GrantRemoveEvent(player, grant))
                    } else {
                        BungeePlugin.instance.proxy.pluginManager.callEvent(GrantCreateEvent(player, grant))
                    }
                }
            }
        }
    }

}