package com.minexd.core.bukkit.profile.menu

import com.minexd.core.bukkit.friend.menu.FriendsMenu
import com.minexd.core.bukkit.util.Constants
import com.minexd.core.friend.cache.FriendsCache
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.buttons.MenuButton
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

object ProfileLayout {

    fun render(player: Player, buttons: MutableMap<Int, Button>) {
        buttons[0] = MenuButton()
                .name("${ChatColor.GOLD}${ChatColor.BOLD}My Profile")
                .playerTexture(player.name)
                .action(ClickType.LEFT) {

                }

        buttons[3] = MenuButton()
                .name("${ChatColor.GREEN}${ChatColor.BOLD}Friends")
                .texturedIcon(Constants.FRIENDS_ICON)
                .action(ClickType.LEFT) {
                    if (FriendsCache.isLoaded(player.uniqueId)) {
                        FriendsMenu(FriendsCache.get(player.uniqueId)).openMenu(player)
                    } else {
                        player.sendMessage("${ChatColor.RED}Whoops! We couldn't fetch your friends list...")
                    }
                }

        buttons[4] = MenuButton()
                .name("${ChatColor.LIGHT_PURPLE}${ChatColor.BOLD}Statistics")
                .texturedIcon(Constants.STATISTICS_ICON)

        buttons[8] = MenuButton()
                .name("${ChatColor.WHITE}${ChatColor.BOLD}Settings")
                .texturedIcon(Constants.SETTINGS_ICON)
    }

}