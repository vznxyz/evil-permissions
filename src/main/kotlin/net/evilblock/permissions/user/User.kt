package net.evilblock.permissions.user

import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.rank.Rank
import net.evilblock.permissions.rank.RankHandler
import net.evilblock.permissions.user.grant.Grant
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

abstract class User(val uniqueId: UUID) {

    val grants: ArrayList<Grant> = arrayListOf()
    val permissions: ArrayList<String> = arrayListOf()
    var requiresSave = false
    var requiresApply = false

    fun getCompoundedPermissions(): Set<String> {
        val permissions = HashSet<String>()
        permissions.addAll(this.permissions)
        permissions.addAll(RankHandler.getDefaultRank().getCompoundedPermissions())

        getGrantsByGroups(EvilPermissions.instance.plugin.getActiveGroups())
            .filter { grant -> grant.isActive() }
            .sortedBy { grant -> grant.rank.displayOrder }
            .forEach { grant -> permissions.addAll(grant.rank.getCompoundedPermissions()) }

        return permissions
    }

    fun getMappedCompoundedPermissions(): Map<Rank, Set<String>> {
        val defaultRank = RankHandler.getDefaultRank()

        val map: HashMap<Rank, HashSet<String>> = hashMapOf()
        defaultRank.getMappedCompoundedPermissions(map)

        getGrantsByGroups(EvilPermissions.instance.plugin.getActiveGroups())
            .filter { grant -> grant.isActive() }
            .sortedBy { grant -> grant.rank.displayOrder }
            .forEach { grant -> grant.rank.getMappedCompoundedPermissions(map) }

        return map
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
        return getGrantsByGroups(EvilPermissions.instance.plugin.getActiveGroups())
            .filter { grant -> grant.isActive() && !grant.rank.hidden }
            .minBy { grant -> grant.rank.displayOrder }
    }

    fun getPlayerListPrefix(): String {
        return getBestDisplayRank().playerListPrefix.replace('&', '\u00A7')
    }

    abstract fun getUsername(): String

    abstract fun apply()

}