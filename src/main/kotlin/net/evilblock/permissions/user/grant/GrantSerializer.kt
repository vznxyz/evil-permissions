package net.evilblock.permissions.user.grant

import net.evilblock.permissions.EvilPermissions
import org.bson.Document
import java.util.*

object GrantSerializer {

    /**
     * Serializes a given [grant]'s data to a [document].
     */
    @JvmStatic
    fun serialize(document: Document, grant: Grant): Document {
        document["id"] = grant.id.toString()
        document["rank"] = grant.rank.id
        document["reason"] = grant.reason
        document["issuedBy"] = if (grant.issuedBy != null) grant.issuedBy.toString() else null
        document["issuedAt"] = grant.issuedAt
        document["expiresAt"] = grant.expiresAt
        document["removalReason"] = grant.removalReason
        document["removedBy"] = if (grant.removedBy != null) grant.removedBy!!.toString() else null
        document["removedAt"] = grant.removedAt
        return document
    }

    /**
     * Deserializes a given [document]'s data to a given [grant] implementation.
     */
    @JvmStatic
    fun <T : Grant> deserialize(document: Document, grant: T): T {
        grant.rank = EvilPermissions.instance.rankHandler.getRankById(document.getString("rank")) ?: throw IllegalStateException("Couldn't deserialize grant because the rank doesn't exist")
        grant.reason = document.getString("reason")
        grant.issuedAt = document.getLong("issuedAt")!!

        val addedBy = document.getString("issuedBy")
        if (addedBy != null) {
            grant.issuedBy = UUID.fromString(addedBy)
        }

        grant.expiresAt = document.getLong("expiresAt")
        grant.removalReason = document.getString("removalReason")
        grant.removedAt = document.getLong("removedAt")

        val removedBy = document.getString("removedBy")
        if (removedBy != null) {
            grant.removedBy = UUID.fromString(removedBy)
        }

        return grant
    }

}