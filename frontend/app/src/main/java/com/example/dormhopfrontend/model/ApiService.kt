package com.example.dormhopfrontend.model

import com.example.dormhopfrontend.model.RoomDto
import com.example.dormhopfrontend.model.UserDto
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

data class RoomsResponse(val rooms: List<RoomDto>, val total: Int)
data class VerifyRequest(@SerializedName("id_token") val idToken: String)
data class VerifyResponse(val token: String, val user: UserDto)
data class UpdateRoomRequest(
    val dorm: String,
    @SerializedName("room_number") val roomNumber: String,
    val occupancy: Int,
    val amenities: List<String>,
    val description: String?
)
data class VisibilityRequest(@SerializedName("is_room_listed") val isListed: Boolean)
data class VisibilityResponse(
    @SerializedName("is_room_listed") val isRoomListed: Boolean,
    @SerializedName("updated_at") val updatedAt: String
)

interface ApiService {
    @POST("api/auth/verify_id_token")
    suspend fun verifyIdToken(@Body req: VerifyRequest): Response<VerifyResponse>

    @GET("api/users/me")
    suspend fun getProfile(): Response<UserDto>

    @GET("api/rooms")
    suspend fun listRooms(): Response<RoomsResponse>

    @GET("api/recommendations")
    suspend fun recommendRooms(): Response<RoomsResponse>

    @PATCH("api/users/me/room")
    suspend fun updateRoom(@Body body: UpdateRoomRequest): Response<RoomDto>

    @PATCH("api/users/me/room/visibility")
    suspend fun setVisibility(@Body body: VisibilityRequest): Response<VisibilityResponse>
}