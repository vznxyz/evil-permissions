package net.evilblock.permissions.plugin.bukkit.hook

import be.maximvdw.placeholderapi.PlaceholderAPI
import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.plugin.bukkit.BukkitPlugin

object MVdWPlaceholderHook {

    @JvmStatic
    fun hook() {
        PlaceholderAPI.registerPlaceholder(BukkitPlugin.instance, "wp_rank_name") { event ->
            if (event.isOnline) {
                val user = EvilPermissions.instance.userHandler.getByUniqueId(event.player.uniqueId)
                return@registerPlaceholder user?.getBestDisplayRank()?.displayName ?: EvilPermissions.instance.rankHandler.getDefaultRank().displayName
            } else {
                return@registerPlaceholder "offline"
            }
        }

        PlaceholderAPI.registerPlaceholder(BukkitPlugin.instance, "wp_rank_name_colored") { event ->
            if (event.isOnline) {
                val user = EvilPermissions.instance.userHandler.getByUniqueId(event.player.uniqueId)
                return@registerPlaceholder user?.getBestDisplayRank()?.getColoredDisplayName() ?: EvilPermissions.instance.rankHandler.getDefaultRank().getColoredDisplayName()
            } else {
                return@registerPlaceholder "offline"
            }
        }

        PlaceholderAPI.registerPlaceholder(BukkitPlugin.instance, "wp_rank_prefix") { event ->
            if (event.isOnline) {
                val user = EvilPermissions.instance.userHandler.getByUniqueId(event.player.uniqueId)
                return@registerPlaceholder user?.getBestDisplayRank()?.getChatPrefix() ?: EvilPermissions.instance.rankHandler.getDefaultRank().getChatPrefix()
            } else {
                return@registerPlaceholder "offline"
            }
        }
    }

}