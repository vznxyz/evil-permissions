package com.minexd.core.bukkit.presence.command

import net.evilblock.cubed.Cubed
import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.parameter.Param
import com.minexd.core.presence.PlayerPresenceHandler
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import java.util.*

object PresenceFindCommand {

    @Command(
        names = ["pfind"],
        description = "Find a player's presence",
        permission = "core.presence.find",
        async = true
    )
    @JvmStatic
    fun execute(sender: CommandSender, @Param(name = "target") uuid: UUID) {
        val presence = PlayerPresenceHandler.findPresence(uuid)
        if (presence != null) {
            sender.sendMessage("${ChatColor.GOLD}Found presence! ${ChatColor.WHITE}${Cubed.instance.uuidCache.name(uuid)} ${ChatColor.YELLOW}is playing ${ChatColor.GREEN}${presence.server}${ChatColor.YELLOW}!")
        } else {
            sender.sendMessage("${ChatColor.RED}Couldn't find a presence for ${ChatColor.WHITE}${Cubed.instance.uuidCache.name(uuid)}${ChatColor.RED}!")
        }
    }

}