package net.evilblock.permissions.plugin.bukkit.user.listener

import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.plugin.bukkit.BukkitPlugin
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

            EvilPermissions.instance.userHandler.loadedUsers[user.uniqueId] = user
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
            EvilPermissions.instance.userHandler.getByUniqueId(event.player.uniqueId)?.apply()
        }
    }

    @EventHandler
    fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        val user = EvilPermissions.instance.userHandler.getByUniqueId(event.player.uniqueId)
        if (user != null && user.requiresSave) {
            EvilPermissions.instance.database.saveUser(user)
        }

        EvilPermissions.instance.userHandler.loadedUsers.remove(event.player.uniqueId)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onAsyncPlayerChatEventLow(event: AsyncPlayerChatEvent) {
        if (BukkitPlugin.instance.config.getBoolean("format-chat")) {
            val user = EvilPermissions.instance.userHandler.getByUniqueId(event.player.uniqueId)
            if (user != null) {
                val activeRank = user.getBestDisplayRank()

                val format = BukkitPlugin.instance.config.getString("chat-format")
                    .replace("{rankDisplayName}", activeRank.displayName)
                    .replace("{rankPlayerListPrefix}", activeRank.playerListPrefix)
                    .replace("{rankChatPrefix}", activeRank.prefix)
                    .replace("{rankColor}", activeRank.gameColor)

                    event.format = ChatColor.translateAlternateColorCodes('&', format)
            }
        }
    }

}