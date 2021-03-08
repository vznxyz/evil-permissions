package com.minexd.core.bukkit.profile.punishment.command.history

import com.minexd.core.profile.Profile
import com.minexd.core.util.Permissions
import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.parameter.Param
import com.minexd.core.bukkit.profile.punishment.menu.PunishmentTypesMenu
import org.bukkit.entity.Player

object CheckCommand {

    @Command(
        names = ["check", "c", "history"],
        description = "View a player's punishments",
        permission = Permissions.HISTORY,
        async = true
    )
    @JvmStatic
    fun execute(player: Player, @Param(name = "player") profile: Profile) {
        PunishmentTypesMenu(profile).openMenu(player)
    }

}