package com.minexd.core.proxy.profile

import com.minexd.core.profile.Profile
import com.minexd.core.proxy.ProxyPlugin
import java.util.*

class ProxyProfile(uuid: UUID) : Profile(uuid) {

    override fun getUsername(): String {
        return ""
    }

    override fun apply() {
        val player = ProxyPlugin.instance.proxy.getPlayer(uuid) ?: return

        getCompoundedPermissions().forEach {
            if (it.startsWith("-")) {
                player.setPermission(it.substring(1), false)
            } else {
                player.setPermission(it, true)
            }
        }
    }

}