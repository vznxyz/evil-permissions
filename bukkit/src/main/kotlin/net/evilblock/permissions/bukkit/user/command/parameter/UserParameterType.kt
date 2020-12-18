package net.evilblock.permissions.bukkit.user.command.parameter

import net.evilblock.cubed.command.data.parameter.ParameterType
import net.evilblock.permissions.bukkit.user.BukkitUser
import net.evilblock.permissions.user.User
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

object UserParameterType : ParameterType<User?> {

    override fun transform(sender: CommandSender, source: String): User? {
        return BukkitUser.loadOrCreateByUsername(source)
    }

    override fun tabComplete(sender: Player, flags: Set<String>, source: String): List<String> {
        val completions = ArrayList<String>()

        Bukkit.getOnlinePlayers().forEach { player ->
            if (sender.canSee(player)) {
                completions.add(player.name)
            }
        }

        return completions
    }

}