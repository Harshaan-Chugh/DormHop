//package com.example.dormhopfrontend.model
//
//import android.content.SharedPreferences
//import android.util.Log
//import com.example.dormhopfrontend.model.ApiService
//import com.example.dormhopfrontend.api.requests.IdTokenRequest
//import com.example.dormhopfrontend.api.requests.RegisterRequest
//import com.example.dormhopfrontend.api.requests.VisibilityRequest
//import com.example.dormhopfrontend.api.responses.VerifyResponse
//import com.example.dormhopfrontend.api.responses.UserDto
//import com.example.dormhopfrontend.api.responses.VerifyResponse
//import javax.inject.Inject
//import javax.inject.Singleton
//
//@Singleton
//class UserRepository @Inject constructor(
//    private val api: ApiService,
//    private val prefs: SharedPreferences
//) {
//    companion object {
//        private const val TAG = "UserRepository"
//        private const val PREF_JWT = "jwt_token"
//    }
//
//    /** Saves JWT in SharedPreferences */
//    private fun saveToken(token: String) {
//        prefs.edit().putString(PREF_JWT, token).apply()
//    }
//
//    /** Retrieves saved JWT or null */
//    fun getToken(): String? =
//        prefs.getString(PREF_JWT, null)
//
//    /**
//     * Exchange Google ID token for your own JWT + user info.
//     * Returns the user on success, or null on failure.
//     */
//    suspend fun loginWithGoogle(idToken: String): UserDto? {
//        return try {
//            val resp = api.verifyIdToken(IdTokenRequest(idToken))
//            if (resp.isSuccessful) {
//                val body = resp.body()!!
//                saveToken(body.token)
//                body.user
//            } else {
//                Log.e(TAG, "loginWithGoogle failed: ${resp.code()} ${resp.message()}")
//                null
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Exception in loginWithGoogle", e)
//            null
//        }
//    }
//
//    /**
//     * Dev-only registration endpoint (skips Google).
//     * Returns the user on success, or null on failure.
//     */
//    suspend fun registerUser(
//        email: String,
//        fullName: String,
//        classYear: Int,
//        currentRoom: Map<String, Any>? = null
//    ): UserDto? {
//        return try {
//            val req = RegisterRequest(
//                email = email,
//                fullName = fullName,
//                classYear = classYear,
//                currentRoom = currentRoom
//            )
//            val resp = api.registerUser(req)
//            if (resp.isSuccessful) {
//                val body = resp.body()!!
//                saveToken(body.token)
//                body.user
//            } else {
//                Log.e(TAG, "registerUser failed: ${resp.code()} ${resp.message()}")
//                null
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Exception in registerUser", e)
//            null
//        }
//    }
//
//    /**
//     * Fetch the current user’s profile (requires Authorization header).
//     * Returns the user on success, or null on failure.
//     */
//    suspend fun fetchProfile(): UserDto? {
//        return try {
//            val resp = api.getProfile()
//            if (resp.isSuccessful) {
//                resp.body()
//            } else {
//                Log.e(TAG, "fetchProfile failed: ${resp.code()} ${resp.message()}")
//                null
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Exception in fetchProfile", e)
//            null
//        }
//    }
//}
