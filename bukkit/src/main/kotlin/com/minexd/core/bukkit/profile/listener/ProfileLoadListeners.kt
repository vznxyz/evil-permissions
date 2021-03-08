package com.minexd.core.bukkit.profile.listener

import com.minexd.core.CoreXD
import net.evilblock.cubed.logging.ErrorHandler
import net.evilblock.cubed.util.bukkit.Tasks
import com.minexd.core.profile.ProfileHandler
import com.minexd.core.profile.punishment.Punishment
import com.minexd.core.profile.punishment.PunishmentType
import net.evilblock.cubed.Cubed
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

object ProfileLoadListeners : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onAsyncPlayerPreLoginEvent(event: AsyncPlayerPreLoginEvent) {
        // we need to check if player is still logged in when receiving another login attempt
        // this happens when a player using a custom client that can access the server list while in-game (and reconnecting)
        val player = Bukkit.getPlayer(event.uniqueId)
        if (player != null && player.isOnline) {
            event.loginResult = AsyncPlayerPreLoginEvent.Result.KICK_OTHER
            event.kickMessage = "${ChatColor.RED}You tried to login too quickly after disconnecting.\nTry again in a few seconds."

            Tasks.sync {
                player.kickPlayer("${ChatColor.RED}Duplicate login kick")
            }

            return
        }

        try {
            val response = CoreXD.instance.profilesService.login(event.uniqueId, mapOf("ipAddress" to event.address.hostAddress)).execute()
            if (response.isSuccessful) {
                val profile = response.body()!!
                profile.cacheExpiry = null
                ProfileHandler.cacheProfile(profile)
            } else {
                throw IllegalStateException("Failed to load profile: ${response.errorBody()!!.string()}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loginError(event, e)
            return
        }

        try {
            val response = CoreXD.instance.profilesService.getSuspensionInfo(event.uniqueId).execute()
            if (response.isSuccessful) {
                val data = response.body()!!

                val punishment = Serializers.gson.fromJson(data["punishment"].asJsonObject, Punishment::class.java)
                val relation = UUID.fromString(data["relation"].asString)

                val kickMessage = buildString {
                    append("\n")

                    if (punishment.punishmentType == PunishmentType.BAN) {
                        append("${ChatColor.RED}Your account has been suspended from MineXD!")
                    } else {
                        append("${ChatColor.RED}Your account has been blacklisted from MineXD!")
                    }

                    append("\n${ChatColor.RED}Reason: ${ChatColor.YELLOW}Hidden")
                    append("\n${ChatColor.RED}Expires: ${ChatColor.YELLOW}")

                    if (punishment.isPermanent()) {
                        append("Never")
                    } else {
                        append(TimeUtil.formatIntoAbbreviatedString((punishment.getRemainingTime() / 1000.0).toInt()))
                    }

                    if (event.uniqueId != relation) {
                        val relatedUsername = Cubed.instance.uuidCache.name(relation)
                        append("\n\nThis punishment is in relation to ${ChatColor.YELLOW}$relatedUsername${ChatColor.RED}.")
                    }

                    append("\n${ChatColor.RED}www.minexd.com")
                }

                event.loginResult = AsyncPlayerPreLoginEvent.Result.KICK_BANNED
                event.kickMessage = kickMessage
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loginError(event, e)
        }
    }

    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        if (ProfileHandler.isProfileCached(event.player.uniqueId)) {
            Tasks.async {
                ProfileHandler.getProfile(event.player.uniqueId).apply()
            }
        }
    }

    @EventHandler
    fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        ProfileHandler.forgetProfile(event.player.uniqueId)
    }

    private fun loginError(event: AsyncPlayerPreLoginEvent, exception: Exception) {
        val eventDetails = mapOf(
                "PlayerName" to event.name,
                "PlayerUUID" to event.uniqueId.toString(),
                "PlayerIP" to event.address.hostAddress
        )

        val logId = ErrorHandler.generateErrorLog("LoginError", eventDetails, exception)

        val kickMessage = StringBuilder()
                .append("${ChatColor.RED}Failed to load your profile!")
                .append("\n")
                .append("${ChatColor.GRAY}Check our status page to see")
                .append("\n")
                .append("${ChatColor.GRAY}if we're having problems.")
                .append("\n")
                .append("${ChatColor.GRAY}Error ID: ${ChatColor.WHITE}$logId")

        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, kickMessage.toString())
    }

}