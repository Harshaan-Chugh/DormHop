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
 * Response wrapper for the user's saved‐rooms endpoint.
 */
data class SavedRoomsResponse(
    @SerializedName("saved_rooms")
    val savedRooms: List<RoomDto>
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
 * Payload to create or update a user.
 */
data class UpdateUserRequest(
    @SerializedName("full_name")  val fullName:  String,
    @SerializedName("class_year") val classYear: Int,
    @SerializedName("is_room_listed") val isRoomListed: Boolean
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

/**
 * Body for adding/removing a saved room.
 */
data class RoomIdRequest(
    @SerializedName("room_id")
    val roomId: Int
)


/**
 * All the repsonses for the knocks
 */
data class KnockRequest(
    @SerializedName("to_room_id") val toRoomId: Int
)

data class AcceptRequest(
    val status: String = "accepted"
)

data class KnockResponse(
    val id: Int,
    val from_user: UserDto,
    val to_room: RoomDto,
    val status: String,
    val created_at: String,
    val accepted_at: String?,
    val contacts: Contacts? = null
)

data class Contacts(
    val requester_email: String,
    val owner_email: String
)

data class KnocksListResponse(
    val knocks: List<KnockResponse>
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
    @PATCH("users/me/")
    suspend fun updateUser(
        @Body body: UpdateUserRequest
    ): Response<UserDto>

    /**
     * Update Room Details
     */
    @PATCH("users/me/room/")
    suspend fun updateRoom(
        @Body body: UpdateRoomRequest
    ): Response<RoomDto>

    /**
     * Create Room Details
     */
    @POST("users/me/room/")
    suspend fun createRoom(                    // <─ add this
        @Body body: UpdateRoomRequest
    ): Response<RoomDto>

    /**
     * Toggle whether the authenticated user's room is listed.
     */
    @PATCH("users/me/room/visibility/")
    suspend fun setVisibility(
        @Body body: VisibilityRequest
    ): Response<VisibilityResponse>

    /**
     * Saved Room Endpoints
     */
    @GET("users/me/saved_rooms/")
    suspend fun listSavedRooms(): Response<SavedRoomsResponse>

    @DELETE("users/me/saved_rooms/{room_id}/")
    suspend fun unsaveRoom(@Path("room_id") roomId: Int): Response<Unit>

    @POST("users/me/saved_rooms/")
    suspend fun saveRoom(@Body req: RoomIdRequest): Response<Unit>

    /**
     * Knock Room Endpoints
     */
    @POST("knocks/")
    suspend fun sendKnock(
        @Body req: KnockRequest
    ): Response<KnockResponse>

    @GET("knocks/sent/")
    suspend fun listSentKnocks(): Response<KnocksListResponse>

    @GET("knocks/received/")
    suspend fun listReceivedKnocks(): Response<KnocksListResponse>

    @PATCH("knocks/{id}/")
    suspend fun acceptKnock(
        @Path("id") knockId: Int,
        @Body req: AcceptRequest
    ): Response<KnockResponse>

    @DELETE("knocks/{id}/")
    suspend fun deleteKnock(
        @Path("id") knockId: Int
    ): Response<Unit>


    /**
     * Use the webscraper to get community features
     */
    @GET("dorm_features/")
    suspend fun getDormFeatures(): Response<DormFeaturesResponse>
}