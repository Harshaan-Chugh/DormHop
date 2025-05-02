package com.example.dormhopfrontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormhopfrontend.model.ApiService
import com.example.dormhopfrontend.model.RoomDto
import com.example.dormhopfrontend.model.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel to fetch and hold a RoomDto by ID.
 */
@HiltViewModel
class DetailViewModel @Inject constructor(
    private val api: ApiService,
    private val rooms: RoomRepository
) : ViewModel() {

    private val _room     = MutableStateFlow<RoomDto?>(null)
    val room: StateFlow<RoomDto?> = _room

    private val _features = MutableStateFlow<List<String>>(emptyList())
    val  features: StateFlow<List<String>> = _features

    /** Called by the screen */
    fun load(roomId: Int) = viewModelScope.launch {
        // 1) room itself
        _room.value = rooms.getRoomById(roomId)

        // 2) dorm‑wide feature cache (fetch once per VM)
        if (_features.value.isEmpty()) {
            val resp = api.getDormFeatures()
            if (resp.isSuccessful) {
                val map = resp.body().orEmpty()
                val dorm = _room.value?.dorm
                _features.value = map[dorm].orEmpty()
            }
        }
    }
}