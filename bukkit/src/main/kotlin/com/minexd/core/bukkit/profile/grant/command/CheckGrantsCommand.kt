package com.minexd.core.bukkit.profile.grant.command

import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.parameter.Param
import com.minexd.core.CoreXD
import com.minexd.core.bukkit.profile.grant.menu.CheckGrantsMenu
import com.minexd.core.profile.Profile
import com.minexd.core.util.Permissions
import org.bukkit.ChatColor
import org.bukkit.entity.Player

object CheckGrantsCommand {

    @Command(
        names = ["cgrants", "checkgrants", "check-grants"],
        description = "View grants issued by a specific staff member",
        permission = Permissions.GRANTS_STAFF_HISTORY,
        async = true
    )
    @JvmStatic
    fun execute(player: Player, @Param(name = "player") profile: Profile) {
        try {
            val response = CoreXD.instance.auditService.getGrantsIssuedBy(profile.uuid).execute()
            if (response.isSuccessful) {
                CheckGrantsMenu(profile.uuid, response.body()!!).openMenu(player)
            } else {
                player.sendMessage("${ChatColor.RED}${response.errorBody()!!.string()}!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            player.sendMessage("${ChatColor.RED}Failed to send request to API!")
        }
    }

}