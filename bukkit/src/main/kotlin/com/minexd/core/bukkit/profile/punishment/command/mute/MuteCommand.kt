package com.minexd.core.bukkit.profile.punishment.command.mute

import com.minexd.core.CoreXD
import com.minexd.core.profile.Profile
import com.minexd.core.profile.punishment.Punishment
import com.minexd.core.profile.punishment.PunishmentType
import com.minexd.core.util.Permissions
import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.flag.Flag
import net.evilblock.cubed.command.data.parameter.Param
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

object MuteCommand {

    @Command(
        names = ["mute"],
        description = "Mute a player",
        permission = Permissions.MUTE_PERMANENT,
        async = true
    )
    @JvmStatic
    fun execute(
        sender: CommandSender,
        @Flag(value = ["s", "silent"], description = "Silently mute the player") silent: Boolean,
        @Param(name = "player") profile: Profile,
        @Param(name = "reason", wildcard = true) reason: String
    ) {
        var issuer: UUID? = null
        if (sender is Player) {
            issuer = sender.uniqueId
        }

        if (profile.getActivePunishment(PunishmentType.MUTE) != null) {
            sender.sendMessage("${profile.getUsername()} ${ChatColor.RED}is already muted!")
            return
        }

        val punishment = Punishment(
                uuid = UUID.randomUUID(),
                punishmentType = PunishmentType.MUTE
        )

        punishment.reason = reason
        punishment.issuedBy = issuer

        try {
            val response = CoreXD.instance.profilesService.punish(profile.uuid, punishment).execute()
            if (!response.isSuccessful) {
                sender.sendMessage("${ChatColor.RED}${response.errorBody()!!.string()}!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            sender.sendMessage("${ChatColor.RED}Failed to send request to API!")
        }
    }

}