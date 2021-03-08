package com.minexd.core.audit

import com.minexd.core.profile.grant.GrantQueryResult
import com.minexd.core.profile.punishment.PunishmentQueryResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.*

interface AuditService {

    @GET("audit/grants/{id}")
    fun getGrantsIssuedBy(@Path("id") id: UUID): Call<List<GrantQueryResult>>

    @GET("audit/punishments/{id}")
    fun getPunishmentsIssuedBy(@Path("id") id: UUID): Call<List<PunishmentQueryResult>>

}