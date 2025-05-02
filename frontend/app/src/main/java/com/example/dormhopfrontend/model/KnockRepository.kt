package com.example.dormhopfrontend.model

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KnockRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun sendKnock(roomId: Int) = api.sendKnock(KnockRequest(roomId))
    suspend fun listSent()   = api.listSentKnocks()
    suspend fun listReceived() = api.listReceivedKnocks()
    suspend fun accept(id: Int)  = api.acceptKnock(id, AcceptRequest())
    suspend fun delete(id: Int)  = api.deleteKnock(id)
}