package com.minexd.core.bukkit.profile.command.parameter

import net.evilblock.cubed.command.data.parameter.ParameterType
import com.minexd.core.profile.Profile
import com.minexd.core.profile.ProfileHandler
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class ProfileParameterType : ParameterType<Profile?> {

    override fun transform(sender: CommandSender, source: String): Profile? {
        return try {
            if (source == "self" && sender is Player) {
                return ProfileHandler.getProfile(sender.uniqueId)
            }

            ProfileHandler.fetchProfile(source, ensureExists = true).also {  profile ->
                if (profile == null) {
                    sender.sendMessage("${ChatColor.WHITE}$source ${ChatColor.RED}has never played the server before!")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            sender.sendMessage("${ChatColor.RED}Failed to fetch ${ChatColor.WHITE}${source}${ChatColor.RED}'s profile!")
            null
        }
    }

    override fun tabComplete(player: Player, flags: Set<String>, source: String): List<String> {
        val completions = ArrayList<String>()

        Bukkit.getOnlinePlayers().forEach { other ->
            if (player.canSee(other)) {
                completions.add(other.name)
            }
        }

        return completions
    }

}