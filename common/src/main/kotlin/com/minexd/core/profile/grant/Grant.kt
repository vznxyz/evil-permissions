package com.minexd.core.profile.grant

import com.google.gson.annotations.JsonAdapter
import com.minexd.core.rank.Rank
import java.util.*

open class Grant(
    @JsonAdapter(Rank.RankReferenceSerializer::class)
    var rank: Rank,
    var issuedBy: UUID?,
    val reason: String,
    var expiresAt: Long?
) {

    val id: UUID = UUID.randomUUID()
    val issuedAt: Long = System.currentTimeMillis()

    var removed: Boolean = false
    var removedBy: UUID? = null
    var removedAt: Long? = null
    var removeReason: String? = null

    fun isActive(): Boolean {
        return removedAt == null && (expiresAt == null || System.currentTimeMillis() < expiresAt!!)
    }

}