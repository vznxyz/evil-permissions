package com.minexd.core.bukkit.profile.listener

import com.google.gson.JsonObject
import com.minexd.core.CoreXD
import com.minexd.core.profile.ProfileHandler
import com.minexd.core.profile.punishment.Punishment
import com.minexd.core.profile.punishment.PunishmentType
import mkremins.fanciful.FancyMessage
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.time.TimeUtil
import net.evilblock.pidgin.message.handler.IncomingMessageHandler
import net.evilblock.pidgin.message.listener.MessageListener
import org.apache.commons.lang.StringUtils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import java.util.*
import java.util.concurrent.TimeUnit

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

    @IncomingMessageHandler("ExecutePunishment")
    fun onExecutePunishment(json: JsonObject) {
        val target = UUID.fromString(json.get("Target").asString)
        val targetName = json.get("TargetName").asString

        val executor = UUID.fromString(json.get("Executor").asString)
        val executorName = json.get("ExecutorName").asString

        val punishment = Serializers.gson.fromJson(json.get("Punishment").asJsonObject, Punishment::class.java)

        val sharedAccounts = if (json.has("SharedAccounts")) {
            json.get("SharedAccounts").asJsonArray.map { UUID.fromString(it.asString) }
        } else {
            null
        }

        val silent = json.get("Silent").asBoolean

        executePunishment(target, targetName, executor, executorName, punishment, sharedAccounts, silent)
    }

    private fun executePunishment(target: UUID, targetName: String, executor: UUID, executorName: String, punishment: Punishment, sharedAccounts: List<UUID>?, silent: Boolean) {
        val tooltip = arrayListOf<FancyMessage>()
        if (punishment.pardoned) {
            tooltip.add(FancyMessage("Pardoned by: ")
                    .color(ChatColor.YELLOW)
                    .then(executorName))

            tooltip.add(FancyMessage("Reason: ")
                    .color(ChatColor.YELLOW)
                    .then(punishment.reason)
                    .color(ChatColor.RED))
        } else {
            val durationText = if (punishment.expiresAt == null) {
                "Permanent"
            } else {
                TimeUtil.formatIntoDetailedString(TimeUnit.MILLISECONDS.toSeconds(punishment.expiresAt!! - System.currentTimeMillis()).toInt())
            }

            tooltip.add(FancyMessage("Issued by: ")
                    .color(ChatColor.YELLOW)
                    .then(executorName))

            tooltip.add(FancyMessage("Duration: ")
                    .color(ChatColor.YELLOW)
                    .then(durationText)
                    .color(ChatColor.RED))

            tooltip.add(FancyMessage("Reason: ")
                    .color(ChatColor.YELLOW)
                    .then(punishment.reason)
                    .color(ChatColor.RED))
        }

        val message = buildString {
            if (silent) {
                append("${ChatColor.GRAY}(Silent) ")
            }

            append(targetName)
            append(" ${ChatColor.GREEN}was ")

            if (punishment.pardoned) {
                append("un${punishment.punishmentType.action}")
            } else {
                if (punishment.punishmentType.temporal) {
                    if (punishment.expiresAt == null) {
                        append("permanently ")
                    } else {
                        append("temporarily ")
                    }
                }

                append(punishment.punishmentType.action)
            }

            append(" by ")
            append(executorName)
        }

        val staffMessage = FancyMessage(message).formattedTooltip(tooltip)

        for (onlinePlayer in Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.hasPermission(punishment.punishmentType.getViewPermission())) {
                staffMessage.send(onlinePlayer)
            } else if (!silent) {
                onlinePlayer.sendMessage(message)
            }
        }

        if (!punishment.pardoned) {
            if (punishment.punishmentType.kick) {
                Bukkit.getPlayer(target)?.also { player ->
                    player.kickPlayer(ChatColor.translateAlternateColorCodes('&', StringUtils.join(punishment.punishmentType.kickMessages, "\n")))
                }

                if (sharedAccounts != null) {
                    for (alt in sharedAccounts) {
                        Bukkit.getPlayer(alt)?.also { player ->
                            player.kickPlayer(ChatColor.translateAlternateColorCodes('&', StringUtils.join(punishment.punishmentType.kickMessages, "\n")))
                        }
                    }
                }
            } else if (punishment.punishmentType == PunishmentType.WARN) {
                val player = Bukkit.getPlayer(target)
                if (player != null) {
                    player.sendMessage("${ChatColor.RED}${ChatColor.BOLD}You have been warned!")
                    player.sendMessage("${ChatColor.RED}Reason: ${punishment.reason}")
                }
            }
        }
    }

}