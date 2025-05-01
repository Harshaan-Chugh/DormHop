package com.example.dormhopfrontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class AuthViewModel @Inject constructor() : ViewModel() {
    private val _idToken = MutableStateFlow<String?>(null)
    /**
     * Exposed token as a read-only flow. Null until user signs in.
     */
    val idToken: StateFlow<String?> = _idToken.asStateFlow()

    /**
     * Call this when you receive the Google ID token from RegistrationScreen.
     */
    fun setIdToken(token: String) {
        viewModelScope.launch {
            _idToken.value = token
        }
    }
}
