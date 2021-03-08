package com.minexd.core.rank

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RankService {

    @GET("ranks/list")
    fun list(): Call<List<Rank>>

    @GET("ranks/get/{id}")
    fun get(@Path("id") id: String): Call<Rank>

    @POST("ranks/create/{id}")
    fun create(@Path("id") id: String): Call<Rank>

    @POST("ranks/delete/{id}")
    fun delete(@Path("id") id: String): Call<Void>

    @JvmSuppressWildcards
    @POST("ranks/update/{id}")
    fun update(@Path("id") id: String, @Body data: Map<String, Any?>): Call<Void>

}