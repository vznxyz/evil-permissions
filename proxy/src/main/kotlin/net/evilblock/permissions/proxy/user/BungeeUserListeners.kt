package net.evilblock.permissions.proxy.user

import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.user.UserHandler
import net.md_5.bungee.api.event.LoginEvent
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

object BungeeUserListeners : Listener {

    @EventHandler(priority = 99)
    fun onLoginEvent(event: LoginEvent) {
        if (!event.isCancelled) {
            var user = EvilPermissions.instance.database.fetchUser(event.connection.uniqueId)

            if (user == null) {
                user = EvilPermissions.instance.plugin.makeUser(event.connection.uniqueId)
            }

            UserHandler.loadedUsers[user.uniqueId] = user
        }
    }

    @EventHandler(priority = 0)
    fun onPostLoginEvent(event: PostLoginEvent) {
        UserHandler.getByUniqueId(event.player.uniqueId)!!.apply()
    }

    @net.md_5.bungee.event.EventHandler(priority = 99)
    fun onPlayerDisconnectEvent(event: PlayerDisconnectEvent) {
        UserHandler.loadedUsers.remove(event.player.uniqueId)
    }

}