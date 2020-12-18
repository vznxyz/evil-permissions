package net.evilblock.permissions.bukkit.user.grant.command

import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.parameter.Param
import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.bukkit.user.grant.menu.CheckGrantsMenu
import net.evilblock.permissions.user.User
import net.evilblock.permissions.util.Permissions
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
    fun execute(player: Player, @Param(name = "player") user: User) {
        val grants = EvilPermissions.instance.database.fetchGrantsIssuedBy(user.uniqueId)
        if (grants.isEmpty()) {
            player.sendMessage("${ChatColor.RED}")
        } else {
            CheckGrantsMenu(user.uniqueId, grants).openMenu(player)
        }
    }

}