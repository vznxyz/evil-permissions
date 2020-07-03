package net.evilblock.permissions.user

import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.user.grant.Grant
import net.evilblock.permissions.user.grant.GrantSerializer
import org.bson.Document
import java.lang.IllegalStateException
import java.util.*

object UserSerializer {

    @JvmStatic
    fun deserialize(document: Document): User {
        val user = EvilPermissions.instance.plugin.makeUser(UUID.fromString(document.getString("id")))

        for (grantDocument in document["grants"] as List<Document>) {
            try {
                val grant = GrantSerializer.deserialize(grantDocument, Grant())
                user.grants.add(grant)
            } catch (ignore: IllegalStateException) {}
        }

        if (document.containsKey("permissions")) {
            user.permissions.addAll(document["permissions"] as List<String>)
        }

        return user
    }

    @JvmStatic
    fun serialize(user: User): Document {
        val document = Document("id", user.uniqueId.toString())
        document["grants"] = user.grants.map { GrantSerializer.serialize(Document(), it) }.toList()
        document["permissions"] = user.permissions.toList()
        return document
    }

}