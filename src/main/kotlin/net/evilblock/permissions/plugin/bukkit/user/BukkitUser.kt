package net.evilblock.permissions.plugin.bukkit.user

import net.evilblock.cubed.Cubed
import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.plugin.bukkit.BukkitPlugin
import net.evilblock.permissions.user.User
import org.bukkit.Bukkit
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.permissions.PermissionAttachment
import java.util.*

class BukkitUser(uuid: UUID) : User(uuid) {

    private var attachment: PermissionAttachment? = null
    private var applying: Boolean = false

    override fun getUsername(): String {
        return Cubed.instance.uuidCache.name(uniqueId)
    }

    override fun apply() {
        if (Bukkit.isPrimaryThread()) {
            throw IllegalStateException("Cannot apply permissions to player on main thread")
        }

        synchronized(this) {
            if (applying) {
                return
            }

            applying = true

            try {
                val player = Bukkit.getPlayer(uniqueId) ?: return
                val plugin = EvilPermissions.instance.plugin as BukkitPlugin

                if (attachment == null) {
                    attachment = player.addAttachment(plugin)
                } else {
                    val permissions = attachment!!.permissions.keys.toList()
                    for (permission in permissions) {
                        attachment!!.unsetPermission(permission)
                    }
                }

                for (permission in getCompoundedPermissions()) {
                    if (permission.startsWith("-")) {
                        attachment!!.setPermission(permission.substring(1), false)
                    } else {
                        attachment!!.setPermission(permission, true)
                    }
                }

                val bestDisplayRank = getBestDisplayRank()

                player.setMetadata("EP_PLAYER_LIST_NAME", FixedMetadataValue(BukkitPlugin.instance, bestDisplayRank.playerListPrefix))

                if (plugin.config.getBoolean("players.set-display-name", true)) {
                    player.displayName = bestDisplayRank
                        .processPlaceholders(plugin.config.getString("players.display-name-format"))
                        .replace("{playerName}", player.name)
                }

                if (plugin.config.getBoolean("players.set-player-list-name", true)) {
                    var playerListName = bestDisplayRank
                        .processPlaceholders(plugin.config.getString("players.player-list-name-format"))
                        .replace("{playerName}", player.name)

                    if (playerListName.length > 16) {
                        playerListName = playerListName.substring(0, 15)
                    }

                    player.playerListName = playerListName
                }
            } catch (e: Exception) {
                BukkitPlugin.instance.logger.severe("Failed to apply user attributes to player ${getUsername()}:")
                e.printStackTrace()
            }

            applying = false
        }
    }

}