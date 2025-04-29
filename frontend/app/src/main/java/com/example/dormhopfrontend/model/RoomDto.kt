package com.example.dormhopfrontend.model

import com.google.gson.annotations.SerializedName

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
)
