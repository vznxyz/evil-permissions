package com.minexd.core

import com.minexd.core.api.APIKeyInterceptor
import com.minexd.core.audit.AuditService
import com.minexd.core.friend.FriendsService
import com.minexd.core.plugin.Plugin
import com.minexd.core.profile.ProfileService
import com.minexd.core.rank.RankHandler
import com.minexd.core.rank.RankMessageListeners
import com.minexd.core.rank.RankService
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.pidgin.Pidgin
import net.evilblock.pidgin.PidginOptions
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class CoreXD(val plugin: Plugin) {

    companion object {
        @JvmStatic
        lateinit var instance: CoreXD
    }

    lateinit var pidgin: Pidgin

    val originId: UUID = UUID.randomUUID()

    lateinit var retrofit: Retrofit

    lateinit var ranksService: RankService
    lateinit var profilesService: ProfileService
    lateinit var auditService: AuditService
    lateinit var friendsService: FriendsService

    fun initialLoad() {
        instance = this

        val client = OkHttpClient.Builder()
                .addInterceptor(APIKeyInterceptor())
                .build()

        retrofit = Retrofit.Builder()
                .baseUrl(plugin.getAPIUrl())
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(Serializers.gson))
                .build()

        ranksService = retrofit.create(RankService::class.java)
        profilesService = retrofit.create(ProfileService::class.java)
        auditService = retrofit.create(AuditService::class.java)
        friendsService = retrofit.create(FriendsService::class.java)

        RankHandler.loadRanks()

        pidgin = Pidgin("CoreXD", plugin.getRedis().jedisPool!!, plugin.getGSON(), PidginOptions(async = true))
        pidgin.registerListener(RankMessageListeners)
    }

}