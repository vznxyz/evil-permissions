package com.minexd.core.bukkit.profile.grant.command

import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.flag.Flag
import net.evilblock.cubed.command.data.parameter.Param
import com.minexd.core.CoreXD
import com.minexd.core.rank.Rank
import com.minexd.core.bukkit.profile.grant.menu.GrantMenu
import com.minexd.core.bukkit.profile.grant.menu.GrantsMenu
import com.minexd.core.profile.Profile
import com.minexd.core.profile.grant.Grant
import com.minexd.core.util.Permissions
import com.minexd.core.util.TimeUtils
import net.evilblock.cubed.util.time.Duration
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object GrantCommands {

    @Command(
        names = ["grant"],
        description = "Grant a player a rank",
        permission = Permissions.GRANT,
        async = true
    )
    @JvmStatic
    fun grant(sender: CommandSender, @Param("target") target: Profile) {
        if (sender is Player) {
            GrantMenu(target).openMenu(sender)
        } else {
            sender.sendMessage("${ChatColor.RED}You must be a player to use this command!")
        }
    }

    @Command(
        names = ["ogrant"],
        description = "Grant a player a rank",
        permission = "op",
        async = true
    )
    @JvmStatic
    fun ogrant(
            sender: CommandSender,
            @Param("target") target: Profile,
            @Param("rank") rank: Rank,
            @Param("duration") duration: Duration,
            @Param("reason", wildcard = true) reason: String
    ) {
        if (sender is Player) {
            sender.sendMessage("${ChatColor.RED}This command can only be executed by console!")
            return
        }

        val issuedBy = if (sender is Player) {
            sender.uniqueId
        } else {
            null
        }

        val grant = Grant(
                rank = rank,
                issuedBy = issuedBy,
                reason = reason,
                expiresAt = if (duration.isPermanent()) null else (System.currentTimeMillis() + duration.get())
        )

        try {
            val response = CoreXD.instance.profilesService.grant(target.uuid, grant).execute()
            if (response.isSuccessful) {
                val period = if (grant.expiresAt == null) {
                    "forever"
                } else {
                    TimeUtils.formatIntoDetailedString(((grant.expiresAt!! - System.currentTimeMillis()) / 1000).toInt())
                }

                sender.sendMessage(buildString {
                    append("${ChatColor.GREEN}You've granted ")
                    append(target.getColoredUsername())
                    append(" ${ChatColor.GREEN}the ")
                    append(grant.rank.getColoredDisplayName())
                    append(" ${ChatColor.GREEN}rank for a period of ")
                    append("${ChatColor.AQUA}$period")
                    append("${ChatColor.GREEN}!")
                })
            } else {
                sender.sendMessage("${ChatColor.RED}${response.errorBody()!!.string()}!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Command(
        names = ["grants"],
        description = "View a player's grant history",
        permission = Permissions.GRANTS_HISTORY,
        async = true
    )
    @JvmStatic
    fun grants(player: Player, @Param("target") target: Profile) {
        GrantsMenu(target).openMenu(player)
    }

    @Command(
            names = ["grant-perm", "grant-permission", "grantperm", "grantpermission"],
            description = "Grant a player a permission",
            permission = Permissions.GRANT_PERMISSIONS,
            async = true
    )
    @JvmStatic
    fun grantPermission(
            sender: CommandSender,
            @Flag(value = ["r", "remove"], description = "Removes the permission") remove: Boolean,
            @Param("target") target: Profile,
            @Param("permission") permission: String
    ) {
        if (remove && !target.permissions.contains(permission)) {
            sender.sendMessage("${ChatColor.WHITE}${target.getUsername()} ${ChatColor.RED}hasn't been assigned the ${ChatColor.WHITE}${ChatColor.BOLD}$permission ${ChatColor.RED}permission!")
            return
        } else if (!remove && target.permissions.contains(permission)) {
            sender.sendMessage("${ChatColor.WHITE}${target.getUsername()} ${ChatColor.RED}has already been assigned the ${ChatColor.WHITE}${ChatColor.BOLD}$permission ${ChatColor.RED}permission!")
            return
        }

        val call = if (remove) {
            CoreXD.instance.profilesService.revokePermission(target.uuid, permission)
        } else {
            CoreXD.instance.profilesService.addPermission(target.uuid, permission)
        }

        val response = call.execute()
        if (response.isSuccessful) {
            if (remove) {
                sender.sendMessage("${ChatColor.GREEN}Successfully revoked permission ${ChatColor.WHITE}${ChatColor.BOLD}$permission ${ChatColor.RED}from ${ChatColor.WHITE}${target.getUsername()}${ChatColor.GREEN}!")
            } else {
                sender.sendMessage("${ChatColor.GREEN}Successfully granted permission ${ChatColor.WHITE}${ChatColor.BOLD}$permission ${ChatColor.GREEN}to ${ChatColor.WHITE}${target.getUsername()}${ChatColor.GREEN}!")
            }
        }
    }

    @Command(
        names = ["hasperm", "has-perm"],
        permission = "op",
        async = true
    )
    @JvmStatic
    fun hasPerm(sender: CommandSender, @Param("player") player: Player, @Param("permission") permission: String) {
        if (player.hasPermission(permission)) {
            sender.sendMessage("${ChatColor.GREEN}Has permission!")
        } else {
            sender.sendMessage("${ChatColor.RED}Does not have permission!")
        }
    }

}