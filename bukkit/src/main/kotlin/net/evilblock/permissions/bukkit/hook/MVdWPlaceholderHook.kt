package net.evilblock.permissions.bukkit.hook

import be.maximvdw.placeholderapi.PlaceholderAPI
import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.bukkit.BukkitPlugin
import net.evilblock.permissions.rank.RankHandler
import net.evilblock.permissions.user.UserHandler

object MVdWPlaceholderHook {

    @JvmStatic
    fun hook() {
        PlaceholderAPI.registerPlaceholder(BukkitPlugin.instance, "wp_rank_name") { event ->
            if (event.isOnline) {
                val user = UserHandler.getByUniqueId(event.player.uniqueId)
                return@registerPlaceholder user?.getBestDisplayRank()?.displayName ?: RankHandler.getDefaultRank().displayName
            } else {
                return@registerPlaceholder "offline"
            }
        }

        PlaceholderAPI.registerPlaceholder(BukkitPlugin.instance, "wp_rank_name_colored") { event ->
            if (event.isOnline) {
                val user = UserHandler.getByUniqueId(event.player.uniqueId)
                return@registerPlaceholder user?.getBestDisplayRank()?.getColoredDisplayName() ?: RankHandler.getDefaultRank().getColoredDisplayName()
            } else {
                return@registerPlaceholder "offline"
            }
        }

        PlaceholderAPI.registerPlaceholder(BukkitPlugin.instance, "wp_rank_prefix") { event ->
            if (event.isOnline) {
                val user = UserHandler.getByUniqueId(event.player.uniqueId)
                return@registerPlaceholder user?.getBestDisplayRank()?.getChatPrefix() ?: RankHandler.getDefaultRank().getChatPrefix()
            } else {
                return@registerPlaceholder "offline"
            }
        }
    }

}