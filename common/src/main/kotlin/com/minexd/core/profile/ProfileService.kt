package com.minexd.core.profile

import com.google.gson.JsonObject
import com.minexd.core.profile.punishment.Punishment
import com.minexd.core.profile.grant.Grant
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.*

interface ProfileService {

    @GET("profile/{id}")
    fun get(@Path("id") id: UUID): Call<Profile>

    @GET("profile/{id}/touch")
    fun touch(@Path("id") id: UUID): Call<Profile>

    @JvmSuppressWildcards
    @POST("profile/{id}/login")
    fun login(@Path("id") id: UUID, @Body data: Map<String, Any?>): Call<Profile>

    @POST("profile/{id}/grant")
    fun grant(@Path("id") id: UUID, @Body grant: Grant): Call<Void>

    @JvmSuppressWildcards
    @POST("profile/{id}/revokeGrant")
    fun revokeGrant(@Path("id") id: UUID, @Body data: Map<String, Any?>): Call<Void>

    @POST("profile/{id}/permissions/add")
    fun addPermission(@Path("id") id: UUID, @Body permission: String): Call<Void>

    @POST("profile/{id}/permissions/revoke")
    fun revokePermission(@Path("id") id: UUID, @Body permission: String): Call<Void>

    @POST("profile/{id}/punish")
    fun punish(@Path("id") id: UUID, @Body punishment: Punishment): Call<Void>

    @JvmSuppressWildcards
    @POST("profile/{id}/pardon")
    fun pardon(@Path("id") id: UUID, @Body data: Map<String, Any?>): Call<Void>

    @GET("profile/{id}/suspensionInfo")
    fun getSuspensionInfo(@Path("id") id: UUID): Call<JsonObject>

    @GET("profile/{id}/sharedAccounts")
    fun getSharedAccounts(@Path("id") id: UUID): Call<List<UUID>>

}