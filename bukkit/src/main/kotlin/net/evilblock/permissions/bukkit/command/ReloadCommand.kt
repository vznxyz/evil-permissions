package net.evilblock.permissions.bukkit.command

import net.evilblock.cubed.command.Command
import net.evilblock.permissions.bukkit.BukkitPlugin
import net.evilblock.permissions.util.Permissions
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

object ReloadCommand {

    @Command(
        names = ["evilperms reload", "ep reload"],
        description = "Reload the EvilPermissions configuration",
        permission = Permissions.RELOAD,
        async = true
    )
    @JvmStatic
    fun execute(sender: CommandSender) {
        BukkitPlugin.instance.reloadConfig()

        sender.sendMessage("${ChatColor.GREEN}Reloaded EvilPermissions configuration!")
    }

}