package net.evilblock.permissions.bukkit.user.task

import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.user.UserHandler

class UserSaveTask : Runnable {

    override fun run() {
        for (user in UserHandler.loadedUsers.values) {
            if (user.requiresSave) {
                try {
                    EvilPermissions.instance.database.saveUser(user)
                    user.requiresSave = false
                } catch (e: Exception) {}
            }
        }
    }

}