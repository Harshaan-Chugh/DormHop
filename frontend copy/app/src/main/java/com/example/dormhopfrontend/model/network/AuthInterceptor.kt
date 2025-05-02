package com.example.dormhopfrontend.model.network

import com.example.dormhopfrontend.viewmodel.AuthViewModel
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

//grabs the token and then stores it in the header
//used for the oauth
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenManager.token
        val req = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(req)
    }
}

