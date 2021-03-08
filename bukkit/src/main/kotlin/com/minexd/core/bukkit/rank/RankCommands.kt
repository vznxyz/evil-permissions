package com.minexd.core.bukkit.rank

import net.evilblock.cubed.command.Command
import com.minexd.core.bukkit.rank.menu.RankGroupEditorMenu
import com.minexd.core.rank.RankHandler
import com.minexd.core.profile.ProfileHandler
import com.minexd.core.util.TimeUtils
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object RankCommands {

    @Command(
        names = ["rank"],
        description = "Check your current rank and its expiration"
    )
    @JvmStatic
    fun execute(player: Player) {
        val profile = ProfileHandler.getProfile(player.uniqueId)
        val activeRank = profile.getBestDisplayRank()
        val activeGrant = profile.getBestDisplayGrant()

        player.sendMessage("${ChatColor.GOLD}You're the ${ChatColor.RESET}${activeRank.getColoredDisplayName()} ${ChatColor.GOLD}rank.")

        if (activeGrant?.expiresAt != null) {
            val formattedTime = TimeUtils.formatIntoDetailedString(((activeGrant.expiresAt!! - System.currentTimeMillis()) / 1000).toInt())
            player.sendMessage("${ChatColor.GOLD}Your rank will expire in ${ChatColor.GREEN}$formattedTime${ChatColor.GOLD}.")
        }
    }

    @Command(
        names = ["rank editor"],
        permission = "op",
        async = true
    )
    @JvmStatic
    fun editor(player: Player) {
        RankGroupEditorMenu().openMenu(player)
    }

    @Command(
        names = ["rank list"],
        permission = "op",
        async = true
    )
    @JvmStatic
    fun list(sender: CommandSender) {
        for (rank in RankHandler.getRanks().sortedBy { rank -> rank.displayOrder }) {
            sender.sendMessage(rank.getColoredDisplayName())
        }
    }

}