package net.evilblock.permissions.bukkit.user.listener

import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.bukkit.BukkitPlugin
import net.evilblock.permissions.user.UserHandler
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class BukkitUserListeners : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onAsyncPlayerPreLoginEvent(event: AsyncPlayerPreLoginEvent) {
        try {
            var user = EvilPermissions.instance.database.fetchUser(event.uniqueId)

            if (user == null) {
                user = EvilPermissions.instance.plugin.makeUser(event.uniqueId)
            }

            UserHandler.loadedUsers[user.uniqueId] = user
        } catch (e: Exception) {
            EvilPermissions.instance.plugin.getLogger().severe("Failed to load user data for ${event.uniqueId}")
            e.printStackTrace()
            event.loginResult = AsyncPlayerPreLoginEvent.Result.KICK_OTHER
            event.kickMessage = "${ChatColor.RED}${ChatColor.BOLD}Failed to load your user data.\n${ChatColor.RED}${ChatColor.BOLD}Please try reconnecting again later."
        }
    }

    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        BukkitPlugin.instance.server.scheduler.runTaskAsynchronously(BukkitPlugin.instance) {
            UserHandler.getByUniqueId(event.player.uniqueId)?.apply()
        }
    }

    @EventHandler
    fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        val user = UserHandler.getByUniqueId(event.player.uniqueId)
        if (user != null && user.requiresSave) {
            EvilPermissions.instance.database.saveUser(user)
        }

        UserHandler.loadedUsers.remove(event.player.uniqueId)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onAsyncPlayerChatEventLow(event: AsyncPlayerChatEvent) {
        if (BukkitPlugin.instance.config.getBoolean("format-chat")) {
            val user = UserHandler.getByUniqueId(event.player.uniqueId)
            if (user != null) {
                val activeRank = user.getBestDisplayRank()

                val format = activeRank.processPlaceholders(BukkitPlugin.instance.config.getString("chat-format"))
                event.format = ChatColor.translateAlternateColorCodes('&', format)
            }
        }
    }

}