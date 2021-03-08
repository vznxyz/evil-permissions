package com.minexd.core.bukkit.profile.command

import com.minexd.core.bukkit.profile.menu.ProfileMenu
import com.minexd.core.profile.Profile
import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.parameter.Param
import org.bukkit.entity.Player

object ProfileCommand {

    @Command(
            names = ["profile", "prof", "user", "u"],
            description = "Open a player's profile",
            async = true
    )
    @JvmStatic
    fun execute(player: Player, @Param(name = "player", defaultValue = "self") profile: Profile) {
        ProfileMenu(profile).openMenu(player)
    }

}