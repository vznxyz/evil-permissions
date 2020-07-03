package net.evilblock.permissions.rank

import org.bson.Document

object RankSerializer {

    @JvmStatic
    fun serialize(rank: Rank): Document {
        val document = Document("id", rank.id)
        document["id"] = rank.id
        document["displayName"] = rank.displayName
        document["displayOrder"] = rank.displayOrder
        document["prefix"] = rank.prefix
        document["playerListPrefix"] = rank.playerListPrefix
        document["gameColor"] = rank.gameColor
        document["inheritedRanks"] = rank.inheritedRanks.map { it.id }
        document["default"] = rank.default
        document["hidden"] = rank.hidden
        document["groups"] = rank.groups
        document["permissions"] = rank.permissions
        return document
    }

    @JvmStatic
    fun deserialize(document: Document): Rank {
        val rank = Rank(document.getString("id"))
        rank.displayName = document.getString("displayName")
        rank.displayOrder = document.getInteger("displayOrder")!!
        rank.prefix = document.getString("prefix")
        rank.playerListPrefix = document.getString("playerListPrefix")
        rank.gameColor = document.getString("gameColor")
        rank.inheritedRanks = hashSetOf()
        rank.default = document.getBoolean("default")!!
        rank.hidden = document.getBoolean("hidden")

        rank.groups = hashSetOf()
        if (document.containsKey("groups")) {
            rank.groups.addAll(document.getList("groups", String::class.java))
        }

        rank.permissions = hashSetOf()
        if (document.containsKey("permissions")) {
            rank.permissions.addAll(document.getList("permissions", String::class.java))
        }

        return rank
    }

}