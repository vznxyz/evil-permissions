package com.minexd.core.bukkit.friend.command

import com.minexd.core.bukkit.friend.menu.FriendsMenu
import com.minexd.core.friend.cache.FriendsCache
import net.evilblock.cubed.command.Command
import org.bukkit.ChatColor
import org.bukkit.entity.Player

object FriendsCommand {

    @Command(
        names = ["friends", "friends list"],
        description = "Open your friends list"
    )
    @JvmStatic
    fun execute(player: Player) {
        if (!FriendsCache.isLoaded(player.uniqueId)) {
            player.sendMessage("${ChatColor.RED}Whoops! We couldn't fetch your friends list...")
            return
        }

        FriendsMenu(FriendsCache.get(player.uniqueId)).openMenu(player)
    }

}