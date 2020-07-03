package net.evilblock.permissions.plugin.bukkit.user.grant

import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.flag.Flag
import net.evilblock.cubed.command.data.parameter.Param
import net.evilblock.pidgin.message.Message
import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.rank.Rank
import net.evilblock.permissions.user.User
import net.evilblock.permissions.plugin.bukkit.user.grant.menu.GrantMenu
import net.evilblock.permissions.plugin.bukkit.user.grant.menu.GrantsMenu
import net.evilblock.permissions.rank.RankHandler
import net.evilblock.permissions.user.grant.Grant
import net.evilblock.permissions.util.DateUtil
import net.evilblock.permissions.util.Permissions
import net.evilblock.permissions.util.TimeUtils
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.lang.Exception
import java.util.*

object GrantCommands {

    @Command(
        names = ["grantperm"],
        description = "Grant a player a permission",
        permission = Permissions.GRANT_PERMISSION,
        async = true
    )
    @JvmStatic
    fun grantPerm(
        sender: CommandSender,
        @Flag(value = ["r", "remove"], description = "Removes the permission") remove: Boolean,
        @Param("target") target: User,
        @Param("permission") permission: String
    ) {
        val update = if (remove) {
            target.permissions.remove(permission)
        } else {
            target.permissions.add(permission)
        }

        if (update) {
            target.apply()

            EvilPermissions.instance.database.saveUser(target)

            if (remove) {
                sender.sendMessage("${ChatColor.GREEN}Removed permission from user.")
            } else {
                sender.sendMessage("${ChatColor.GREEN}Added permission to user.")
            }
        } else {
            sender.sendMessage("${ChatColor.RED}No changes made to the user's permissions.")
        }
    }

    @Command(
        names = ["grant"],
        description = "Grant a player a rank",
        permission = Permissions.GRANT,
        async = true
    )
    @JvmStatic
    fun grant(
        sender: CommandSender,
        @Flag(value = ["r", "remove"], description = "Removes the rank from the player") remove: Boolean,
        @Param("target") target: User,
        @Param("rank", defaultValue = "**none**") rank: Rank
    ) {
        if (remove || rank == RankHandler.IGNORE) {
            if (sender is Player) {
                GrantMenu(target).openMenu(sender)
                return
            }
        }

        if (remove) {
            val update = target.grants.removeIf { it.rank == rank }

            if (update) {
                target.apply()

                EvilPermissions.instance.database.saveUser(target)

                sender.sendMessage("${ChatColor.GREEN}Removed rank from user.")
            } else {
                sender.sendMessage("${ChatColor.RED}No changes made to the user's ranks.")
            }
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
        @Param("target") target: User,
        @Param("rank") rank: Rank,
        @Param("duration") duration: String,
        @Param("reason", wildcard = true) reason: String
    ) {
        if (sender is Player) {
            sender.sendMessage("${ChatColor.RED}This command can only be executed by console.")
            return
        }

        for (grant in target.grants) {
            if (grant.isActive()) {
                if (grant.rank == rank) {
                    sender.sendMessage("${ChatColor.RED}That player has already been assigned that rank.")
                    return
                }
            }
        }

        var perm = false
        var expiresAt = 0L
        var issuer: UUID? = null

        if (sender is Player) {
            issuer = sender.uniqueId
        }

        if (duration.toLowerCase() == "perm") {
            perm = true
        }

        if (!(perm)) {
            try {
                expiresAt = System.currentTimeMillis() - DateUtil.parseDateDiff(duration, false)
            } catch (exception: Exception) {
                sender.sendMessage("${ChatColor.RED}Invalid duration.")
                return
            }
        }

        val grant = Grant()
        grant.rank = rank
        grant.reason = reason
        grant.issuedBy = issuer
        grant.issuedAt = System.currentTimeMillis()

        if (!perm) {
            grant.expiresAt = expiresAt + System.currentTimeMillis()
        }

        target.grants.add(grant)

        EvilPermissions.instance.database.saveUser(target)
        EvilPermissions.instance.pidgin.sendMessage(Message("GRANT_UPDATE", mapOf("uniqueId" to target.uniqueId.toString(), "grant" to grant.id.toString())))

        val period = if (grant.expiresAt == null) {
            "forever"
        } else {
            TimeUtils.formatIntoDetailedString(((grant.expiresAt!! - System.currentTimeMillis()) / 1000).toInt())
        }

        sender.sendMessage("${ChatColor.GREEN}You've granted ${target.getPlayerListPrefix() + target.getUsername()} ${ChatColor.GREEN}the ${grant.rank.getColoredDisplayName()} ${ChatColor.GREEN}rank for a period of ${ChatColor.YELLOW}$period${ChatColor.GREEN}.")
    }

    @Command(
        names = ["grants"],
        description = "View a player's grant history",
        permission = Permissions.GRANT_HISTORY,
        async = true
    )
    @JvmStatic
    fun grants(player: Player, @Param("target") target: User) {
        GrantsMenu(target).openMenu(player)
    }

    @Command(
        names = ["pdebug"],
        permission = "op",
        async = true
    )
    @JvmStatic
    fun debug(player: Player, @Param("target") target: User) {
        val mappedPerms = target.getMappedCompoundedPermissions()

        player.sendMessage("${ChatColor.YELLOW}${ChatColor.BOLD}Permissions assigned by ranks")

        for ((rank, perms) in mappedPerms) {
            player.sendMessage("${rank.getColoredDisplayName()} ${ChatColor.RESET}- ${perms.joinToString()}")
        }

        player.sendMessage(" ")
        player.sendMessage("${ChatColor.YELLOW}${ChatColor.BOLD}Permissions assigned to user")
        player.sendMessage(target.permissions.joinToString())
    }

    @Command(
        names = ["hasperm"],
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