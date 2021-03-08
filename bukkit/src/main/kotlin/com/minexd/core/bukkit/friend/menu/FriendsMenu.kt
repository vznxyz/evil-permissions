package com.minexd.core.bukkit.friend.menu

import com.minexd.core.bukkit.util.Constants
import com.minexd.core.friend.result.Friend
import net.evilblock.cubed.menu.Button
import com.minexd.core.friend.FriendsList
import net.evilblock.cubed.menu.FilteredPaginatedMenu
import net.evilblock.cubed.menu.buttons.GlassButton
import net.evilblock.cubed.menu.buttons.TexturedHeadButton
import net.evilblock.cubed.menu.filter.MenuFilter
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.ItemUtils
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack

class FriendsMenu(val friendsList: FriendsList) : FilteredPaginatedMenu<Friend>() {

    var filters: MutableList<MenuFilter<Friend>> = arrayListOf()

    init {
        autoUpdate = true
        updateAfterClick = true
    }

    override fun getAutoUpdateTicks(): Long {
        return 3000L
    }

    override fun getPrePaginatedTitle(player: Player): String {
        return "Friends"
    }

    override fun getGlobalButtons(player: Player): Map<Int, Button>? {
        return hashMapOf<Int, Button>().also { buttons ->
            buttons[0] = FriendsButton()
            buttons[1] = FriendRequestsButton()
//            buttons[2] = FriendsButton()

            for (i in 9 until 18) {
                buttons[i] = GlassButton(5)
            }
        }
    }

    override fun getSourceSet(player: Player): List<Friend> {
        return friendsList.getFriends().toList()
    }

    override fun getFilters(player: Player): List<MenuFilter<Friend>> {
        return filters
    }

    override fun size(buttons: Map<Int, Button>): Int {
        return 54
    }

    override fun getMaxItemsPerPage(player: Player): Int {
        return 36
    }

    override fun getButtonsStartOffset(): Int {
        return 9
    }

    private inner class FriendsButton : TexturedHeadButton(Constants.FRIENDS_ICON) {
        override fun getName(player: Player): String {
            return "${ChatColor.GREEN}${ChatColor.BOLD}Friends"
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            if (clickType.isLeftClick) {
                FriendsMenu(friendsList).openMenu(player)
            }
        }
    }

    private inner class FriendRequestsButton : TexturedHeadButton(Constants.FRIEND_REQUESTS_ICON) {
        override fun getName(player: Player): String {
            return "${ChatColor.AQUA}${ChatColor.BOLD}Friend Requests"
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            if (clickType.isLeftClick) {

            }
        }
    }

    inner class FriendButton(friend: Friend) : FilteredItemButton<Friend>(friend) {
        override fun getName(player: Player): String {
            return "${ChatColor.YELLOW}${item.username}"
        }

        override fun getDescription(player: Player): List<String> {
            return arrayListOf<String>().also { desc ->
                if (friendsList.isPresenceLoaded() && friendsList.hasPresence(item.uuid)) {
                    val presence = friendsList.getPresence(item.uuid)
                    if (presence.isOnline()) {
                        desc.add("${ChatColor.GREEN}Currently online, playing ${presence.server}")

                        if (presence.rich != null) {
                            desc.add("${ChatColor.GRAY}${presence.rich}")
                        }
                    } else {
                        desc.add("${ChatColor.RED}Currently offline")
                    }
                } else {
                    desc.add("${ChatColor.GRAY}Loading...")
                }
            }
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            if (clickType.isLeftClick) {
                player.sendMessage("friend ${item.username}")
            }
        }

        override fun getButtonItem(player: Player): ItemStack {
            return ItemBuilder
                    .copyOf(ItemUtils.getPlayerHeadItem(item.username))
                    .name(getName(player))
                    .setLore(getDescription(player))
                    .build()
        }
    }

    override fun createItemButton(player: Player, item: Friend): FilteredItemButton<Friend> {
        return FriendButton(item)
    }

    private inner class FriendRequestButton(friend: Friend)

}