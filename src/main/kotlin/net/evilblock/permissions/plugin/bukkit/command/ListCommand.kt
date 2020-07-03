package net.evilblock.permissions.plugin.bukkit.command

import net.evilblock.cubed.command.Command
import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.rank.Rank
import net.evilblock.permissions.user.User
import net.evilblock.permissions.util.Permissions
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object ListCommand {

    @Command(["who", "list"])
    @JvmStatic
    fun execute(sender: CommandSender) {
        val visibleRanks = EvilPermissions.instance.rankHandler.getRanks()
            .filter { canSeeGroup(sender, it) }
            .sortedBy { it.displayOrder }

        val sortedPlayers = Bukkit.getOnlinePlayers()
            .asSequence()
            .filter { canSeePlayer(sender, it) }
            .mapNotNull { EvilPermissions.instance.userHandler.getByUniqueId(it.uniqueId) }
            .sortedBy { it.getBestDisplayRank().displayOrder }

        val styledPlayers = sortedPlayers
            .map { stylePlayer(sender, it) }
            .toMutableList()

        val defaultPlayerListPrefix = EvilPermissions.instance.rankHandler.getDefaultRank().playerListPrefix.replace('&', '§')

        // support non-loaded players (lol)
        val nonLoadedUsers = Bukkit.getOnlinePlayers()
            .filter { EvilPermissions.instance.userHandler.getByUniqueId(it.uniqueId) == null }
            .map { "${ChatColor.RESET}$defaultPlayerListPrefix${it.name}" }

        styledPlayers.addAll(nonLoadedUsers)

        sender.sendMessage(visibleRanks.joinToString(separator = "${ChatColor.GRAY}, ") { styleGroup(sender, it) })

        val onlinePlayers = styledPlayers.size
        val maxPlayers = Bukkit.getMaxPlayers()
        val playerCountPart = "${ChatColor.GRAY}($onlinePlayers/${maxPlayers})"

        sender.sendMessage("$playerCountPart [${ChatColor.RESET}${styledPlayers.joinToString(separator = "${ChatColor.GRAY}, ")}${ChatColor.GRAY}]")
    }

    private fun canSeeGroup(sender: CommandSender, rank: Rank): Boolean {
        if (sender is Player) {
            if (!rank.isActiveOnServer()) {
                return false
            }

            if (rank.isHidden()) {
                if (!sender.hasPermission(Permissions.LIST_VIEW_HIDDEN)) {
                    return false
                }
            }
        }

        return true
    }

    private fun canSeePlayer(sender: CommandSender, player: Player): Boolean {
        return if (sender is Player) {
            sender.canSee(player)
        } else {
            true
        }
    }

    private fun styleGroup(sender: CommandSender, rank: Rank): String {
        if (sender is Player) {
            if (rank.isHidden()) {
                return "${ChatColor.GRAY}${ChatColor.BOLD}${ChatColor.STRIKETHROUGH}${ChatColor.stripColor(rank.displayName)}"
            }
        }

        return "${ChatColor.translateAlternateColorCodes('&', rank.playerListPrefix)}${rank.displayName}"
    }

    private fun stylePlayer(sender: CommandSender, user: User): String {
        if (sender is Player) {
            val otherPlayer = Bukkit.getPlayer(user.uniqueId)
            if (otherPlayer != null && !sender.canSee(otherPlayer) && !sender.isOp) {
                return "${ChatColor.GRAY}${ChatColor.BOLD}${ChatColor.STRIKETHROUGH}${user.getUsername()}"
            }
        }

        return "${ChatColor.RESET}${user.getPlayerListPrefix()}${user.getUsername()}"
    }

}