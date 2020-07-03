package net.evilblock.permissions.plugin.bungee.user

import net.evilblock.permissions.EvilPermissions
import net.md_5.bungee.api.event.LoginEvent
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

class BungeeUserListeners : Listener {

    @EventHandler(priority = 99)
    fun onLoginEvent(event: LoginEvent) {
        if (!event.isCancelled) {
            var user = EvilPermissions.instance.database.fetchUser(event.connection.uniqueId)

            if (user == null) {
                user = EvilPermissions.instance.plugin.makeUser(event.connection.uniqueId)
            }

            EvilPermissions.instance.userHandler.loadedUsers[user.uniqueId] = user
        }
    }

    @EventHandler(priority = 0)
    fun onPostLoginEvent(event: PostLoginEvent) {
        EvilPermissions.instance.userHandler.getByUniqueId(event.player.uniqueId)!!.apply()
    }

    @net.md_5.bungee.event.EventHandler(priority = 99)
    fun onPlayerDisconnectEvent(event: PlayerDisconnectEvent) {
        EvilPermissions.instance.userHandler.loadedUsers.remove(event.player.uniqueId)
    }

}