package net.evilblock.permissions.bukkit.user.task

import net.evilblock.permissions.user.UserHandler

class UserApplyTask : Runnable {

    override fun run() {
        for (user in UserHandler.loadedUsers.values) {
            if (user.requiresApply) {
                user.requiresApply = false
                user.apply()
            }
        }
    }

}