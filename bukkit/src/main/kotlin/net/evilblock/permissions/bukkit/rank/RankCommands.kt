package net.evilblock.permissions.bukkit.rank

import net.evilblock.cubed.command.Command
import net.evilblock.cubed.command.data.parameter.Param
import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.bukkit.BukkitPlugin
import net.evilblock.permissions.bukkit.rank.event.RankUpdateEvent
import net.evilblock.permissions.rank.Rank
import net.evilblock.permissions.bukkit.rank.menu.RankGroupsMenu
import net.evilblock.permissions.bukkit.rank.menu.bulk.BulkSelection
import net.evilblock.permissions.rank.RankHandler
import net.evilblock.permissions.user.UserHandler
import net.evilblock.permissions.util.TimeUtils
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
        val user = UserHandler.getByUniqueId(player.uniqueId)

        if (user == null) {
            player.sendMessage("${ChatColor.RED}Your user data isn't loaded.")
        } else {
            val activeRank = user.getBestDisplayRank()
            val activeGrant = user.getBestDisplayGrant()

            player.sendMessage("${ChatColor.GOLD}You're the ${ChatColor.RESET}${activeRank.getColoredDisplayName()} ${ChatColor.GOLD}rank.")

            if (activeGrant?.expiresAt != null) {
                val formattedTime = TimeUtils.formatIntoDetailedString(((activeGrant.expiresAt!! - System.currentTimeMillis()) / 1000).toInt())
                player.sendMessage("${ChatColor.GOLD}Your rank will expire in ${ChatColor.GREEN}$formattedTime${ChatColor.GOLD}.")
            }
        }
    }

    @Command(
        names = ["rank editor"],
        permission = "op",
        async = true
    )
    @JvmStatic
    fun editor(player: Player) {
        RankGroupsMenu().openMenu(player)
    }

    @Command(
        names = ["rank editor rm_perm"],
        permission = "op",
        async = true
    )
    @JvmStatic
    fun removePerm(player: Player, @Param("rank") rank: Rank, @Param("permissionNode") permission: String) {
        if (rank.permissions.remove(permission)) {
            player.sendMessage("${ChatColor.GREEN}Removed permission node.")

            EvilPermissions.instance.database.saveRank(rank)
            BukkitPlugin.instance.server.pluginManager.callEvent(RankUpdateEvent(rank))
        } else {
            player.sendMessage("${ChatColor.RED}Permission node isn't assigned to rank.")
        }
    }

    @Command(
        names = ["rank editor rm_group"],
        permission = "op",
        async = true
    )
    @JvmStatic fun removeGroup(player: Player, @Param("rank") rank: Rank, @Param("group") group: String) {
        if (rank.groups.remove(group)) {
            player.sendMessage("${ChatColor.AQUA}Removed group ${ChatColor.BLUE}${ChatColor.BOLD}$group ${ChatColor.AQUA}from rank.")

            EvilPermissions.instance.database.saveRank(rank)
            BukkitPlugin.instance.server.pluginManager.callEvent(RankUpdateEvent(rank))
        } else {
            player.sendMessage("${ChatColor.RED}Group ${ChatColor.BLUE}${ChatColor.BOLD}$group ${ChatColor.RED}isn't assigned to rank.")
        }
    }

    @Command(
        names = ["rank editor rm_group_bulk"],
        permission = "op",
        async = true
    )
    @JvmStatic
    fun removeGroupBulk(player: Player, @Param("group") group: String) {
        val bulkSelection = BulkSelection.get(player)
        if (bulkSelection == null) {
            player.sendMessage(BulkSelection.MISSING_SESSION)
            return
        }

        for (rank in (bulkSelection as Set<Rank>)) {
            rank.groups.remove(group)

            EvilPermissions.instance.database.saveRank(rank)
            BukkitPlugin.instance.server.pluginManager.callEvent(RankUpdateEvent(rank))
        }

        player.sendMessage("${ChatColor.AQUA}Removed group ${ChatColor.BLUE}${ChatColor.BOLD}$group ${ChatColor.AQUA}from selected ranks.")
    }

    @Command(
        names = ["rank editor reopen_bulk_sel"],
        permission = "op",
        async = true
    )
    @JvmStatic fun reopenBulkSelection(player: Player) {
        BulkSelection.reopen(player)
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