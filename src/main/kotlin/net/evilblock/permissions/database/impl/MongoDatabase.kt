package net.evilblock.permissions.database.impl

import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.ReplaceOptions
import net.evilblock.cubed.Cubed
import net.evilblock.pidgin.message.Message
import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.database.Database
import net.evilblock.permissions.plugin.bukkit.BukkitPlugin
import net.evilblock.permissions.rank.Rank
import net.evilblock.permissions.user.User
import net.evilblock.permissions.user.UserSerializer
import org.bson.Document
import java.lang.RuntimeException
import java.util.*

class MongoDatabase : Database {

    private val client: MongoClient = EvilPermissions.instance.plugin.getMongoClient()
    private val ranksCollection: MongoCollection<Document> = client.getDatabase(BukkitPlugin.instance.config.getString("database-name")).getCollection("ranks")
    private val usersCollection: MongoCollection<Document> = client.getDatabase(BukkitPlugin.instance.config.getString("database-name")).getCollection("users")

    init {
        ranksCollection.createIndex(Document("id", 1))
        usersCollection.createIndex(Document("id", 1))
    }

    override fun fetchRank(id: String): Rank? {
        val document = ranksCollection.find(Document("id", id)).first()
        if (document != null) {
            try {
                val rank = Cubed.gson.fromJson(document.toJson(), Rank::class.java)

                // keep this check, it allows database compatibility between versions
                rank.runCompatibilityFix()

                for (inheritedRankId in document.getList("inheritedRanks", String::class.java)) {
                    val inheritedRank = EvilPermissions.instance.rankHandler.getRankById(inheritedRankId)
                    if (inheritedRank != null) {
                        rank.inheritedRanks.add(inheritedRank)
                    }
                }
            } catch (e: Exception) {
                if (document.containsKey("id")) {
                    throw RuntimeException("Failed to load rank from document: " + document.getString("id"), e)
                } else {
                    throw RuntimeException("Failed to load rank from document: Couldn't identify rank ID", e)
                }
            }
        }
        return null
    }

    override fun fetchRanks(): List<Rank> {
        val fetchedRanks = hashMapOf<Rank, List<String>>()

        // fetch documents and deserialize into rank objects
        for (document in ranksCollection.find()) {
            try {
                val rank = Cubed.gson.fromJson(document.toJson(), Rank::class.java)

                // keep this check, it allows database compatibility between versions
                rank.runCompatibilityFix()

                fetchedRanks[rank] = document.getList("inheritedRanks", String::class.java)
            } catch (e: Exception) {
                if (document.containsKey("id")) {
                    throw RuntimeException("Failed to load rank from document: " + document.getString("id"), e)
                } else {
                    throw RuntimeException("Failed to load rank from document: Couldn't identify rank ID", e)
                }
            }
        }

        // copy data to existing references
        for (deserializedRank in fetchedRanks.keys) {
            val internalRank: Rank = EvilPermissions.instance.rankHandler.getRankById(deserializedRank.id) ?: deserializedRank

            if (deserializedRank !== internalRank) {
                internalRank.copyFrom(deserializedRank)
            }
        }

        // setup inheritance
        for (rankEntry in fetchedRanks) {
            var internalRank: Rank? = EvilPermissions.instance.rankHandler.getRankById(rankEntry.key.id)

            if (internalRank == null) {
                internalRank = rankEntry.key
            }

            for (inheritedRankId in rankEntry.value) {
                val inheritedRank = fetchedRanks.keys.firstOrNull { it.id == inheritedRankId }
                if (inheritedRank != null) {
                    internalRank.inheritedRanks.add(inheritedRank)
                }
            }
        }

        return fetchedRanks.keys.toList()
    }

    override fun saveRank(rank: Rank) {
        ranksCollection.replaceOne(Document("id", rank.id), Document.parse(Cubed.gson.toJson(rank)), ReplaceOptions().upsert(true))
        EvilPermissions.instance.pidgin.sendMessage(Message("RANK_UPDATE", mapOf("id" to rank.id, "action" to "UPDATE")))
    }

    override fun deleteRank(rank: Rank) {
        ranksCollection.deleteOne(Document("id", rank.id))
        EvilPermissions.instance.pidgin.sendMessage(Message("RANK_UPDATE", mapOf("id" to rank.id, "action" to "DELETE")))
    }

    override fun fetchUser(uniqueId: UUID): User? {
        val document = usersCollection.find(Document("id", uniqueId.toString())).first()

        if (document != null) {
            return UserSerializer.deserialize(document)
        }

        return null
    }

    override fun saveUser(user: User) {
        usersCollection.replaceOne(Document("id", user.uniqueId.toString()), UserSerializer.serialize(user), ReplaceOptions().upsert(true))
    }

    override fun resetUsersOneLetterRanks() {
        for (document in usersCollection.find()) {
            val user = UserSerializer.deserialize(document)
            if (user.grants.removeIf { it.rank.id.length == 1 }) {
                saveUser(user)
            }
        }
    }

}