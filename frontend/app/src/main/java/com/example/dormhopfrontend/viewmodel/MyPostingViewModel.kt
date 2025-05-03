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
import kotlinx.coroutines.flow.asStateFlow


@HiltViewModel
class MyPostingViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {

    private val _user = MutableStateFlow<UserDto?>(null)
    val   user : StateFlow<UserDto?> = _user

    private val _loading = MutableStateFlow(true)
    val   loading : StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error  : StateFlow<String?> = _error

    private val _features = MutableStateFlow<List<String>>(emptyList())
    val features : StateFlow<List<String>> = _features.asStateFlow()

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _loading.value = true
        _error.value   = null

        val resp = api.getProfile()
        if (resp.isSuccessful) {
            val profile = resp.body()!!
            _user.value = profile

            profile.currentRoom?.dorm?.let { dormName ->
                val featsResp = api.getDormFeatures()
                if (featsResp.isSuccessful) {
                    val all = featsResp.body().orEmpty()
                    _features.value = all[dormName].orEmpty()
                }
            }
        } else {
            _error.value = "${resp.code()} ${resp.message()}"
        }

        _loading.value = false
    }
}