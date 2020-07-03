package net.evilblock.permissions.rank

import com.google.gson.JsonObject
import net.evilblock.pidgin.message.handler.IncomingMessageHandler
import net.evilblock.pidgin.message.listener.MessageListener
import net.evilblock.permissions.EvilPermissions

object RankMessageListeners : MessageListener {

    @IncomingMessageHandler("RANK_UPDATE")
    fun onRankUpdate(data: JsonObject) {
        val id = data["id"].asString

        when (data["action"].asString) {
            "UPDATE" -> {
                EvilPermissions.instance.rankHandler.performRankUpdate(id)
            }
            "DELETE" -> {
                val rank = EvilPermissions.instance.rankHandler.getRankById(id)
                if (rank != null) {
                    EvilPermissions.instance.rankHandler.removeRank(rank)
                }
            }
        }
    }

}