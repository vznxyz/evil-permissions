package com.minexd.core.bukkit.profile.punishment.command.blacklist

import com.minexd.core.CoreXD
import com.minexd.core.profile.Profile
import com.minexd.core.profile.punishment.PunishmentType
import com.minexd.core.util.Permissions
import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.flag.Flag
import net.evilblock.cubed.command.data.parameter.Param
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object UnblacklistCommand {

    @Command(
        names = ["unblacklist"],
        description = "Unblacklist a player",
        permission = Permissions.BLACKLIST_REMOVE,
        async = true
    )
    @JvmStatic
    fun execute(
        sender: CommandSender,
        @Flag(value = ["s", "silent"], description = "Silently un-blacklist the player") silent: Boolean,
        @Param(name = "player") profile: Profile,
        @Param(name = "reason", wildcard = true) reason: String
    ) {
        val removedBy = if (sender is Player) sender.uniqueId else null

        try {
            val requestData = mapOf(
                    "punishmentType" to PunishmentType.BLACKLIST.name,
                    "pardonedBy" to removedBy,
                    "pardonReason" to reason,
                    "silent" to silent
            )

            val response = CoreXD.instance.profilesService.pardon(profile.uuid, requestData).execute()
            if (!response.isSuccessful) {
                sender.sendMessage("${ChatColor.RED}${response.errorBody()!!.string()}!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            sender.sendMessage("${ChatColor.RED}Failed to send request to API!")
        }
    }
}