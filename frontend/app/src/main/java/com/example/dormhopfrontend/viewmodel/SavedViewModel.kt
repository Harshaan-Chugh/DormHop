package com.example.dormhopfrontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormhopfrontend.model.RoomDto
import com.example.dormhopfrontend.model.network.SavedRoomsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedViewModel @Inject constructor(
    private val repo: SavedRoomsRepository
) : ViewModel() {

    private val _rooms   = MutableStateFlow<List<RoomDto>>(emptyList())
    private val _loading = MutableStateFlow(false)
    private val _error   = MutableStateFlow<String?>(null)

    val rooms   : StateFlow<List<RoomDto>> = _rooms
    val loading : StateFlow<Boolean>       = _loading
    val error   : StateFlow<String?>       = _error

    init { load() }

    fun load() {
        viewModelScope.launch {
            _loading.value = true
            _error.value   = null
            try {
                _rooms.value = repo.getSavedRooms()
            } catch (t: Throwable) {
                _error.value = t.message ?: "Unknown error"
            }
            _loading.value = false
        }
    }

    fun unsave(roomId: Int) {
        viewModelScope.launch {
            if (repo.unsave(roomId)) {
                _rooms.value = _rooms.value.filterNot { it.id == roomId }
            } else {
                _error.value = "Failed to remove"
            }
        }
    }
}
