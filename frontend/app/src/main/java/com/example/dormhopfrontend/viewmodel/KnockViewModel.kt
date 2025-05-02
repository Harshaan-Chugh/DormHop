package com.example.dormhopfrontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormhopfrontend.model.KnockRepository
import com.example.dormhopfrontend.model.KnockResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KnockViewModel @Inject constructor(
    private val repo: KnockRepository
) : ViewModel() {
    private val _sent = MutableStateFlow<List<KnockResponse>>(emptyList())
    val sent: StateFlow<List<KnockResponse>> = _sent.asStateFlow()

    private val _received = MutableStateFlow<List<KnockResponse>>(emptyList())
    val received: StateFlow<List<KnockResponse>> = _received.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init { loadAll() }

    fun loadAll() {
        viewModelScope.launch {
            // load sent knocks
            val sentResp = repo.listSent()
            if (sentResp.isSuccessful) {
                _sent.value = sentResp.body()?.knocks.orEmpty()
            } else {
                _error.value = "Failed to load sent knocks: ${'$'}{sentResp.code()} ${'$'}{sentResp.message()}"
            }

            // load received knocks
            val recResp = repo.listReceived()
            if (recResp.isSuccessful) {
                _received.value = recResp.body()?.knocks.orEmpty()
            } else {
                _error.value = "Failed to load received knocks: ${'$'}{recResp.code()} ${'$'}{recResp.message()}"
            }
        }
    }

    fun sendKnock(toRoomId: Int, onResult: (KnockResponse) -> Unit) {
        viewModelScope.launch {
            val resp = repo.sendKnock(toRoomId)
            val knock = resp.body()
            if (knock != null) {
                onResult(knock)
                loadAll()
            } else {
                _error.value = "Failed to knock: ${resp.code()} ${resp.message()}"
            }
        }
    }

    fun acceptKnock(id: Int) {
        viewModelScope.launch {
            val resp = repo.accept(id)
            val knock = resp.body()
            if (knock != null) {
                loadAll()
            } else {
                _error.value = "Accept failed: ${resp.code()} ${resp.message()}"
            }
        }
    }

    fun deleteKnock(id: Int) {
        viewModelScope.launch {
            if (repo.delete(id).isSuccessful) loadAll()
            else _error.value = "Delete failed"
        }
    }
}