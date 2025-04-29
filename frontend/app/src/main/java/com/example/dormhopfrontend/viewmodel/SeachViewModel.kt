package com.example.dormhopfrontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormhopfrontend.model.RoomRepository
import com.example.dormhopfrontend.model.RoomDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repo: RoomRepository
) : ViewModel() {

    val query = MutableStateFlow("")
    private val _allRooms = MutableStateFlow<List<RoomDto>>(emptyList())
    // makes it so that the rooms shown will change based on the query
    val rooms: StateFlow<List<RoomDto>> = combine(_allRooms, query) { list, q ->
        if (q.isBlank()) list
        else list.filter { room ->
            room.dorm.contains(q, ignoreCase = true)
                    || room.roomNumber.contains(q, ignoreCase = true)
                    || room.amenities.any { it.contains(q, ignoreCase = true) }
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            _allRooms.value = repo.listRooms()
        }
    }
}
