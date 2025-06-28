package com.example.dormhopfrontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dormhopfrontend.model.ApiService
import com.example.dormhopfrontend.model.RoomRepository
import com.example.dormhopfrontend.model.RoomDto
import com.example.dormhopfrontend.model.UserDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UiState(
    val step: Int = 1,

    // personal
    val fullName: String = "",
    val email: String = "",
    val classYear: String? = null,
    val isRoomListed: Boolean = false,
    val gender: String? = null,
    val canProceedPersonal: Boolean = false,

    // dorm
    val dorm: String = "",
    val roomNumber: String = "",
    val occupancy: String? = null,
    val amenities: List<String> = emptyList(),
    val description: String = "",
    val canProceedDorm: Boolean = false,

    // save
    val saving: Boolean = false,
    val error: String? = null,
    val done: Boolean = false
)

private data class PersonalInfo(
    val fullName: String,
    val email: String,
    val classYear: String?,
    val isRoomListed: Boolean,
    val gender: String?
)

private data class DormInfo(
    val dorm: String,
    val roomNumber: String,
    val occupancy: String?,
    val amenities: List<String>,
    val description: String
)

@HiltViewModel
class CreateProfileViewModel @Inject constructor(
    private val repo: RoomRepository,
    private val api: ApiService
) : ViewModel() {

    private val _step = MutableStateFlow(1)

    private val _fullName = MutableStateFlow("")
    private val _email = MutableStateFlow("")
    private val _classYear = MutableStateFlow<String?>(null)
    private val _isRoomListed = MutableStateFlow(false)
    private val _gender = MutableStateFlow<String?>(null)

    private val _dorm = MutableStateFlow("")
    private val _roomNumber = MutableStateFlow("")
    private val _occupancy = MutableStateFlow<String?>(null)
    private val _amenities = MutableStateFlow<List<String>>(emptyList())
    private val _description = MutableStateFlow("")

    private val _saving = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _done = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            val resp = api.getProfile()
            if (resp.isSuccessful) {
                val user: UserDto = resp.body()!!
                _fullName.value = user.fullName
                _email.value = user.email
                _classYear.value = user.classYear.toString()
                _isRoomListed.value = user.isRoomListed
                _gender.value = user.gender

                user.currentRoom?.let { room ->
                    _dorm.value = room.dorm
                    _roomNumber.value = room.roomNumber
                    _occupancy.value = when (room.occupancy) {
                        1 -> "Single"
                        2 -> "Double"
                        3 -> "Triple"
                        4 -> "Quad"
                        else -> room.occupancy.toString()
                    }
                    _amenities.value = room.amenities
                    _description.value = room.description.orEmpty()
                }
            }
        }
    }

    private val personalInfo: Flow<PersonalInfo> = combine(
        _fullName, _email, _classYear, _isRoomListed, _gender
    ) { fn, em, cy, listed, gender ->
        PersonalInfo(fn, em, cy, listed, gender)
    }

    private val dormInfo: Flow<DormInfo> = combine(
        _dorm, _roomNumber, _occupancy, _amenities, _description
    ) { dorm, rn, occ, ams, desc ->
        DormInfo(dorm, rn, occ, ams, desc)
    }

    private val partialState: StateFlow<UiState> = combine(
        _step,
        personalInfo,
        dormInfo,
        _saving,
        _error
    ) { step, personal, dorm, saving, error ->
        UiState(
            step = step,

            fullName = personal.fullName,
            email = personal.email,
            classYear = personal.classYear,
            isRoomListed = personal.isRoomListed,
            gender = personal.gender,
            canProceedPersonal = personal.fullName.isNotBlank()
                    && personal.email.isNotBlank()
                    && personal.classYear != null
                    && personal.gender != null,

            dorm = dorm.dorm,
            roomNumber = dorm.roomNumber,
            occupancy = dorm.occupancy,
            amenities = dorm.amenities,
            description = dorm.description,
            canProceedDorm = dorm.dorm.isNotBlank()
                    && dorm.roomNumber.isNotBlank()
                    && dorm.occupancy != null,

            saving = saving,
            error = error,
            done = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    val uiState: StateFlow<UiState> = combine(
        partialState,
        _done
    ) { partial, done ->
        partial.copy(done = done)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    /** Personal step setters **/
    fun onFullNameChanged(fn: String) {
        _fullName.value = fn
    }

    fun onEmailChanged(em: String) {
        _email.value = em
    }

    fun onClassYearChanged(cy: String?) {
        _classYear.value = cy
    }

    fun onIsRoomListedChanged(v: Boolean) {
        _isRoomListed.value = v
    }

    fun onGenderChanged(g: String?) {
        _gender.value = g
    }

    fun onNextPersonal() {
        _step.value = 2
    }

    fun onBackToPersonal() {
        _step.value = 1
    }

    /** Dorm step setters **/
    fun onDormChanged(d: String) {
        _dorm.value = d
    }

    fun onRoomNumberChanged(r: String) {
        _roomNumber.value = r
    }

    fun onOccupancyChanged(o: String?) {
        _occupancy.value = o
    }

    fun addAmenity(a: String) {
        _amenities.value = _amenities.value + a
    }

    fun removeAmenity(a: String) {
        _amenities.value = _amenities.value - a
    }

    fun onDescriptionChanged(d: String) {
        _description.value = d
    }

    /** Final save **/
    fun onSave() {
        viewModelScope.launch {
            _saving.value = true
            _error.value = null
            try {
                val occInt = when (_occupancy.value) {
                    "Single" -> 1
                    "Double" -> 2
                    "Triple" -> 3
                    "Quad" -> 4
                    else -> _occupancy.value?.toIntOrNull() ?: 1
                }

                val result: RoomDto? = repo.updateRoom(
                    dorm = _dorm.value,
                    roomNumber = _roomNumber.value,
                    occupancy = occInt,
                    amenities = _amenities.value,
                    description = _description.value.ifBlank { null }
                )
                if (result != null) {
                    _done.value = true
                } else {
                    _error.value = "Failed to save room"
                }
            } catch (t: Throwable) {
                _error.value = t.message ?: "Unexpected error"
            }
            _saving.value = false
        }
    }
}
