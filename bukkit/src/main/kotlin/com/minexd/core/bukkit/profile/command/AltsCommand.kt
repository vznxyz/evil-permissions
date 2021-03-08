package com.minexd.core.bukkit.profile.command

import com.minexd.core.profile.punishment.PunishmentType
import com.minexd.core.CoreXD
import com.minexd.core.profile.Profile
import com.minexd.core.profile.ProfileHandler
import com.minexd.core.util.Permissions
import net.evilblock.cubed.Cubed
import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.parameter.Param
import net.evilblock.cubed.util.text.TextSplitter
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

object AltsCommand {

    @Command(
        names = ["alts", "findalts", "dupeip", "shared-accs", "sharedaccs"],
        description = "Find a player's alt accounts",
        permission = Permissions.ALTS_VIEW,
        async = true
    )
    @JvmStatic
    fun execute(sender: CommandSender, @Param(name = "player") target: Profile) {
        val response = try {
            CoreXD.instance.profilesService.getSharedAccounts(target.uuid).execute()
        } catch (e: Exception) {
            e.printStackTrace()
            sender.sendMessage("${ChatColor.RED}Failed to fetch ${target.getColoredUsername()}${ChatColor.RED}'s shared accounts!")
            return
        }

        if (!response.isSuccessful) {
            sender.sendMessage("${ChatColor.RED}${response.errorBody()!!.string()}!")
            return
        }

        val sharedAccounts = response.body()!!
        if (sharedAccounts.isEmpty()) {
            sender.sendMessage("${ChatColor.RED}No shared accounts found for ${target.getColoredUsername()}${ChatColor.RED}!")
            return
        }

        val renderedNames = sharedAccounts.joinToString(separator = "${ChatColor.GRAY}, ") { altId ->
            val altProfile = ProfileHandler.getOrFetchProfile(altId)

            when {
                altProfile.getActivePunishment(PunishmentType.BAN) != null -> {
                    "${ChatColor.RED}${altProfile.getUsername()}"
                }
                altProfile.getActivePunishment(PunishmentType.BLACKLIST) != null -> {
                    "${ChatColor.DARK_RED}${altProfile.getUsername()}"
                }
                else -> {
                    Cubed.instance.uuidCache.name(altId)
                }
            }
        }

        if (renderedNames.length < 4000) {
            sender.sendMessage("${ChatColor.YELLOW}Alts of ${target.getColoredUsername()} ${ChatColor.YELLOW}${ChatColor.BOLD}(${sharedAccounts.size}): ${ChatColor.GRAY}$renderedNames")
        } else {
            var first = true
            for (message in TextSplitter.split(length = 4000, text = renderedNames)) {
                if (first) {
                    sender.sendMessage("${ChatColor.YELLOW}Alts of ${target.getColoredUsername()} ${ChatColor.YELLOW}${ChatColor.BOLD}(${sharedAccounts.size}): ${ChatColor.GRAY}$message")
                } else {
                    sender.sendMessage("${ChatColor.GRAY}$message")
                }

                first = false
            }
        }
    }

}