package com.minexd.core.proxy.profile

import com.google.gson.JsonObject
import com.minexd.core.CoreXD
import net.evilblock.pidgin.message.handler.IncomingMessageHandler
import net.evilblock.pidgin.message.listener.MessageListener
import com.minexd.core.profile.ProfileHandler
import java.util.*

object ProfileMessageListeners : MessageListener {

    @IncomingMessageHandler("ProfileUpdate")
    fun onProfileUpdate(json: JsonObject) {
        val uuid = UUID.fromString(json.get("UUID").asString)

        try {
            val response = CoreXD.instance.profilesService.get(uuid).execute()
            if (response.isSuccessful) {
                val profile = response.body()!!

                if (ProfileHandler.isProfileCached(uuid)) {
                    ProfileHandler.cacheProfile(profile)
                }

                profile.apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}