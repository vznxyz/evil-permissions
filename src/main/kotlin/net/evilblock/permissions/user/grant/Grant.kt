package net.evilblock.permissions.user.grant

import net.evilblock.permissions.rank.Rank
import net.evilblock.permissions.util.Permissions
import org.bukkit.entity.Player
import java.util.*

open class Grant(val id: UUID = UUID.randomUUID()) {

    lateinit var rank: Rank
    lateinit var reason: String
    var issuedBy: UUID? = null
    var issuedAt: Long = -1
    var expiresAt: Long? = null
    var removalReason: String? = null
    var removedBy: UUID? = null
    var removedAt: Long? = null

    fun isActive(): Boolean {
        return removedAt == null && (expiresAt == null || System.currentTimeMillis() < expiresAt!!)
    }

    fun canBeRemovedBy(remover: Player): Boolean {
        if (!remover.hasPermission(Permissions.GRANT_REMOVE)) {
            return false
        }

        if (remover.hasPermission(Permissions.GRANT_REMOVE + ".*")) {
            return true
        }

        if (remover.hasPermission(Permissions.GRANT_REMOVE + ".${rank.id}")) {
            return true
        }

        return false
    }

}