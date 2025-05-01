package com.example.dormhopfrontend.model.network

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val ctx: Context
) {
    private val prefs = ctx.getSharedPreferences("auth", Context.MODE_PRIVATE)

    var token: String?
        get() = prefs.getString("jwt", null)
        set(value) {
            prefs.edit().putString("jwt", value).apply()
        }
}