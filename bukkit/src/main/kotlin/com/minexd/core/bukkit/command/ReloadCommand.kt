package com.minexd.core.bukkit.command

import net.evilblock.cubed.command.Command
import com.minexd.core.bukkit.BukkitPlugin
import com.minexd.core.util.Permissions
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

object ReloadCommand {

    @Command(
        names = ["core reload"],
        description = "Reload the Core config",
        permission = Permissions.RELOAD,
        async = true
    )
    @JvmStatic
    fun execute(sender: CommandSender) {
        BukkitPlugin.instance.reloadConfig()

        sender.sendMessage("${ChatColor.GREEN}Successfully reloaded the Core config!")
    }

}