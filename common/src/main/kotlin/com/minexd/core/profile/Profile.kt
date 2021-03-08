package com.minexd.core.profile

import com.minexd.core.profile.grant.Grant
import com.minexd.core.profile.punishment.Punishment
import com.minexd.core.profile.punishment.PunishmentType
import com.minexd.core.rank.Rank
import com.minexd.core.rank.RankHandler
import java.util.*

abstract class Profile(val uuid: UUID) {

    @Transient var cacheExpiry: Long? = null
    @Transient var requiresApply = false

    val firstSeen: Long = System.currentTimeMillis()
    val identities: MutableSet<String> = hashSetOf()

    val grants: ArrayList<Grant> = arrayListOf()
    val permissions: ArrayList<String> = arrayListOf()

    val punishments: MutableList<Punishment> = ArrayList()

    abstract fun getUsername(): String

    abstract fun apply()

    fun getCompoundedPermissions(): Set<String> {
        val permissions = HashSet<String>()
        permissions.addAll(this.permissions)
        permissions.addAll(RankHandler.getDefaultRank().getCompoundedPermissions())

        getGrantsByGroups(setOf("GLOBAL"))
            .filter { grant -> grant.isActive() }
            .sortedBy { grant -> grant.rank.displayOrder }
            .forEach { grant -> permissions.addAll(grant.rank.getCompoundedPermissions()) }

        return permissions
    }

    fun getGrantById(id: UUID): Grant? {
        for (grant in grants) {
            if (grant.id == id) {
                return grant
            }
        }
        return null
    }

    fun getGrantsByGroups(groups: Set<String>): List<Grant> {
        val grants = arrayListOf<Grant>()
        for (grant in this.grants) {
            for (group in groups) {
                if (grant.rank.groups.contains(group)) {
                    grants.add(grant)
                }
            }
        }
        return grants
    }

    fun getBestDisplayRank(): Rank {
        return getBestDisplayGrant()?.rank ?: RankHandler.getDefaultRank()
    }

    fun getBestDisplayGrant(): Grant? {
        return getGrantsByGroups(setOf("GLOBAL"))
            .filter { grant -> grant.isActive() && !grant.rank.hidden }
            .minBy { grant -> grant.rank.displayOrder }
    }

    fun getColoredUsername(): String {
        return getBestDisplayRank().getColor() + getUsername()
    }

    fun getPunishmentById(id: UUID): Punishment? {
        for (punishment in punishments) {
            if (punishment.uuid == id) {
                return punishment
            }
        }
        return null
    }

    fun getActivePunishment(type: PunishmentType): Punishment? {
        for (punishment in punishments) {
            if (punishment.punishmentType == type && punishment.isActive()) {
                return punishment
            }
        }
        return null
    }

}