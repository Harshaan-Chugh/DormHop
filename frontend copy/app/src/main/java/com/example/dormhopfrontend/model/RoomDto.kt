package com.example.dormhopfrontend.model

import com.google.gson.annotations.SerializedName

<<<<<<< HEAD
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
=======
/**
 * Mirrors the Python Room.serialize():
 * {
 *   "dorm": String,
 *   "room_number": String,
 *   "occupancy": Int,
 *   "amenities": List<String>,
 *   "description": String?    // may be null
 * }
 */
data class RoomDto(
    @SerializedName("dorm")
    val dorm: String,

    @SerializedName("room_number")
    val roomNumber: String,

    @SerializedName("occupancy")
    val occupancy: Int,

    @SerializedName("amenities")
    val amenities: List<String>,

    @SerializedName("description")
    val description: String?
>>>>>>> b415288ab12c93f049217ef3a1cbb40ddf906c9c
)
