package com.minexd.core.bukkit.friend.menu.filter

import com.minexd.core.friend.result.Friend
import net.evilblock.cubed.menu.FilteredPaginatedMenu
import net.evilblock.cubed.menu.filter.MenuFilter
import org.bukkit.entity.Player

class UsernameFilter(val search: String) : MenuFilter<Friend> {

    override fun filter(player: Player, menu: FilteredPaginatedMenu<Friend>, list: List<Friend>): List<Friend> {
        val set = hashSetOf<Friend>()

        // give better search results by
        // 1. first searching for names that start with the query
        // 2. second searching for names that contain the query (and are not already added, so we use set)
        set.addAll(list.filter { it.username.startsWith(search, ignoreCase = true) })
        set.addAll(list.filter { it.username.contains(search, ignoreCase = true) })

        return set.toList()
    }

}