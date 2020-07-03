package net.evilblock.permissions.user

import net.evilblock.cubed.Cubed
import net.evilblock.cubed.store.bukkit.UUIDCache
import net.evilblock.permissions.EvilPermissions
import java.util.*
import kotlin.collections.HashMap

class UserHandler {

    val loadedUsers: HashMap<UUID, User> = hashMapOf()

    fun getByUniqueId(uniqueId: UUID) : User? {
        return loadedUsers[uniqueId]
    }

    fun loadOrCreate(uniqueId: UUID) : User? {
        return getByUniqueId(uniqueId) ?: EvilPermissions.instance.database.fetchUser(uniqueId) ?: EvilPermissions.instance.plugin.makeUser(uniqueId)
    }

    fun loadOrCreateByUsername(input: String): User? {
        var uuid: UUID?
        try {
            uuid = UUID.fromString(input)
        } catch (e: Exception) {
            uuid = Cubed.instance.uuidCache.uuid(input)
            if (uuid == null) {
                val optionalProfile = UUIDCache.fetchFromMojang(input)
                if (optionalProfile.isPresent) {
                    uuid = optionalProfile.get().first
                }
            }
        }

        if (uuid == null) {
            return null
        }

        return loadOrCreate(uuid)
    }

}