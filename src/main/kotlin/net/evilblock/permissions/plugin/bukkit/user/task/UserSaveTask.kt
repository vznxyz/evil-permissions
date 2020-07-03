package net.evilblock.permissions.plugin.bukkit.user.task

import net.evilblock.permissions.EvilPermissions

class UserSaveTask : Runnable {

    override fun run() {
        for (user in EvilPermissions.instance.userHandler.loadedUsers.values) {
            if (user.requiresSave) {
                try {
                    EvilPermissions.instance.database.saveUser(user)
                    user.requiresSave = false
                } catch (e: Exception) {}
            }
        }
    }

}