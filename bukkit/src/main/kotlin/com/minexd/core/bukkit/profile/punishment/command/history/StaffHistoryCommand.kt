package com.minexd.core.bukkit.profile.punishment.command.history

import com.minexd.core.CoreXD
import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.parameter.Param
import com.minexd.core.bukkit.profile.punishment.menu.PunishmentsIssuedByMenu
import com.minexd.core.profile.Profile
import com.minexd.core.util.Permissions
import org.bukkit.ChatColor
import org.bukkit.entity.Player

object StaffHistoryCommand {

    @Command(
        names = ["staffhitory", "chistory"],
        description = "View punishments issued by a specific staff member",
        permission = Permissions.HISTORY_STAFF,
        async = true
    )
    @JvmStatic
    fun execute(player: Player, @Param(name = "player") profile: Profile) {
        try {
            val response = CoreXD.instance.auditService.getPunishmentsIssuedBy(profile.uuid).execute()
            if (response.isSuccessful) {
                val results = response.body()!!
                PunishmentsIssuedByMenu(profile.uuid, results).openMenu(player)
            } else {
                player.sendMessage("${ChatColor.RED}${response.errorBody()!!.string()}!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            player.sendMessage("${ChatColor.RED}Failed to send request to API!")
        }
    }

}