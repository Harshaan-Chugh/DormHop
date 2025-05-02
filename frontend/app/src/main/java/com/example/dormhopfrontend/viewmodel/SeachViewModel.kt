package com.example.dormhopfrontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormhopfrontend.model.RoomDto
import com.example.dormhopfrontend.model.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repo: RoomRepository
) : ViewModel() {

    // 1. Search query text
    val query = MutableStateFlow("")

    // 2. Filter selections
    val selectedOccupancies = MutableStateFlow<Set<Int>>(emptySet())
    val selectedCampuses    = MutableStateFlow<Set<String>>(emptySet())

    // 3. Backing list of all loaded rooms
    private val _allRooms = MutableStateFlow<List<RoomDto>>(emptyList())

    // 4. Combined state: applies search + filters
    val rooms: StateFlow<List<RoomDto>> = combine(
        _allRooms,
        query,
        selectedOccupancies,
        selectedCampuses
    ) { list, q, occs, camps ->
        list.filter { room ->
            // occupancy filter
            (occs.isEmpty() || room.occupancy in occs)
                    // campus filter
                    && (camps.isEmpty() || room.campus in camps)
                    // search text filter
                    && (q.isBlank()
                    || room.dorm.contains(q, ignoreCase = true)
                    || room.roomNumber.contains(q, ignoreCase = true)
                    || room.amenities.any { it.contains(q, ignoreCase = true) }
                    )
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        // initial load
        viewModelScope.launch {
            _allRooms.value = repo.listRooms()
        }
    }

    /** Toggle an occupancy filter on/off */
    fun toggleOccupancy(occupancy: Int, isSelected: Boolean) {
        selectedOccupancies.update { set ->
            if (isSelected) set + occupancy else set - occupancy
        }
    }

    /** Toggle a campus filter on/off */
    fun toggleCampus(campus: String, isSelected: Boolean) {
        selectedCampuses.update { set ->
            if (isSelected) set + campus else set - campus
        }
    }

    /** Load the next page of results and append */
    fun loadMore() {
        viewModelScope.launch {
            val nextPage = repo.loadNextPage()
            _allRooms.update { current -> current + nextPage }
        }
    }
}
