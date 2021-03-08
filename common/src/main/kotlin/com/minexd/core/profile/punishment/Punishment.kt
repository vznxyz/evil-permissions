package com.minexd.core.profile.punishment

import com.minexd.core.util.TimeUtils
import java.util.*

class Punishment(val uuid: UUID = UUID.randomUUID(), val punishmentType: PunishmentType) {

    var reason: String = ""
    var issuedBy: UUID? = null
    val issuedAt: Long = System.currentTimeMillis()
    var expiresAt: Long? = null

    var pardoned: Boolean = false
    var pardonReason: String? = null
    var pardonedBy: UUID? = null
    var pardonedAt: Long? = null

    fun isActive(): Boolean {
        return !pardoned && (expiresAt == null || System.currentTimeMillis() < expiresAt!!)
    }

    fun isPermanent(): Boolean {
        return expiresAt == null
    }

    fun getDuration(): Long {
        return expiresAt!! - issuedAt
    }

    fun getRemainingTime(): Long {
        return expiresAt!! - System.currentTimeMillis()
    }

    fun getFormattedRemainingTime(): String {
        return TimeUtils.formatIntoDetailedString((getRemainingTime() / 1000.0).toInt())
    }

}