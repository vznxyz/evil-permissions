package com.minexd.core.bukkit.hook

import be.maximvdw.placeholderapi.PlaceholderAPI
import com.minexd.core.bukkit.BukkitPlugin
import com.minexd.core.rank.RankHandler
import com.minexd.core.profile.ProfileHandler

object MVdWPlaceholderHook {

    @JvmStatic
    fun hook() {
        PlaceholderAPI.registerPlaceholder(BukkitPlugin.instance, "wp_rank_name") { event ->
            if (event.isOnline) {
                val profile = ProfileHandler.getProfile(event.player.uniqueId)
                return@registerPlaceholder profile?.getBestDisplayRank()?.displayName ?: RankHandler.getDefaultRank().displayName
            } else {
                return@registerPlaceholder "offline"
            }
        }

        PlaceholderAPI.registerPlaceholder(BukkitPlugin.instance, "wp_rank_name_colored") { event ->
            if (event.isOnline) {
                val profile = ProfileHandler.getProfile(event.player.uniqueId)
                return@registerPlaceholder profile?.getBestDisplayRank()?.getColoredDisplayName() ?: RankHandler.getDefaultRank().getColoredDisplayName()
            } else {
                return@registerPlaceholder "offline"
            }
        }

        PlaceholderAPI.registerPlaceholder(BukkitPlugin.instance, "wp_rank_prefix") { event ->
            if (event.isOnline) {
                val profile = ProfileHandler.getProfile(event.player.uniqueId)
                return@registerPlaceholder profile?.getBestDisplayRank()?.getChatPrefix() ?: RankHandler.getDefaultRank().getChatPrefix()
            } else {
                return@registerPlaceholder "offline"
            }
        }
    }

}