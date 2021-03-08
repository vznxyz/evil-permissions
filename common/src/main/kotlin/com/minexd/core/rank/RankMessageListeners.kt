package com.minexd.core.rank

import com.google.gson.JsonObject
import net.evilblock.pidgin.message.handler.IncomingMessageHandler
import net.evilblock.pidgin.message.listener.MessageListener
import com.minexd.core.CoreXD

object RankMessageListeners : MessageListener {

    @IncomingMessageHandler("RankUpdate")
    fun onRankUpdate(data: JsonObject) {
        val id = data["id"].asString

        val update = try {
            RankUpdate.valueOf(data["update"].asString)
        } catch (e: Exception) {
            CoreXD.instance.plugin.getLogger().severe("Failed to handle rank update:")
            e.printStackTrace()
        }

        when (update) {
            RankUpdate.UPDATE -> {
                RankHandler.performRankUpdate(id)
            }
            RankUpdate.DELETE -> {
                val rank = RankHandler.getRankById(id)
                if (rank != null) {
                    RankHandler.forget(rank)
                }
            }
        }
    }

}