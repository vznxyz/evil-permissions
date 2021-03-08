package com.minexd.core.api

import com.minexd.core.CoreXD
import okhttp3.Interceptor
import okhttp3.Response

class APIKeyInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain
                .request()
                .newBuilder()
                .header("Content-Type", "application/json")
                .header("X-API-Key", CoreXD.instance.plugin.getAPIKey())

        return chain.proceed(requestBuilder.build())
    }

}