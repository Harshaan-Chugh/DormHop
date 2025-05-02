package com.example.dormhopfrontend.model.network

import com.example.dormhopfrontend.model.ApiService
import com.example.dormhopfrontend.model.RoomDto
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SavedRoomsRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun getSavedRooms(): List<RoomDto> =
        try {
            val resp = api.listSavedRooms()
            if (resp.isSuccessful) resp.body()?.savedRooms.orEmpty()
            else emptyList()
        } catch (t: Throwable) {
            emptyList()
        }

    suspend fun unsave(roomId: Int): Boolean =
        try {
            val resp = api.unsaveRoom(roomId)
            resp.isSuccessful
        } catch (t: Throwable) {
            false
        }
}
