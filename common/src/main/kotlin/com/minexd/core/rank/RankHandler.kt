package com.minexd.core.rank

import com.minexd.core.CoreXD
import java.util.concurrent.ConcurrentHashMap

object RankHandler {

    private val ranks: MutableMap<String, Rank> = ConcurrentHashMap()

    private val defaultRank = Rank(id = "default").also { rank ->
        rank.displayName = "Default"
        rank.displayOrder = Integer.MAX_VALUE
        rank.default = true
        rank.groups = hashSetOf("GLOBAL")
    }

    fun loadRanks() {
        val request = CoreXD.instance.ranksService.list().execute()
        if (request.isSuccessful) {
            for (rank in request.body()!!) {
                cache(rank)
            }
        }
    }

    fun getRanks(): Collection<Rank> {
        return ranks.values
    }

    fun getRankById(name: String): Rank? {
        return ranks[name.toLowerCase()]
    }

    fun cache(rank: Rank) {
        ranks[rank.id.toLowerCase()] = rank
    }

    fun forget(rank: Rank) {
        ranks.remove(rank.id.toLowerCase())
    }

    fun getDefaultRank(): Rank {
        for (rank in ranks.values) {
            if (rank.default) {
                for (group in rank.groups) {
                    if (CoreXD.instance.plugin.getActiveGroups().contains(group)) {
                        return rank
                    }
                }
            }
        }
        return defaultRank
    }

    fun getRankGroups(): Set<String> {
        val groups = hashSetOf<String>()
        groups.add("GLOBAL")

        for (rank in ranks.values) {
            groups.addAll(rank.groups)
        }

        return groups
    }

    fun getRanksByGroup(group: String): List<Rank> {
        return ranks.values.filter { it.groups.contains(group) }.toList()
    }

    fun performRankUpdate(name: String) {
        val response = CoreXD.instance.ranksService.get(name).execute()
        if (response.isSuccessful) {
            val freshRank = response.body()!!

            val internalRank = getRankById(freshRank.id)
            if (internalRank == null) {
                cache(freshRank)
            } else {
                CoreXD.instance.plugin.getEventHandler().callRankUpdateEvent(internalRank)
            }
        }
    }

}
