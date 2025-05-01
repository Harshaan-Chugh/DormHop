package com.example.dormhopfrontend.model

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id")               val id: Int,
    @SerializedName("email")            val email: String,
    @SerializedName("full_name")        val fullName: String,
    @SerializedName("class_year")       val classYear: Int,
    @SerializedName("created_at")       val createdAt: String,
    @SerializedName("current_room")     val currentRoom: RoomDto?,     // may be null
    @SerializedName("is_room_listed")   val isRoomListed: Boolean
)
