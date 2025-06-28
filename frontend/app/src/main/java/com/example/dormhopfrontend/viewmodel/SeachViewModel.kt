package com.example.dormhopfrontend.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormhopfrontend.model.ApiService
import com.example.dormhopfrontend.model.RoomDto
import com.example.dormhopfrontend.model.RoomIdRequest
import com.example.dormhopfrontend.model.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repo: RoomRepository,
    private val api: ApiService
) : ViewModel() {
    // Recommendations
    private val _recommendedRooms = MutableStateFlow<List<RoomDto>>(emptyList())
    val showRecommended           = MutableStateFlow(false)

    // 1. Search + filters
    val query               = MutableStateFlow("")
    val selectedOccupancies = MutableStateFlow<Set<Int>>(emptySet())
    val selectedCampuses    = MutableStateFlow<Set<String>>(emptySet())
    private val _genderFilter = MutableStateFlow("Male")
    val genderFilter: StateFlow<String?> = _genderFilter.asStateFlow()


    fun setGenderFilter(gender: String) {
        _genderFilter.value = gender
    }

    // 2. All loaded rooms
    private val _allRooms = MutableStateFlow<List<RoomDto>>(emptyList())

    // 3. Combined search + filter
    private val sourceRooms: Flow<List<RoomDto>> = combine(
        _allRooms,
        _recommendedRooms,
        showRecommended
    ) { all, recs, showRec ->
        if (showRec) recs else all
    }

    // Filtered result
    val rooms: StateFlow<List<RoomDto>> = combine(
        sourceRooms,
        query,
        selectedOccupancies,
        selectedCampuses,
        genderFilter,
    ) { baseList, q, occs, camps, gender ->
        baseList.filter { room ->
            (occs.isEmpty() || room.occupancy in occs) &&
                    (camps.isEmpty() || room.campus in camps) &&
                    (gender.isNullOrBlank() || room.userGender?.equals(gender, ignoreCase = true) == true) &&
                    (q.isBlank()
                            || room.dorm.contains(q, ignoreCase = true)
                            || room.roomNumber.contains(q, ignoreCase = true)
                            || room.amenities.any { it.contains(q, ignoreCase = true) }
                            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // 4. Saved‐rooms IDs
    private val _savedIds = MutableStateFlow<Set<Int>>(emptySet())
    val savedIds: StateFlow<Set<Int>> = _savedIds.asStateFlow()

    init {
        viewModelScope.launch {
            _allRooms.value         = repo.listRooms()
            _recommendedRooms.value = repo.recommendRooms()
            _allRooms.value         = repo.listRooms()

            // fetch saved IDs…
            val resp = api.listSavedRooms()
            if (resp.isSuccessful) {
                _savedIds.value = resp.body()
                    ?.savedRooms
                    ?.map { it.id }
                    .orEmpty()
                    .toSet()
            }
        }
    }

    fun setShowRecommended(on: Boolean) {
        showRecommended.value = on
    }

    fun toggleOccupancy(occupancy: Int, isSelected: Boolean) {
        selectedOccupancies.update { set ->
            if (isSelected) set + occupancy else set - occupancy
        }
    }

    fun toggleCampus(campus: String, isSelected: Boolean) {
        selectedCampuses.update { set ->
            if (isSelected) set + campus else set - campus
        }
    }

    fun loadMore() {
        viewModelScope.launch {
            val next = repo.loadNextPage()
            _allRooms.update { it + next }
        }
    }

    fun toggleSave(room: RoomDto) {
        val wasSaved = room.id in _savedIds.value
        viewModelScope.launch {
            try {
                if (wasSaved) {
                    val resp = api.unsaveRoom(room.id)
                    Log.d("SearchVM", "unsaveRoom → ${resp.code()} / ${resp.errorBody()?.string()}")
                    if (resp.isSuccessful) {
                        _savedIds.update { it - room.id }
                    }
                } else {
                    val resp = api.saveRoom(RoomIdRequest(room.id))
                    Log.d("SearchVM", "saveRoom   → ${resp.code()} / ${resp.errorBody()?.string()}")
                    if (resp.isSuccessful) {
                        _savedIds.update { it + room.id }
                    }
                }
            } catch (t: Throwable) {
                Log.e("SearchVM", "toggleSave failed", t)
            }
        }
    }
}
