package com.minexd.core.bukkit.rank

import net.evilblock.cubed.command.data.parameter.ParameterType
import com.minexd.core.rank.Rank
import com.minexd.core.rank.RankHandler
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.lang.Exception

class RankParameterType : ParameterType<Rank?> {

    override fun transform(sender: CommandSender, source: String): Rank? {
        try {
            val rank = RankHandler.getRankById(source)
            if (rank != null) {
                return rank
            }
        } catch (e: Exception) {
            sender.sendMessage(ChatColor.RED.toString() + "Failed to parse rank unique ID.")
            return null
        }

        sender.sendMessage(ChatColor.RED.toString() + "Couldn't find a rank by that unique ID.")
        return null
    }

    override fun tabComplete(player: Player, flags: Set<String>, source: String): List<String> {
        return emptyList()
    }

}