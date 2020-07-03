package net.evilblock.permissions.plugin.bukkit.user.listener

import com.google.gson.JsonObject
import net.evilblock.pidgin.message.handler.IncomingMessageHandler
import net.evilblock.pidgin.message.listener.MessageListener
import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.plugin.bukkit.user.grant.event.GrantCreateEvent
import net.evilblock.permissions.plugin.bukkit.user.grant.event.GrantRemoveEvent
import org.bukkit.Bukkit
import java.util.*

object BukkitUserMessageListeners : MessageListener {

    @IncomingMessageHandler("GRANT_UPDATE")
    fun onGrantUpdate(json: JsonObject) {
        val uniqueId = UUID.fromString(json.get("uniqueId").asString)
        val player = Bukkit.getPlayer(uniqueId) ?: return

        val user = EvilPermissions.instance.userHandler.getByUniqueId(uniqueId)
        if (user != null) {
            val grantId = UUID.fromString(json.get("grant").asString)
            for (grant in user.grants) {
                if (grant.id == grantId) {
                    if (grant.removedAt != null) {
                        Bukkit.getServer().pluginManager.callEvent(GrantRemoveEvent(player, grant))
                    } else {
                        Bukkit.getServer().pluginManager.callEvent(GrantCreateEvent(player, grant))
                    }
                }
            }
        }
    }

}