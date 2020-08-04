package net.evilblock.permissions.rank

import net.evilblock.permissions.EvilPermissions
import java.util.*

object RankHandler {

    val IGNORE: Rank = Rank("**IGNORE**")

    private val ranks = ArrayList<Rank>()

    private val defaultRank = Rank(
        id = "default",
        displayName = "Default",
        displayColor = "${Rank.COLOR_CHAR}f",
        displayOrder = Integer.MAX_VALUE,
        prefix = "",
        playerListPrefix = "",
        displayNamePrefix = "",
        permissions = hashSetOf(),
        inheritedRanks = hashSetOf(),
        default = true,
        hidden = false,
        groups = hashSetOf("GLOBAL")
    )

    fun loadRanks() {
        for (fetchedRank in EvilPermissions.instance.database.fetchRanks()) {
            ranks.add(fetchedRank)
        }
    }

    fun getRanks(): List<Rank> {
        return ArrayList(ranks)
    }

    fun getDefaultRank(): Rank {
        for (rank in ranks) {
            if (rank.default) {
                for (group in rank.groups) {
                    if (EvilPermissions.instance.plugin.getActiveGroups().contains(group)) {
                        return rank
                    }
                }
            }
        }
        return defaultRank
    }

    fun getRankById(name: String): Rank? {
        for (rank in ranks) {
            if (rank.id.equals(name, ignoreCase = true)) {
                return rank
            }
        }
        return null
    }

    fun createRank(name: String): Rank {
        val rank = Rank(name)
        ranks.add(rank)
        return rank
    }

    fun removeRank(rank: Rank) {
        ranks.remove(rank)
    }

    fun getRankGroups(): Set<String> {
        val groups = hashSetOf<String>()
        groups.add("GLOBAL")

        for (rank in ranks) {
            groups.addAll(rank.groups)
        }

        return groups
    }

    fun getRanksByGroup(group: String): List<Rank> {
        return ranks.filter { it.groups.contains(group) }.toList()
    }

    fun performRankUpdate(name: String) {
        val serializedRank = EvilPermissions.instance.database.fetchRank(name)
        if (serializedRank != null) {
            val internalRank = getRankById(serializedRank.id)
            if (internalRank != null) {
                EvilPermissions.instance.plugin.getEventHandler().callRankUpdateEvent(internalRank)
            }
        }
    }

}
