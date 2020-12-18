package net.evilblock.permissions.bukkit.user.command

import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.parameter.Param
import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.user.User
import net.evilblock.permissions.util.Permissions
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

object UserResetCommand {

    @Command(
        names = ["evilperms user reset", "ep user reset"],
        description = "Reset a user's grants",
        permission = Permissions.USER_RESET,
        async = true
    )
    @JvmStatic
    fun execute(sender: CommandSender, @Param(name = "target", defaultValue = "self") target: User) {
        target.grants.clear()
        target.permissions.clear()

        EvilPermissions.instance.database.saveUser(target)

        sender.sendMessage("${ChatColor.GREEN}Successfully reset user!")
    }

}