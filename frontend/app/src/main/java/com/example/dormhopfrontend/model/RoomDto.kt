package com.example.dormhopfrontend.model

import com.google.gson.annotations.SerializedName

data class RoomDto(
    @SerializedName("id")            val id: Int,
    @SerializedName("dorm")          val dorm: String,
    @SerializedName("room_number")   val roomNumber: String,
    @SerializedName("occupancy")     val occupancy: Int,
    @SerializedName("amenities")     val amenities: List<String>,
    @SerializedName("description")   val description: String?,
    @SerializedName("created_at")    val createdAt: String,
    @SerializedName("updated_at")    val updatedAt: String,
    @SerializedName("is_room_listed")val isRoomListed: Boolean,
    @SerializedName("owner")         val owner: OwnerDto
)

data class OwnerDto(
    @SerializedName("id")           val id: Int,
    @SerializedName("email")        val email: String,
    @SerializedName("full_name")    val fullName: String,
    @SerializedName("class_year")   val classYear: Int
)