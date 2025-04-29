package com.example.dormhopfrontend.model

import com.google.gson.annotations.SerializedName

/**
 * Mirrors the Python User.serialize():
 * {
 *   "id": Int,
 *   "email": String,
 *   "full_name": String,
 *   "class_year": Int,
 *   "created_at": String,     // ISO timestamp + "Z"
 *   "current_room": RoomDto?  // nested Room or null
 *   "is_room_listed": Boolean
 * }
 */
data class UserDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("email")
    val email: String,

    @SerializedName("full_name")
    val fullName: String,

    @SerializedName("class_year")
    val classYear: Int,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("current_room")
    val currentRoom: RoomDto?,

    @SerializedName("is_room_listed")
    val isRoomListed: Boolean
)
