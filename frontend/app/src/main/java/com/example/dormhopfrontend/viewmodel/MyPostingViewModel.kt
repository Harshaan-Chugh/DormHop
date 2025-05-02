package com.example.dormhopfrontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormhopfrontend.model.ApiService
import com.example.dormhopfrontend.model.UserDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPostingViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {

    private val _user = MutableStateFlow<UserDto?>(null)
    val   user : StateFlow<UserDto?> = _user

    private val _loading = MutableStateFlow(true)
    val   loading : StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val   error  : StateFlow<String?> = _error

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _loading.value = true
        _error.value   = null
        val resp = api.getProfile()
        if (resp.isSuccessful)       _user.value = resp.body()
        else                         _error.value = "${resp.code()} ${resp.message()}"
        _loading.value = false
    }
}