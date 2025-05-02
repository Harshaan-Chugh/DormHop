package com.example.dormhopfrontend.model

import android.content.ContentValues.TAG
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class RoomRepository @Inject constructor(
    private val api: ApiService
) {

    /**
     * Fetches all listed rooms.
     */
    suspend fun listRooms(): List<RoomDto> {
        return try {
            val resp = api.listRooms()
            if (resp.isSuccessful) {
                resp.body()?.rooms.orEmpty()
            } else {
                Log.e(TAG, "listRooms failed: ${resp.code()} ${resp.message()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in listRooms", e)
            emptyList()
        }
    }

<<<<<<< HEAD
    /** Fetch one room by ID, or null on failure */
    suspend fun getRoomById(roomId: Int): RoomDto? {
        return try {
            val resp = api.getRoom(roomId)
            if (resp.isSuccessful) resp.body()
            else {
                Log.e("RoomRepo", "getRoomById failed: ${resp.code()} ${resp.message()}")
                null
            }
        } catch(e: Exception) {
            Log.e("RoomRepo", "Exception in getRoomById", e)
            null
        }
    }

=======
>>>>>>> b415288ab12c93f049217ef3a1cbb40ddf906c9c
    /**
     * Fetches personalized recommendations.
     */
    suspend fun recommendRooms(): List<RoomDto> {
        return try {
            val resp = api.recommendRooms()
            if (resp.isSuccessful) {
                resp.body()?.rooms.orEmpty()
            } else {
                Log.e(TAG, "recommendRooms failed: ${resp.code()} ${resp.message()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in recommendRooms", e)
            emptyList()
        }
    }

    /**
     * Creates or updates the current user's room.
     */
    suspend fun updateRoom(
        dorm: String,
        roomNumber: String,
        occupancy: Int,
        amenities: List<String>,
        description: String?
    ): RoomDto? {
        return try {
            val request = UpdateRoomRequest(
                dorm = dorm,
                roomNumber = roomNumber,
                occupancy = occupancy,
                amenities = amenities,
                description = description
            )
            val resp = api.updateRoom(request)
            if (resp.isSuccessful) {
                resp.body()
            } else {
                Log.e(TAG, "updateRoom failed: ${resp.code()} ${resp.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in updateRoom", e)
            null
        }
    }

    /**
     * Toggles whether the user's room is publicly listed.
     *
     * @return the updated listing flag
     */
    suspend fun setRoomVisibility(isListed: Boolean): Boolean? {
        return try {
            val resp = api.setVisibility(VisibilityRequest(isListed))
            if (resp.isSuccessful) {
                resp.body()?.isRoomListed
            } else {
                Log.e(TAG, "setRoomVisibility failed: ${resp.code()} ${resp.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in setRoomVisibility", e)
            null
        }
    }
}