package net.evilblock.permissions.plugin.bukkit.user.task

import net.evilblock.permissions.EvilPermissions

class UserApplyTask : Runnable {

    override fun run() {
        for (user in EvilPermissions.instance.userHandler.loadedUsers.values) {
            if (user.requiresApply) {
                user.requiresApply = false
                user.apply()
            }
        }
    }

}