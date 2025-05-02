package com.example.dormhopfrontend.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormhopfrontend.model.ApiService
import com.example.dormhopfrontend.model.UserDto
import com.example.dormhopfrontend.model.VerifyRequest
import com.example.dormhopfrontend.model.network.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Holds authentication state (the Google ID token) for the app.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val api: ApiService
) : ViewModel() {
    // Phase 1: raw Google ID token
    private val _googleIdToken = MutableStateFlow<String?>(null)
    val googleIdToken: StateFlow<String?> = _googleIdToken.asStateFlow()

    // Phase 2: backend JWT
    private val _jwt = MutableStateFlow<String?>(null)
    val jwt: StateFlow<String?> = _jwt.asStateFlow()

    /* ───── Profile & onboarding flag ───── */
    private val _user         = MutableStateFlow<UserDto?>(null)
    val user: StateFlow<UserDto?> = _user.asStateFlow()

    private val _needsProfile = MutableStateFlow(false)
    val needsProfile: StateFlow<Boolean> = _needsProfile.asStateFlow()

    /**
     * Called when user selects a Google account and ID token is obtained.
     */
    fun onGoogleIdToken(idToken: String) {
        _googleIdToken.value = idToken
    }

    /**
     * Exchanges the Google ID token for our server JWT.
     */
    fun exchangeForJwt(idToken: String) {
        viewModelScope.launch {
            val resp = api.verifyIdToken(VerifyRequest(idToken))
            Log.d("AuthVM", "verifyIdToken → code=${resp.code()}, body=${resp.errorBody()?.string() ?: resp.body()}")
            if (resp.isSuccessful) {
                resp.body()?.let { body ->
                    _jwt.value = body.token
                    tokenManager.token = body.token
                }
            } else {
                // BACKEND REJECTED YOUR ID TOKEN → RESET TO PHASE 1
                Log.w("AuthVM", "Invalid ID token, resetting back to registration")
                _googleIdToken.value = null
            }
        }
    }

    /** Exchange Google-token → JWT, persist, then pull profile. */
    fun pullProfile() {
        viewModelScope.launch {
            val resp = api.getProfile()
            if (resp.isSuccessful) {
                resp.body()?.let { profile ->
                    _user.value = profile
                    /* need profile if no dorm yet */
                    _needsProfile.value = profile.currentRoom == null
                }
            } else {
                Log.e("AuthVM", "getProfile failed: ${resp.code()}")
            }
        }
    }

    /** Convenience: wipe state so UI returns to RegistrationScreen. */
    private fun resetAuth() {
        _googleIdToken.value = null
        _jwt.value           = null
        _user.value          = null
        _needsProfile.value  = false
        tokenManager.token   = null
    }
}


