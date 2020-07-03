package net.evilblock.permissions

import net.evilblock.pidgin.Pidgin
import net.evilblock.pidgin.PidginOptions
import net.evilblock.permissions.database.Database
import net.evilblock.permissions.database.impl.MongoDatabase
import net.evilblock.permissions.plugin.Plugin
import net.evilblock.permissions.rank.*
import net.evilblock.permissions.user.*

class EvilPermissions(val plugin: Plugin) {

    companion object {
        @JvmStatic lateinit var instance: EvilPermissions
    }

    val database: Database
    val userHandler: UserHandler
    val rankHandler: RankHandler
    val pidgin: Pidgin

    init {
        instance = this

        database = MongoDatabase()
        plugin.getLogger().info("Loaded database")

        userHandler = UserHandler()
        plugin.getLogger().info("Loaded user handler")

        rankHandler = RankHandler()
        rankHandler.loadRanks()
        plugin.getLogger().info("Loaded rank handler")

        pidgin = Pidgin("EVIL_PERMISSIONS", plugin.getJedisPool(), PidginOptions(async = true))
        pidgin.registerListener(RankMessageListeners)
        plugin.getLogger().info("Loaded pidgin")
    }

}