package com.example.dormhopfrontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val repo: RoomRepository
) : ViewModel() {

    private val _room = MutableStateFlow<RoomDto?>(null)
    val room: StateFlow<RoomDto?> = _room

    fun load(roomId: Int) {
        viewModelScope.launch {
            _room.value = repo.getRoomById(roomId)
        }
    }
}
