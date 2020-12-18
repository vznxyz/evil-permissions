package net.evilblock.permissions.user

import net.evilblock.permissions.EvilPermissions
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object UserHandler {

    val loadedUsers: MutableMap<UUID, User> = ConcurrentHashMap()

    fun cacheUser(user: User) {
        loadedUsers[user.uniqueId] = user
    }

    fun getByUniqueId(uniqueId: UUID) : User? {
        return loadedUsers[uniqueId]
    }

    fun loadOrCreate(uniqueId: UUID) : User {
        return getByUniqueId(uniqueId) ?: EvilPermissions.instance.database.fetchUser(uniqueId) ?: EvilPermissions.instance.plugin.makeUser(uniqueId)
    }

}