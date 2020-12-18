package net.evilblock.permissions.database

import net.evilblock.permissions.database.result.IssuedByQueryResult
import net.evilblock.permissions.rank.Rank
import net.evilblock.permissions.user.User
import java.util.*

interface Database {

    /**
     * Fetches a [Rank] from the database by the given [id].
     */
    fun fetchRank(id: String): Rank?

    /**
     * Fetches a list of [Rank]s from the database.
     */
    fun fetchRanks(): List<Rank>

    /**
     * Saves a given [rank] to the database.
     */
    fun saveRank(rank: Rank)

    /**
     * Deletes a given [rank] from the database.
     */
    fun deleteRank(rank: Rank)

    /**
     * Fetches a [User] from the database by the given [uniqueId].
     */
    @Throws()
    fun fetchUser(uniqueId: UUID): User?

    /**
     * Saves a given [user] to the database.
     */
    fun saveUser(user: User)

    /**
     * Fetches a list of grants issued by the given [uuid].
     */
    fun fetchGrantsIssuedBy(uuid: UUID): List<IssuedByQueryResult>

}