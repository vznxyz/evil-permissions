package com.minexd.core.bukkit.profile.punishment.command.ban

import com.minexd.core.CoreXD
import com.minexd.core.profile.Profile
import com.minexd.core.profile.punishment.Punishment
import com.minexd.core.profile.punishment.PunishmentType
import com.minexd.core.util.Permissions
import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.flag.Flag
import net.evilblock.cubed.command.data.parameter.Param
import net.evilblock.cubed.util.time.Duration
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

object TempBanCommand {

    @Command(
        names = ["tempban"],
        description = "Temporarily ban a player",
        permission = Permissions.BAN,
        async = true
    )
    @JvmStatic
    fun execute(
        sender: CommandSender,
        @Flag(value = ["s", "silent"], description = "Silently ban the player") silent: Boolean,
        @Param(name = "player") profile: Profile,
        @Param(name = "time") duration: Duration,
        @Param(name = "reason", wildcard = true) reason: String
    ) {
        var issuer: UUID? = null
        if (sender is Player) {
            issuer = sender.uniqueId
        }

        if (profile.getActivePunishment(PunishmentType.BAN) != null) {
            sender.sendMessage("${profile.getUsername()} ${ChatColor.RED}is already banned!")
            return
        }

        if (profile.getActivePunishment(PunishmentType.BLACKLIST) != null) {
            sender.sendMessage("${profile.getUsername()} ${ChatColor.RED}is blacklisted!")
            return
        }

        if (sender is Player && !sender.hasPermission(Permissions.BAN_PERMANENT)) {
            if (duration.isPermanent()) {
                sender.sendMessage("${ChatColor.RED}You don't have permission to create a ban this long. Maximum time allowed: 90 days.")
                return
            }
        }

        val punishment = Punishment(
                uuid = UUID.randomUUID(),
                punishmentType = PunishmentType.BAN
        )

        punishment.reason = reason
        punishment.issuedBy = issuer
        punishment.expiresAt = if (duration.isPermanent()) null else (System.currentTimeMillis() + duration.get())

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