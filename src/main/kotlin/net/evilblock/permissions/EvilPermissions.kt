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

    lateinit var database: Database
    lateinit var userHandler: UserHandler
    lateinit var pidgin: Pidgin

    fun initialLoad() {
        instance = this

        database = MongoDatabase()
        userHandler = UserHandler()

        RankHandler.loadRanks()

        pidgin = Pidgin("EVIL_PERMISSIONS", plugin.getJedisPool(), PidginOptions(async = true))
        pidgin.registerListener(RankMessageListeners)
    }

}