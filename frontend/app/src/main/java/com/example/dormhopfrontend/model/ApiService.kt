package com.example.dormhopfrontend.model

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

/**
 * API response wrapper for list endpoints.
 */
data class RoomsResponse(
    val rooms: List<RoomDto>,
    val total: Int
)

/**
 * Request body for Google ID token verification.
 */
data class VerifyRequest(
    @SerializedName("id_token") val idToken: String
)

/**
 * Response from verify_id_token: contains backend JWT and user info.
 */
data class VerifyResponse(
    val token: String,
    val user: UserDto
)

/**
 * Payload to create or update a room.
 */
data class UpdateRoomRequest(
    val dorm: String,
    @SerializedName("room_number") val roomNumber: String,
    val occupancy: Int,
    val amenities: List<String>,
    val description: String?
)

/**
 * Payload to toggle room visibility.
 */
data class VisibilityRequest(
    @SerializedName("is_room_listed") val isListed: Boolean
)

/**
 * Response from toggling visibility.
 */
data class VisibilityResponse(
    @SerializedName("is_room_listed") val isRoomListed: Boolean,
    @SerializedName("updated_at") val updatedAt: String
)

interface ApiService {
    /**
     * Verify Google ID token and receive a JWT.
     */
    @POST("auth/verify_id_token/")
    suspend fun verifyIdToken(
        @Body req: VerifyRequest
    ): Response<VerifyResponse>

    /**
     * Fetch current user profile.
     */
    @GET("users/me/")
    suspend fun getProfile(): Response<UserDto>

    /**
     * List all publicly listed rooms.
     */
    @GET("rooms/")
    suspend fun listRooms(): Response<RoomsResponse>

    /** Fetch a single room by its ID */
    @GET("rooms/{id}/")
    suspend fun getRoom(
        @Path("id") roomId: Int
    ): Response<RoomDto>


    /**
     * Get personalized room recommendations.
     */
    @GET("recommendations/")
    suspend fun recommendRooms(): Response<RoomsResponse>

    /**
     * Create or update the authenticated user's room.
     */
    @PATCH("users/me/room/")
    suspend fun updateRoom(
        @Body body: UpdateRoomRequest
    ): Response<RoomDto>

    /**
     * Toggle whether the authenticated user's room is listed.
     */
    @PATCH("users/me/room/visibility/")
    suspend fun setVisibility(
        @Body body: VisibilityRequest
    ): Response<VisibilityResponse>
}
